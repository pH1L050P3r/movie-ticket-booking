package com.booking.booking.mapper;

import com.booking.booking.dto.TheatreDTO;
import com.booking.booking.models.Theatre;


public class TheatreMapper {
    public static TheatreDTO mapTheatreToTheatreDTO(Theatre theatre){
        return new TheatreDTO(theatre.getId(), theatre.getName(), theatre.getLocation());
    }
}
