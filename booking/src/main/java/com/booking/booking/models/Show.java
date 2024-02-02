package com.booking.booking.models;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Data
@Table(name = "show")
public class Show {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "threatre_id")
    private Long theatreId;
    
    @Column(name = "title")
    private String title;

    @Column(name = "price")
    private Long price;

    @Column(name = "seats_available")
    private Long seatsAvailable;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "show")
    private Set<Booking> bookings = new HashSet<>();
}