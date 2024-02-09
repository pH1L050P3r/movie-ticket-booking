package com.booking.booking.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.booking.booking.dto.BookingCreateRequestDTO;
import com.booking.booking.dto.BookingResponseDTO;
import com.booking.booking.enums.Action;
import com.booking.booking.exceptions.BookingServiceException;
import com.booking.booking.exceptions.WallerServiceException;
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
    @Autowired
    private IWalletClientService walletClientService;

    public BookingResponseDTO createBooking(BookingCreateRequestDTO bookingRequestData) throws WallerServiceException, BookingServiceException{
        Booking booking = new Booking();
        Show show;
        Long price = 0L;

        //Todo : Check user exists or not
        try{
            show = showRepositories.findById(bookingRequestData.getShowId()).get();
        } catch(NoSuchElementException e){
            throw new BookingServiceException("Show with specified id does not exists");
        }

        if(show.getSeatsAvailable() < bookingRequestData.getSeatsBooked())
            throw new BookingServiceException("Required no of seats are not available");

        booking.setSeatsBooked(bookingRequestData.getSeatsBooked());
        booking.setUserId(bookingRequestData.getUserId());
        booking.setShow(show);
        show.setSeatsAvailable(show.getSeatsAvailable() - booking.getSeatsBooked());
        
        price += show.getPrice() * booking.getSeatsBooked();
        
        //debit money from Wallet with price
        try{
            walletClientService.updateUserWalletMoney(price, bookingRequestData.getUserId(), Action.debit);
        } catch(Exception e){
            throw new WallerServiceException(e.getMessage());
        }

        bookingRepositories.save(booking);
        return BookingMapper.mapBookingToBookingResponseDTO(booking);
    }

    public List<BookingResponseDTO> getUserBookings(Long userId){
        List<BookingResponseDTO> bookings = new ArrayList<BookingResponseDTO>();
        for(Booking booking: bookingRepositories.findAllByUserId(userId))
            bookings.add(BookingMapper.mapBookingToBookingResponseDTO(booking));
        return bookings;
    }

    public void deleteUserBookings(Long userId) throws BookingServiceException{
        List<Booking> bookings = bookingRepositories.findAllByUserId(userId);
        this.deleteBookingHelper(bookings);
    }

    public void deleteUserShowBookings(Long userId, Long showId) throws BookingServiceException{
        List<Booking> bookings = bookingRepositories.findAllByUserIdAndShowId(userId, showId);
        this.deleteBookingHelper(bookings);
    }

    public void deleteAllBookings() throws BookingServiceException{
        List<Booking> booking = bookingRepositories.findAll();
        this.deleteBookingHelper(booking);
    }

    private void deleteBookingHelper(List<Booking> bookings) throws BookingServiceException{
        if(bookings.isEmpty()){
            throw new BookingServiceException("Bookings does not exists");
        }

        Map<Long, Show> Shows = new HashMap<>();
        Map<Long, Long> userAmountRefund = new HashMap<>();

        for(Booking booking : bookings){
            Shows.put(booking.getShow().getId(), booking.getShow());
            userAmountRefund.put(booking.getUserId(), 0L);
        }

        for(Booking booking : bookings){
            Show show = Shows.get(booking.getShow().getId());
            show.setSeatsAvailable(show.getSeatsAvailable() + booking.getSeatsBooked());
            userAmountRefund.put(booking.getUserId(), userAmountRefund.get(booking.getUserId()) + show.getPrice()*booking.getSeatsBooked());
        }
        //API request to Add money in Wallet
        for(Long userId : userAmountRefund.keySet()){
            walletClientService.updateUserWalletMoney(userAmountRefund.get(userId), userId, Action.credit);
        }
        bookingRepositories.deleteAll(bookings);
    }
}
