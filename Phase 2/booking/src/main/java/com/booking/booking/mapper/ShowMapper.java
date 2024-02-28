package com.booking.booking.mapper;

import com.booking.booking.dto.ShowDTO;
import com.booking.booking.models.Show;

public class ShowMapper {
    public static ShowDTO showToShowDTO(Show show){
        return new ShowDTO(
            show.getId(),
            show.getTheatre().getId(),
            show.getTitle(),
            show.getPrice(),
            show.getSeatsAvailable()
        );
    }
}
