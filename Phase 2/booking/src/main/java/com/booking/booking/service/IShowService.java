package com.booking.booking.service;

import java.util.List;

import com.booking.booking.dto.ShowDTO;

public interface IShowService {
    public ShowDTO getById(Long showId);
    public List<ShowDTO> getAllByTheaterId(Long theatreId);
    public boolean existsByTheatreId(Long theatreId);
}
