package com.booking.booking.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.booking.booking.dto.BookingCreateRequestDTO;
import com.booking.booking.dto.BookingResponseDTO;
import com.booking.booking.mapper.BookingMapper;
import com.booking.booking.models.Booking;
import com.booking.booking.models.Show;
import com.booking.booking.repositories.BookingRepositories;
import com.booking.booking.repositories.ShowRepositories;

@Service
public class BookingService implements IBookingService {

    @Autowired
    private BookingRepositories bookingRepositories;
    @Autowired
    private ShowRepositories showRepositories;

    public BookingResponseDTO createBooking(BookingCreateRequestDTO bookingRequestData){
        Booking booking = new Booking();
        Show show = showRepositories.findById(bookingRequestData.getShowId()).get();
        Long price = 0L;

        //Todo : Check user exists or not
        //Todo : Check show exisxt or not

        if(show.getSeatsAvailable() < bookingRequestData.getSeatsBooked()){
            //Todo : Throw Exception becaouse seats are not available
        }

        booking.setSeatsBooked(bookingRequestData.getSeatsBooked());
        booking.setUserId(bookingRequestData.getUserId());
        booking.setShow(show);
        show.setSeatsAvailable(show.getSeatsAvailable() - booking.getSeatsBooked());
        
        price += show.getPrice() * booking.getSeatsBooked();
        
        //Todo : debit money from Wallet with price

        bookingRepositories.save(booking);
        return BookingMapper.mapBookingToBookingResponseDTO(booking);
    }

    public List<BookingResponseDTO> getUserBookings(Long userId){
        List<BookingResponseDTO> bookings = new ArrayList<BookingResponseDTO>();
        for(Booking booking: bookingRepositories.findAllByUserId(userId))
            bookings.add(BookingMapper.mapBookingToBookingResponseDTO(booking));
        return bookings;
    }

    public void deleteUserBookings(Long userId){
        List<Booking> bookings = bookingRepositories.findAllByUserId(userId);
        this.deleteBookingHelper(bookings);
    }

    public void deleteUserShowBookings(Long userId, Long showId){
        List<Booking> bookings = bookingRepositories.findAllByUserIdAndShowId(userId, showId);
        this.deleteBookingHelper(bookings);
    }

    public void deleteAllBookings(){
        List<Booking> booking = bookingRepositories.findAll();
        this.deleteBookingHelper(booking);
    }

    private void deleteBookingHelper(List<Booking> bookings){
        if(bookings.isEmpty()){
            //TODO : Raise Exception and catch Controller
        }
        Map<Long, Show> Shows = new HashMap<>();
        Long totalPrice = 0L;

        for(Booking booking : bookings){
            Shows.put(booking.getShow().getId(), booking.getShow());
        }

        for(Booking booking : bookings){
            Show show = Shows.get(booking.getShow().getId());
            show.setSeatsAvailable(show.getSeatsAvailable() + booking.getSeatsBooked());
            totalPrice += show.getPrice();
        }

        //TODO : API request to Add money in Wallet
        bookingRepositories.deleteAll(bookings);
    }
}
