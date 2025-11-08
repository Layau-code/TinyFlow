package com.layor.tinyflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@SpringBootApplication
@EnableAsync
@EnableTransactionManagement
public class TinyFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(TinyFlowApplication.class, args);
    }
    @Service
    public static class LogService {
        @Async("taskExecutor")
        public void recordVisit(String code) {
        }
    }

        @Bean("taskExecutor")
        public Executor taskExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            //配置参数...省略
            executor.initialize();
            return executor;
        }
}
