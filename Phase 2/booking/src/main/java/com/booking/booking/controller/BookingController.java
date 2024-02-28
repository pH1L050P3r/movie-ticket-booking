package com.booking.booking.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.booking.booking.dto.BookingCreateRequestDTO;
import com.booking.booking.dto.BookingResponseDTO;
import com.booking.booking.service.IBookingService;

import jakarta.validation.Valid;

@RestController
public class BookingController {

    @Autowired
    private IBookingService bookingService;

    @GetMapping("/bookings/users/{user_id}")
    public ResponseEntity<List<BookingResponseDTO>> getUserAllBooking(@PathVariable("user_id") Long userId){
        return new ResponseEntity<>(bookingService.getByUserId(userId), HttpStatus.OK);
    }

    @PostMapping("/bookings")
    public ResponseEntity<?> createUserBooking(@Valid @RequestBody BookingCreateRequestDTO bookingRequest){
        try{
            bookingService.create(bookingRequest);
            return new ResponseEntity<HttpStatus>(HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<HttpStatus>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/bookings/users/{user_id}")
    public ResponseEntity<?> deleteUserAllBookings(@PathVariable("user_id") Long userId){
        try{
            bookingService.deleteByUserId(userId);
            return new ResponseEntity<HttpStatus>(HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<HttpStatus>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/bookings/users/{user_id}/shows/{show_id}")
    public ResponseEntity<?> deleteUserAllBookingAssociatedWithShow(@PathVariable("user_id") Long userId, @PathVariable("show_id") Long showId){
        try{
            bookingService.deleteByUserIdAndShowId(userId, showId);
            return new ResponseEntity<HttpStatus>(HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<HttpStatus>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/bookings")
    public ResponseEntity<?> deleteAllBookings(){
        try{
            bookingService.deleteAll();;
            return new ResponseEntity<HttpStatus>(HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<HttpStatus>(HttpStatus.OK);
        }
    }
}
