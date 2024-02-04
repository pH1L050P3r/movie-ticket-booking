package com.booking.booking.mapper;

import com.booking.booking.dto.BookingResponseDTO;
import com.booking.booking.models.Booking;

public class BookingMapper {
    public static BookingResponseDTO mapBookingToBookingResponseDTO(Booking booking){
        return new BookingResponseDTO(
            booking.getId(),
            booking.getShow().getId(),
            booking.getUserId(),
            booking.getSeatsBooked()
        );
    }
}
