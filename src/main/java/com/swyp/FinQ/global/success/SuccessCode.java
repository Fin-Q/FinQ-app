package com.swyp.FinQ.global.success;

import org.springframework.http.HttpStatus;

public interface SuccessCode {

  HttpStatus status();

  String message();
}