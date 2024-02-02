package com.booking.booking.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.booking.booking.models.Booking;
import com.booking.booking.models.Show;
import com.booking.booking.repositories.BookingRepositories;
import com.booking.booking.repositories.ShowRepositories;

@RestController
public class BookingController {
    @Autowired
    private BookingRepositories bookingRepositories;
    @Autowired
    private ShowRepositories showRepositories;

    @GetMapping("/bookings/users/{user_id}")
    public ResponseEntity<List<Booking>> getUserAllBooking(@PathVariable("user_id") Long userId){
        return new ResponseEntity<>(bookingRepositories.findAllByUserId(userId)), HttpStatus.OK);
    }

    @PostMapping("/bookings")
    public ResponseEntity<?> createUserBooking(@RequestBody Booking booking){
    }
}
