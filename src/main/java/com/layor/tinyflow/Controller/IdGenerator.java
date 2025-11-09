package com.layor.tinyflow.Controller;

public interface IdGenerator {
    long nextId(String bizTag);
}