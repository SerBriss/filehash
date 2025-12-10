package com.serbriss.filehash.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileHashConfig {

    @Value("${app.data.location}")
    private String location;

    public String getLocation() {
        return location;
    }
}
