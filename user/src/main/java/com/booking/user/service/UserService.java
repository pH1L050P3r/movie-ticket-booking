package com.booking.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.booking.user.models.User;
import com.booking.user.repository.UserRepository;
import com.booking.user.service.IWalletClientService;

@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private IWalletClientService walletClientService;

	
	public User createUser(String userName, String email) {
		User user = new User(userName, email);
		return userRepository.save(user);
	}
	
	public boolean deleteUser(Long userId) {
		if(!userRepository.existsById(userId)) 
			return false;
		// bookingClientService.deleteBookingsByUserId(userId);
		walletClientService.deleteWalletById(userId);
		userRepository.deleteById(userId);
		return true;
    }

	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	public void deleteAllUsers() {
		// bookingClientService.deleteAllBookings();
		walletClientService.deleteAllWallets();
		userRepository.deleteAllInBatch();
	}
}
