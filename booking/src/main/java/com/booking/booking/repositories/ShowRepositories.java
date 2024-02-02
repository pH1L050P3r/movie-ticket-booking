package com.booking.booking.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.booking.booking.models.Show;

public interface ShowRepositories extends JpaRepository<Show, Long> {
    
}
