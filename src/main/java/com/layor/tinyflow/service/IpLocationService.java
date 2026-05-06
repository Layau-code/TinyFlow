package com.layor.tinyflow.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * IP 地理位置解析服务
 * 简化版本：暂时返回空值，后续可集成 ip2region 或第三方 API
 */
@Service
@Slf4j
public class IpLocationService {

    @PostConstruct
    public void init() {
        log.info("[IpLocation] IP location service initialized (simplified mode)");
    }

    /**
     * 解析 IP 地址获取地理位置
     * @param ip IP 地址
     * @return 格式: "国家|区域|省份|城市|ISP"
     */
    public String search(String ip) {
        if (ip == null || ip.isEmpty()) {
            return null;
        }
        // 过滤私有/本地 IP
        if (isPrivateIp(ip)) {
            return "内网|内网|内网|内网|内网";
        }
        // 简化实现：返回空，后续可集成 ip2region
        return null;
    }

    /**
     * 解析城市名
     */
    public String getCity(String ip) {
        String region = search(ip);
        if (region == null) return "";
        // 格式: 国家|区域|省份|城市|ISP
        String[] parts = region.split("\\|");
        if (parts.length >= 4) {
            String city = parts[3];
            // 如果城市为 "0" 或空，返回省份
            if ("0".equals(city) || city.isEmpty()) {
                return parts.length >= 3 ? parts[2] : "";
            }
            return city;
        }
        return "";
    }

    /**
     * 解析国家名
     */
    public String getCountry(String ip) {
        String region = search(ip);
        if (region == null) return "";
        String[] parts = region.split("\\|");
        if (parts.length >= 1) {
            String country = parts[0];
            return "0".equals(country) ? "" : country;
        }
        return "";
    }

    /**
     * 判断是否为私有 IP
     */
    private boolean isPrivateIp(String ip) {
        if (ip == null) return true;
        return ip.startsWith("10.") ||
               ip.startsWith("172.16.") || ip.startsWith("172.17.") ||
               ip.startsWith("172.18.") || ip.startsWith("172.19.") ||
               ip.startsWith("172.20.") || ip.startsWith("172.21.") ||
               ip.startsWith("172.22.") || ip.startsWith("172.23.") ||
               ip.startsWith("172.24.") || ip.startsWith("172.25.") ||
               ip.startsWith("172.26.") || ip.startsWith("172.27.") ||
               ip.startsWith("172.28.") || ip.startsWith("172.29.") ||
               ip.startsWith("172.30.") || ip.startsWith("172.31.") ||
               ip.startsWith("192.168.") ||
               ip.equals("127.0.0.1") ||
               ip.equals("localhost") ||
               ip.equals("::1");
    }
}