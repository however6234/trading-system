package com.capital.service.impl;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.capital.AccountType;
import com.capital.domain.shared.Account;
import com.capital.domain.user.User;
import com.capital.exception.StatusCode;
import com.capital.exception.TradingException;
import com.capital.repository.AccountRepository;
import com.capital.repository.UserRepository;
import com.capital.service.UserService;

@Service
@Transactional(isolation=Isolation.READ_COMMITTED,rollbackFor = TradingException.class)
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AccountRepository accountRepository;

	public Account recharge(Long userId, BigDecimal amount) throws TradingException {
		User user = userRepository.findByIdAndActive(userId, true);
		if (user == null) {
			throw StatusCode.USER_NOT_FOUND.toException();
		}
		user.recharge(amount);
		userRepository.save(user);
		return user.getAccount();
	}

	public User createUser(String userName, String email) throws TradingException {
		if (userRepository.existsByUserName(userName)) {
			throw StatusCode.USER_ALREADY_EXISTS.toException();
		}
		if (userRepository.existsByEmail(email)) {
			throw StatusCode.EMAIL_EXISTS.toException();
		}

		Account account = new Account();
		account.setCurrency("CNY");
		account.setAccountTYpe(AccountType.User);
		account.setBalance(BigDecimal.ZERO);
		account.setActive(true);
		accountRepository.save(account);

		User user = new User();
		user.setUserName(userName);
		user.setEmail(email);
		user.setAccountId(account.getId());
		user.setActive(true);
		userRepository.save(user);

		return user;
	}
}
