package com.example.demo.aop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  private Object handle(Exception e) {
    log.error("", e);
    return ResponseEntity.ok(Map.of("error", "發生錯誤!!"));
  }
}
