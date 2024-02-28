package com.booking.booking.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.booking.booking.models.Theatre;

public interface TheatreRepositories extends JpaRepository<Theatre, Long> {
    
}
