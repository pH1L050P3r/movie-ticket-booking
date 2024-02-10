package com.booking.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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
