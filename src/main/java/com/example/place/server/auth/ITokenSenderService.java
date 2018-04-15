package com.example.place.server.auth;

import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;

/**
 * @author Simon Baslé
 */
@Service
public interface ITokenSenderService {

	Mono<String> send(String userEmail, String aToken);

}
