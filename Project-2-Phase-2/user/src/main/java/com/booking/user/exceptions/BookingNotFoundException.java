package com.booking.user.exceptions;

public class BookingNotFoundException extends Exception {
    public BookingNotFoundException(String message){
        super(message);
    }
}
