package com.booking.user.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.booking.user.dto.UserDTO;
import com.booking.user.dto.UserRequest;
import com.booking.user.service.UserService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;


@RestController
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@PostMapping("/users")
	public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest request) {
        // Check if user with email already exists
        if (userService.existsByEmail(request.getEmail()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with email already exists");
		
		UserDTO createdUser = userService.createUser(request.getName(), request.getEmail());
		return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
	}

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserEntity(@PathVariable("userId") Long userId) {
        try{
            return ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(userId));
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
	
	@DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable("userId") Long userId) {
        boolean isUserDeleted = userService.deleteUser(userId);
        if (!isUserDeleted)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully");
    }

    @DeleteMapping("/users")
    public ResponseEntity<?> deleteAllUsers() {
        userService.deleteAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body("All users deleted successfully");
    }
}
