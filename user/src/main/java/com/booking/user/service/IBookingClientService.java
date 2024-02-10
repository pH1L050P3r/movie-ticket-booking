package com.booking.user.service;

import com.booking.user.exceptions.BookingNotFoundException;

public interface IBookingClientService {
    public String deleteBookingsByUserId(Long userId) throws BookingNotFoundException;
    public String deleteAllBookings();
}
