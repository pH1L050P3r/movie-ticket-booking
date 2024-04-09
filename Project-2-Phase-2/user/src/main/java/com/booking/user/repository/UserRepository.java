package com.booking.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.booking.user.models.*;

public interface UserRepository extends JpaRepository<User, Long>{
	
	boolean existsByEmail(String email);
	
}

