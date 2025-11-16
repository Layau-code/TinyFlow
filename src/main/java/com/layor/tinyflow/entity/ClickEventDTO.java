package com.layor.tinyflow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ClickEventDTO {
    /**
     * 时间戳，记录点击事件发生的时间
     */
    private LocalDateTime ts;
    
    /**
     * IP地址，记录发起点击的客户端IP
     */
    private String ip;
    
    /**
     * 来源主机，记录请求来源的主机信息
     */
    private String sourceHost;
    
    /**
     * 设备类型，记录用户使用的设备类型（如手机、电脑等）
     */
    private String deviceType;
    
    /**
     * 城市信息，记录用户所在的城市
     */
    private String city;
    
    /**
     * 国家信息，记录用户所在的国家
     */
    private String country;
    
    /**
     * User-Agent，记录客户端的浏览器和系统信息
     */
    private String ua;
    
    /**
     * 引荐来源，记录用户从哪个页面跳转而来
     */
    private String referer;
}