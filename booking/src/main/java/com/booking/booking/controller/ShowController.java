package com.booking.booking.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.booking.booking.dto.ShowDTO;
import com.booking.booking.service.IShowService;


@RestController
public class ShowController {
    @Autowired
    private IShowService showService;

    @GetMapping("/shows/{show_id}")
    public ResponseEntity<?> getShowById(@PathVariable("show_id") Long showId){
        try{
            ShowDTO show = showService.getById(showId);   
            return new ResponseEntity<ShowDTO> (show, HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<HttpStatus>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/shows/theatres/{theatre_id}")
    public ResponseEntity<?> getAllShowShowcasedByTheater(@PathVariable("theatre_id") Long theatreId){
        if(!showService.existsByTheatreId(theatreId))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        try{
            List<ShowDTO> shows = showService.getAllByTheaterId(theatreId);
            return new ResponseEntity<List<ShowDTO>>(shows, HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<HttpStatus>(HttpStatus.BAD_REQUEST);
        }
    }
}
