package com.example.place.server.auth;

import reactor.core.publisher.Mono;

/**
 * @author Simon Baslé
 */
public class OffbandTokenSenderService implements ITokenSenderService {

	private final SignupProperties conf;

	public OffbandTokenSenderService(SignupProperties conf) {
		this.conf = conf;
	}

	@Override
	public Mono<String> send(String userEmail, String aToken) {
		return Mono.fromRunnable(() -> System.out.println(
				String.format("Sign up of %s -> %s (%s/signup/%s?uid=%s)", userEmail, aToken,
						conf.getSiteUrl(), aToken, userEmail)))
		           .thenReturn("ok");
	}
}
