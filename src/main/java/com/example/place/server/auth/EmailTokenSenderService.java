package com.example.place.server.auth;

import com.example.place.server.email.EmailProperties;
import reactor.core.publisher.Mono;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Simon Baslé
 */
public class EmailTokenSenderService implements ITokenSenderService {

	private final EmailProperties  emailConf;
	private final SignupProperties signupConf;
	private final WebClient        mailSender;

	public EmailTokenSenderService(EmailProperties emailConf, SignupProperties signupConf) {
		this.emailConf = emailConf;
		this.signupConf = signupConf;
		this.mailSender = WebClient.builder()
		                           .baseUrl(emailConf.getApiUrl())
		                           .filter(ExchangeFilterFunctions.basicAuthentication("api", emailConf.getApiKey()))
		                           .build();
	}

	@Override
	public Mono<String> send(String userEmail, String aToken) {
		String html = String.format("<h2>Hello %s, you are almost ready to paint!</h2>" +
						"<h3>Your Paint Token is <b>%s</b>.</h3>" +
						"<p>Copy it and click <a href='%s/signup/%s?uid=%s'>here</a> to verify your email, " +
						"then login using your email and token.</p>",
				userEmail,
				aToken,
				signupConf.getSiteUrl(), aToken, userEmail);

		String plainText = String.format("Hello %s, you are almost ready to paint!\n" +
						"Your Paint Token is %s.\n" +
						"Copy it and go to %s/signup/%s?uid=%s to verify your email, " +
						"then login using your email and token.",
				userEmail,
				aToken,
				signupConf.getSiteUrl(), aToken, userEmail);

		return mailSender.post()
		                 .uri("/messages")
		                 .contentType(MediaType.APPLICATION_FORM_URLENCODED)
		                 .accept(MediaType.APPLICATION_JSON )
		                 .body(BodyInserters.fromFormData("from", emailConf.getFrom())
		                                    .with("to", userEmail)
		                                    .with("subject", "Your Collaborative Canvas demo Paint Token")
		                                    .with("text", plainText)
		                                    .with("html", html)
		                 )
		                 .retrieve()
		                 .bodyToMono(String.class);
	}

}
