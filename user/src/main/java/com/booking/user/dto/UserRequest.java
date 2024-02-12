package com.booking.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    @NotNull
    private String name;
    @Email(regexp = "[A-Za-z0-9.]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,3}")
    private String email;
}

