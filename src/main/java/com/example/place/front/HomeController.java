package com.example.place.front;

import java.security.Principal;
import java.util.Map;

import reactor.core.publisher.Mono;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Simon Baslé
 */
@Controller
public class HomeController {

	@GetMapping("/")
	public Mono<String> home(Mono<Principal> principalMono,
			Map<String, Object> model) {
		return principalMono
				.doOnNext(principal -> model.put("user", principal.getName()))
				.thenReturn("canvas");
	}

}
