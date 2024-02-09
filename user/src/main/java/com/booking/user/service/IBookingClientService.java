package com.booking.user.service;

public interface IBookingClientService {
    public String deleteBookingsByUserId(Long userId);
    public String deleteAllBookings();
}
