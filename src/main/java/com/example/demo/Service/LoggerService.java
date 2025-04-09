package com.example.demo.Service;

public interface LoggerService {
    void logInfo(String message);
    void logWarning(String message);
    void logError(String message);
    void logError(String message, Exception ex);
}
