package com.booking.user.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name="User")
@Data
@NoArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	//Todo : name cannot be empty
	private String name;
	//Todo : add unique and check string is in email format or not
	private String email;

	public User(String name, String email){
		this.name = name;
		this.email = email;
	}

}
