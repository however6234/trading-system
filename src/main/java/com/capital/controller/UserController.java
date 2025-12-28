package com.capital.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capital.domain.dto.CreateUserRequest;
import com.capital.domain.dto.RechargeRequest;
import com.capital.domain.dto.Result;
import com.capital.domain.shared.Account;
import com.capital.domain.user.User;
import com.capital.exception.StatusCode;
import com.capital.exception.TradingException;
import com.capital.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	@Autowired
	private UserService userService;

	@PostMapping("/{userId}/recharge")
	@Operation(summary = "user recharge", description = "user recharge for account")
	public Result recharge(@PathVariable Long userId, @Valid @RequestBody RechargeRequest request) {
		try {
			if (!"CNY".equals(request.getCurrency())) {
				throw StatusCode.CURRENCY_NOT_SUPPORTED.toException();
			}
			Account account = userService.recharge(userId, request.getAmount());
			return Result.success(StatusCode.SUCCESS, account);
		} catch (TradingException e) {
			return Result.failure(e.toStatusCode());
		}
	}

	@PostMapping
	@Operation(summary = "create user", description = "create user, will create account by the way")
	public Result createUser(@Valid @RequestBody CreateUserRequest request) {
		try {
			User user = userService.createUser(request.getUsername(), request.getEmail());
			return Result.success(StatusCode.SUCCESS, user);
		} catch (TradingException e) {
			return Result.failure(e.toStatusCode());
		}
	}
}