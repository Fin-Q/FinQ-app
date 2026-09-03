package com.swyp.FinQ.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

  HttpStatus status();

  String errorCode();

  String message();
}