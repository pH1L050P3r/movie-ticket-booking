package com.booking.user.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.Column;
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
	private Long id;
	
	
	@NotNull
	@Column(nullable = false)
	private String name;

	@Column(unique = true)
	@Email(regexp = "[A-Za-z0-9.]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,3}")
	private String email;

	public User(String name, String email){
		this.name = name;
		this.email = email;
	}

}
