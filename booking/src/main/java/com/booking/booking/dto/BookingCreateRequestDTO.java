package com.booking.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateRequestDTO {
    
    @JsonProperty("show_id")
    @NotNull
    private Long showId;
    
    @JsonProperty("user_id")
    @NotNull
    private Long userId;
    
    @JsonProperty("seats_booked")
    @NotNull
    @Min(1)
    private Long seatsBooked;
}
