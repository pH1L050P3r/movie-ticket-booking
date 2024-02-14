package com.booking.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.booking.user.dto.UserDTO;
import com.booking.user.enums.Action;
import com.booking.user.exceptions.BookingNotFoundException;
import com.booking.user.mapper.UserMapper;
import com.booking.user.models.User;
import com.booking.user.repository.UserRepository;

@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private IWalletClientService walletClientService;
	@Autowired
	private IBookingClientService bookingClientService;

	
	public UserDTO createUser(String userName, String email) {
		User user = new User(userName, email);
		user = userRepository.save(user);
		// creating user associated wallet at the time of user creation
		walletClientService.updateUserWalletMoney(user.getId(), 0L, Action.credit);
		return UserMapper.mapUserToUserDTO(user);
	}
	
	public boolean deleteUser(Long userId) {
		if(!userRepository.existsById(userId)) 
			return false;
		
		try{
			bookingClientService.deleteBookingsByUserId(userId);
		}catch(BookingNotFoundException e){
			//Booking microservice returns 404 if no booking found for user else delete's all booking
			//so BookingClientService throws BookingNotFoundException if micro services returns 404 
		}
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
		// while deleting user delete all associated entities i.e. user bookings, user wallet
		try{
			bookingClientService.deleteAllBookings();
		} catch (Exception e){
			// make exception silent
		}
		walletClientService.deleteAllWallets();
		userRepository.deleteAllInBatch();
	}
}
