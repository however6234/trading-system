package com.capital.domain.dto;

import com.capital.exception.StatusCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Result{
	private boolean success;
	private String code;
	private String message;
	private Object data;

	public static Result failure(StatusCode errorCode) {
		return new Result(false, errorCode.getCode(), errorCode.getMessage(), null);
	}

	public static Result success(StatusCode statusCode, Object data) {
		return new Result(true, statusCode.getCode(), statusCode.getMessage(), data);
	}
}
