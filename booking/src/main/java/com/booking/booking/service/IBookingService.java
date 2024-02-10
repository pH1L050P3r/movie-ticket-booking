package com.booking.booking.service;


import java.util.List;

import com.booking.booking.dto.BookingCreateRequestDTO;
import com.booking.booking.dto.BookingResponseDTO;
import com.booking.booking.exceptions.BookingServiceException;
import com.booking.booking.exceptions.WallerServiceException;

public interface IBookingService {
    public BookingResponseDTO create(BookingCreateRequestDTO bookingRequestData)throws WallerServiceException,BookingServiceException;
    public List<BookingResponseDTO> getByUserId(Long userId);
    public void deleteByUserId(Long userId) throws BookingServiceException;
    public void deleteByUserIdAndShowId(Long userId, Long showId) throws BookingServiceException;
    public void deleteAll() throws BookingServiceException;
}
