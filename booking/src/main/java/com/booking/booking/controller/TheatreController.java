package com.booking.booking.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.booking.booking.dto.TheatreDTO;
import com.booking.booking.mapper.TheatreMapper;
import com.booking.booking.repositories.TheatreRepositories;

@RestController
public class TheatreController {
    @Autowired
    private TheatreRepositories threadRepositories;

    @GetMapping("/theatres")
    public ResponseEntity<List<TheatreDTO>> getAllTheatres(){
        return new ResponseEntity<>(
            threadRepositories.findAll().stream().map(
                show -> {return TheatreMapper.mapTheatreToTheatreDTO(show);}
            ).collect(Collectors.toList()), HttpStatus.OK);
    }
}
