package com.booking.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.booking.user.dto.UserDTO;
import com.booking.user.enums.Action;
import com.booking.user.mapper.UserMapper;
import com.booking.user.models.User;
import com.booking.user.repository.UserRepository;
import com.booking.user.service.IWalletClientService;

@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private IWalletClientService walletClientService;

	
	public UserDTO createUser(String userName, String email) {
		User user = new User(userName, email);
		user = userRepository.save(user);
		walletClientService.updateUserWalletMoney(user.getId(), 0L, Action.credit);
		return UserMapper.mapUserToUserDTO(user);
	}
	
	public boolean deleteUser(Long userId) {
		if(!userRepository.existsById(userId)) 
			return false;
		// bookingClientService.deleteBookingsByUserId(userId);
		walletClientService.deleteWalletById(userId);
		userRepository.deleteById(userId);
		return true;
    }

	public boolean isUserExists(Long userId){
		return userRepository.existsById(userId);
	}

	public UserDTO getUserById(Long userId){
		User user = userRepository.findById(userId).get();
		return UserMapper.mapUserToUserDTO(user);
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
