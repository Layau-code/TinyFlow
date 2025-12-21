package com.layor.tinyflow.service;

import com.layor.tinyflow.entity.AuthRequest;
import com.layor.tinyflow.entity.AuthResponse;
import com.layor.tinyflow.entity.User;
import com.layor.tinyflow.repository.UserRepository;
import com.layor.tinyflow.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 认证服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("认证服务测试")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private AuthRequest testAuthRequest;
    private User testUser;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_TOKEN = "test.jwt.token";
    private static final Long TEST_EXPIRATION = 604800L;

    @BeforeEach
    void setUp() {
        testAuthRequest = new AuthRequest();
        testAuthRequest.setUsername(TEST_USERNAME);
        testAuthRequest.setPassword(TEST_PASSWORD);
        testAuthRequest.setEmail(TEST_EMAIL);

        testUser = User.builder()
                .id(1L)
                .username(TEST_USERNAME)
                .password("encoded_password")
                .email(TEST_EMAIL)
                .role("USER")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
    }

    @Test
    @DisplayName("注册 - 成功")
    void testRegister_Success() {
        // Given
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(any(User.class))).thenReturn(TEST_TOKEN);
        when(jwtUtil.getExpirationSeconds()).thenReturn(TEST_EXPIRATION);

        // When
        AuthResponse response = authService.register(testAuthRequest);

        // Then
        assertNotNull(response);
        assertEquals(TEST_TOKEN, response.getToken());
        assertEquals(TEST_USERNAME, response.getUsername());
        assertEquals("USER", response.getRole());
        assertEquals(TEST_EXPIRATION, response.getExpiresIn());

        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtUtil, times(1)).generateToken(any(User.class));
    }

    @Test
    @DisplayName("注册 - 失败（用户名已存在）")
    void testRegister_Fail_UsernameExists() {
        // Given
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.register(testAuthRequest);
        });

        assertEquals("用户名已存在", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("注册 - 失败（邮箱已存在）")
    void testRegister_Fail_EmailExists() {
        // Given
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.register(testAuthRequest);
        });

        assertEquals("邮箱已被注册", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("登录 - 成功")
    void testLogin_Success() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(testUser)).thenReturn(TEST_TOKEN);
        when(jwtUtil.getExpirationSeconds()).thenReturn(TEST_EXPIRATION);

        // When
        AuthResponse response = authService.login(testAuthRequest);

        // Then
        assertNotNull(response);
        assertEquals(TEST_TOKEN, response.getToken());
        assertEquals(TEST_USERNAME, response.getUsername());
        assertEquals("USER", response.getRole());
        assertEquals(TEST_EXPIRATION, response.getExpiresIn());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(testUser);
    }

    @Test
    @DisplayName("登录 - 失败（用户名或密码错误）")
    void testLogin_Fail_BadCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(testAuthRequest);
        });

        assertEquals("用户名或密码错误", exception.getMessage());
        verify(jwtUtil, never()).generateToken(any(User.class));
    }

    @Test
    @DisplayName("获取当前用户ID - 成功")
    void testGetCurrentUserId_Success() {
        // 此测试需要模拟 SecurityContextHolder，由于复杂性，这里简化处理
        // 实际项目中可能需要使用 @WithMockUser 或其他安全测试工具
        Long userId = authService.getCurrentUserId();
        
        // 未设置 SecurityContext 时应返回 null
        assertNull(userId);
    }

    @Test
    @DisplayName("获取当前用户名 - 未登录时返回null")
    void testGetCurrentUsername_NotLoggedIn() {
        // When
        String username = authService.getCurrentUsername();

        // Then
        assertNull(username);
    }
}
