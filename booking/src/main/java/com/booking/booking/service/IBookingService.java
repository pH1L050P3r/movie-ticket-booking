package com.booking.booking.service;


import java.util.List;

import com.booking.booking.dto.BookingCreateRequestDTO;
import com.booking.booking.dto.BookingResponseDTO;

public interface IBookingService {
    public BookingResponseDTO createBooking(BookingCreateRequestDTO bookingRequestData);
    public List<BookingResponseDTO> getUserBookings(Long userId);
    public void deleteUserBookings(Long userId);
    public void deleteUserShowBookings(Long userId, Long showId);
    public void deleteAllBookings();
}
