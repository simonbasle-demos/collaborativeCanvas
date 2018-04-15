package com.example.place;

import com.example.place.server.canvas.CanvasService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PlaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlaceApplication.class, args);
	}

	@Bean
	protected CommandLineRunner initDB(@Autowired CanvasService canvasService) {
		return args -> canvasService.loadData().blockLast();
	}

}
