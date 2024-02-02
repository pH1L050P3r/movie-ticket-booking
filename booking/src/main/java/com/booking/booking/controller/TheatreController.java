package com.booking.booking.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.booking.booking.models.Theatre;
import com.booking.booking.repositories.TheatreRepositories;

@RestController
public class TheatreController {
    @Autowired
    private TheatreRepositories threadRepositories;

    @GetMapping("/theatres")
    public ResponseEntity<List<Theatre>> getAllTheatres(){
        return new ResponseEntity<>(threadRepositories.findAll(), HttpStatus.OK);
    }
}
