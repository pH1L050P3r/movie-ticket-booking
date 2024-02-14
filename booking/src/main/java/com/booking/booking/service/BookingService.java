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
    @Autowired
    private IUserClientService userClientService;

    public BookingResponseDTO create(BookingCreateRequestDTO bookingRequestData) throws BookingServiceException{
        Booking booking = new Booking();
        Show show;
        Long amount = 0L;

        try{
            userClientService.getUserById(bookingRequestData.getUserId());
        } catch(Exception e){
            //User does not exist so does not create any booking
            throw new BookingServiceException("User does not exists");
        }
        
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
        
        amount += show.getPrice() * booking.getSeatsBooked();
        
        //debit money from Wallet equals amount
        try{
            walletClientService.updateByUserId(amount, bookingRequestData.getUserId(), Action.debit);
        } catch(Exception e){
            throw new BookingServiceException(e.getMessage());
        }

        bookingRepositories.save(booking);
        return BookingMapper.mapBookingToBookingResponseDTO(booking);
    }

    public List<BookingResponseDTO> getByUserId(Long userId){
        List<BookingResponseDTO> bookings = new ArrayList<BookingResponseDTO>();
        for(Booking booking: bookingRepositories.findAllByUserId(userId))
            bookings.add(BookingMapper.mapBookingToBookingResponseDTO(booking));
        return bookings;
    }

    public void deleteByUserId(Long userId) throws BookingServiceException{
        List<Booking> bookings = bookingRepositories.findAllByUserId(userId);
        this.deleteBookingHelper(bookings);
    }

    public void deleteByUserIdAndShowId(Long userId, Long showId) throws BookingServiceException{
        List<Booking> bookings = bookingRepositories.findAllByUserIdAndShowId(userId, showId);
        this.deleteBookingHelper(bookings);
    }

    public void deleteAll() throws BookingServiceException{
        try{
            List<Booking> booking = bookingRepositories.findAll();
            this.deleteBookingHelper(booking);
        } catch(BookingServiceException e){
            
        }
    }

    private void deleteBookingHelper(List<Booking> bookings) throws BookingServiceException{
        if(bookings.isEmpty()){
            throw new BookingServiceException("Bookings does not exists");
        }

        Map<Long, Show> Shows = new HashMap<>();
        Map<Long, Long> userAmountRefund = new HashMap<>();

        // creating show list which going to be affected after deleting booking i.e (adding seats back to show)
        // creating user list which going to be affected after deleting booking i.e (adding amount back to the wallet : Refund)
        for(Booking booking : bookings){
            Shows.put(booking.getShow().getId(), booking.getShow());
            userAmountRefund.put(booking.getUserId(), 0L);
        }

        // Adding booked seats back to the show
        // Adding booking amount back to the connected user in userAmountRefund
        for(Booking booking : bookings){
            Show show = Shows.get(booking.getShow().getId());
            show.setSeatsAvailable(show.getSeatsAvailable() + booking.getSeatsBooked());
            userAmountRefund.put(booking.getUserId(), userAmountRefund.get(booking.getUserId()) + show.getPrice()*booking.getSeatsBooked());
        }
        //API request to Add money back into Wallet for all users : user and refundable amount stored in userAmountRefund in key, value pair
        for(Long userId : userAmountRefund.keySet()){
            walletClientService.updateByUserId(userAmountRefund.get(userId), userId, Action.credit);
        }
        bookingRepositories.deleteAll(bookings);
    }
}
