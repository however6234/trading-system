package com.capital.service;

import java.math.BigDecimal;

import com.capital.domain.shared.Account;
import com.capital.domain.user.User;
import com.capital.exception.TradingException;

public interface UserService {
	Account recharge(Long userId, BigDecimal amount) throws TradingException;

	User createUser(String username, String email) throws TradingException;
}
