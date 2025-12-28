package com.capital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capital.domain.shared.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
}
