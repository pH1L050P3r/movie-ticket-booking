package com.booking.user.mapper;

import com.booking.user.dto.UserDTO;
import com.booking.user.models.User;

public class UserMapper {
    public static UserDTO mapUserToUserDTO(User user){
        return new UserDTO(
            user.getId(),
            user.getName(),
            user.getEmail()
        );
    }
}
