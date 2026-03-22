package com.ohpen.configtracker.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(InvalidConfigChangeException.class)
	public ResponseEntity<ErrorResponse> handleInvalidConfigChange(InvalidConfigChangeException ex) {
		ErrorResponse body = new ErrorResponse(ex.getMessage(), Instant.now());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(ConfigChangeNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleConfigChangeNotFound(ConfigChangeNotFoundException ex) {
		ErrorResponse body = new ErrorResponse(ex.getMessage(), Instant.now());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
		ErrorResponse body = new ErrorResponse("Unexpected error occurred", Instant.now());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}

}
