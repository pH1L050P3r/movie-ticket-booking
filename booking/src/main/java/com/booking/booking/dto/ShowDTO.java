package com.booking.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShowDTO {
    private Long id;
    private Long theatreId;
    private String title;
    private Long price;
    private Long seatsAvailable;
}
