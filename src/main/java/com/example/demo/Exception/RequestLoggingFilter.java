package com.example.demo.Exception;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RequestLoggingFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, jakarta.servlet.ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        logger.info("Filter - Request URI: " + req.getRequestURI());
        logger.info("Filter - Request method: " + req.getMethod());
        logger.info("Filter - Raw request parameters: " + req.getParameterMap().toString());
        chain.doFilter(request, response);
    }
}