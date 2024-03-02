package com.booking.booking;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfiguration {

    @Autowired
    private Environment env;

    @Bean
    @Profile("production")
    public DataSource productionDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(env.getProperty("spring.datasource.production.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.production.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.production.password"));
        return dataSource;
    }

    @Bean
    @Profile("test")
    public DataSource testDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(env.getProperty("spring.datasource.test.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.test.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.test.password"));
        return dataSource;
    }
}
