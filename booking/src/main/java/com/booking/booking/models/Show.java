package com.booking.booking.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@Table(name = "show")
@NoArgsConstructor
@AllArgsConstructor
public class Show {
    @Id
    @Column(name = "id")
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "threatre_id")
    private Long theatreId;
    
    @Column(name = "title")
    private String title;

    @Column(name = "price")
    private Long price;

    @Column(name = "seats_available")
    private Long seatsAvailable;
}