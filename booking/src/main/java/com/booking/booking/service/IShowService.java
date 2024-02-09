package com.booking.booking.service;

import java.util.List;

import com.booking.booking.dto.ShowDTO;

public interface IShowService {
    public ShowDTO getShowById(Long showId);
    public List<ShowDTO> getAllShowShowcasedByTheater(Long theatreId);
}
