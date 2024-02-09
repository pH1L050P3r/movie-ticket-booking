package com.booking.user.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.booking.user.models.User;
import com.booking.user.repository.UserRepository;

@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepository;
	
	public User createUser(String userName, String email) {
		User user = new User(userName, email);
		return userRepository.save(user);
	}
	
	public boolean deleteUser(Integer userId) {
		if(!userRepository.existsById(userId)) 
			return false;
		userRepository.deleteById(userId);
		deleteUserBookings(userId);
		deleteWallet(userId);
		return true;
    }

	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	public void deleteAllUsers() {
		List<User> users = userRepository.findAll();
		userRepository.deleteAllInBatch();
		for(User user: users){
			deleteUserBookings(user.getId());
			deleteWallet(user.getId());
		}
	}

	private void deleteWallet(Integer userId){
		//Todo : http request to wallet service and delete wallet
	}

	private void deleteUserBookings(Integer userId){
		//Todo: http request to booking service and delete bookings
	}

}
