package com.booking.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {
    
    private Long id;
    
    @JsonProperty("show_id")
    private Long showId;
    
    @JsonProperty("user_id")
    private Long userId;
    
    @JsonProperty("seats_booked")
    private Long seatsBooked;
}
