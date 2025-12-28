package com.capital.domain.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class CreateUserRequest {
	@NotBlank
	private String username;
	@NotBlank
	@Email
	private String email;
}