package com.layor.tinyflow.service;

import org.springframework.stereotype.Component;

@Component
public interface IdGenerator {
    long nextId(String bizTag);
}