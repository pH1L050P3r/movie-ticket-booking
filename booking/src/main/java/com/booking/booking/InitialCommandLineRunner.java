package com.booking.booking;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.booking.booking.models.Show;
import com.booking.booking.models.Theatre;
import com.booking.booking.repositories.ShowRepositories;
import com.booking.booking.repositories.TheatreRepositories;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

@Component
public class InitialCommandLineRunner implements CommandLineRunner {

    @Autowired
	private TheatreRepositories theatreRepositories;
    @Autowired
    private ShowRepositories showRepositories;

    @Override
    public void run(String... args) throws Exception {
        String csvFilePathTheatres = "theatres.csv";
        String csvFilePathShows ="shows.csv";
        try (
            CSVReader csvReaderTheater = new CSVReaderBuilder(new FileReader(csvFilePathTheatres)).withSkipLines(1).build();
            CSVReader csvReaderShow = new CSVReaderBuilder(new FileReader(csvFilePathShows)).withSkipLines(1).build()
        ) {
            List<String[]> theatreRecords = csvReaderTheater.readAll();
			List<Theatre> theatres = new ArrayList<>();
            for (String[] record : theatreRecords) {
				Long id = Long.valueOf(record[0]);
                String name = record[1];
                String location = record[2];
                if (!theatreRepositories.existsById(id)) {
					theatres.add(new Theatre(id, name, location));
                }
            }
			theatreRepositories.saveAll(theatres);

            List<String[]> showRecords = csvReaderShow.readAll();
			List<Show> shows = new ArrayList<>();
            for (String[] record : showRecords) {
				Long id = Long.valueOf(record[0]);
                Long theatreId = Long.valueOf(record[1]);
                String title = record[2];
                Long price = Long.valueOf(record[3]);
                Long seatsAvailable = Long.valueOf(record[4]);
                if (!showRepositories.existsById(id)) {
					shows.add(new Show(id, theatreId, title, price, seatsAvailable));
                }
            }
			showRepositories.saveAll(shows);

        } catch (Exception e) {
            System.err.println("Error loading data from CSV: " + e.getMessage());
        }
    }
}
