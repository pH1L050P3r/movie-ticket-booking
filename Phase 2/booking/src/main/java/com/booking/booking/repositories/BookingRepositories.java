package com.booking.booking.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.booking.booking.models.Booking;

public interface BookingRepositories extends JpaRepository<Booking, Long> {
    public List<Booking> findAllByUserId(Long userId);
    public List<Booking> findAllByUserIdAndShowId(Long userId, Long showId);
}
