package com.example.demo.Service.Impl;

import com.example.demo.Service.LoggerService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class LoggerServiceImpl implements LoggerService {
    private final Logger logger = LoggerFactory.getLogger(LoggerServiceImpl.class);
    @Override
    public void logInfo(String message) {
        logger.info("[INFO] - {} - {}", LocalDateTime.now(), message);
    }

    @Override
    public void logWarning(String message) {
        logger.warn("[WARNING] - {} - {}", LocalDateTime.now(), message);
    }

    @Override
    public void logError(String message) {
        logger.error("[ERROR] - {} - {}", LocalDateTime.now(), message);
    }

    @Override
    public void logError(String message, Exception ex) {
        logger.error("[ERROR] - {} - {}", LocalDateTime.now(), message, ex);
    }
}
