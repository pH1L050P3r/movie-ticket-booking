package com.booking.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowDTO {
    private Long id;

    @JsonProperty("theatre_id")
    private Long theatreId;

    private String title;
    
    private Long price;

    @JsonProperty("seats_available")
    private Long seatsAvailable;
}
