package com.booking.booking.service;


import java.util.List;

import com.booking.booking.dto.BookingCreateRequestDTO;
import com.booking.booking.dto.BookingResponseDTO;
import com.booking.booking.exceptions.BookingServiceException;
import com.booking.booking.exceptions.WallerServiceException;

public interface IBookingService {
    public BookingResponseDTO createBooking(BookingCreateRequestDTO bookingRequestData)throws WallerServiceException,BookingServiceException;
    public List<BookingResponseDTO> getUserBookings(Long userId);
    public void deleteUserBookings(Long userId) throws BookingServiceException;
    public void deleteUserShowBookings(Long userId, Long showId) throws BookingServiceException;
    public void deleteAllBookings() throws BookingServiceException;
}
