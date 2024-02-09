package com.booking.booking.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.booking.booking.dto.ShowDTO;
import com.booking.booking.mapper.ShowMapper;
import com.booking.booking.repositories.ShowRepositories;

@Service
public class ShowService implements IShowService {
    @Autowired
    private ShowRepositories showRepositories;

    public ShowDTO getShowById(Long showId){
        return ShowMapper.showToShowDTO(
            showRepositories.findById(showId).get()
        );
    }

    public List<ShowDTO> getAllShowShowcasedByTheater(Long theatreId){
        return showRepositories.findAllByTheatreId(theatreId).stream().map(
            show -> {return ShowMapper.showToShowDTO(show);}
        ).collect(Collectors.toList());
    }
}
