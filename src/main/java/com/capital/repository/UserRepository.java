package com.capital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.capital.domain.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
	
	User findByIdAndActive(Long userId,boolean active);
	
    User findByUserName(@Param("userName") String userName);
	
	boolean existsByUserName(String userName);
	
	int countByUserName(String userName);
	
	boolean existsByEmail(String email);
	
	int countByEmail(String email);
}
