package com.example.place;

import com.example.place.server.auth.EmailTokenSenderService;
import com.example.place.server.auth.ITokenSenderService;
import com.example.place.server.auth.OffbandTokenSenderService;
import com.example.place.server.auth.SignupProperties;
import com.example.place.server.canvas.CanvasService;
import com.example.place.server.email.EmailProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PlaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlaceApplication.class, args);
	}

	@Bean
	@ConditionalOnProperty(value = "signup.method", havingValue = "offband")
	protected ITokenSenderService offbandTokenSenderService(SignupProperties signupConf) {
		return new OffbandTokenSenderService(signupConf);
	}

	@Bean
	@ConditionalOnProperty(value = "signup.method", havingValue = "email")
	protected ITokenSenderService emailTokenSenderService(SignupProperties signupConf,
			EmailProperties emailConf) {
		return new EmailTokenSenderService(emailConf, signupConf);
	}

	@Bean
	protected CommandLineRunner initDB(@Autowired CanvasService canvasService) {
		return args -> canvasService.loadData().blockLast();
	}

}
