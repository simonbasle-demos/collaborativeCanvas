package com.example.place.server.canvas;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.example.place.server.data.FeedMessage;
import com.example.place.server.data.PaintInstruction;
import com.example.place.server.data.Pixel;
import com.example.place.server.rate.RateLimitingException;
import com.example.place.server.rate.RateLimitingService;
import com.example.place.server.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.accepted;

/**
 * @author Simon Baslé
 */
@RestController
public class CanvasController {

	private static final Logger LOG = LoggerFactory.getLogger(CanvasController.class);

	private final UserRepository      userRepository;
	private final RateLimitingService rateLimitingService;
	private final PaintService        paintService;
	private final CanvasService       canvasService;

	@Autowired //yeah don't really need that anymore
	public CanvasController(UserRepository repository, RateLimitingService service,
			PaintService paintService, CanvasService canvasService) {
		this.userRepository = repository;
		this.rateLimitingService = service;
		this.paintService = paintService;
		this.canvasService = canvasService;
	}

	@PostMapping(value = "/paint/",
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.TEXT_PLAIN_VALUE)
	public Mono<ResponseEntity<String>> paintPixel(@RequestBody PaintInstruction paint, Mono<Principal> principalMono) {
		return principalMono
				.map(Principal::getName)
				.flatMap(userRepository::findById)
				.flatMap(u -> {
					boolean ok = rateLimitingService.checkRate(
							LocalDateTime.ofEpochSecond(u.lastUpdate, 0, ZoneOffset.UTC));

					if (ok) return paintService.paint(paint.getX(), paint.getY(), paint.getColor(), u)
					                           .thenReturn(u);
					else
						return Mono.error(new RateLimitingException("you cannot paint yet"));
				})
				.map(u -> accepted().body("paint accepted"))
				.onErrorResume(RateLimitingException.class, t -> Mono.just(ResponseEntity
						.status(HttpStatus.TOO_MANY_REQUESTS)
						.body(t.getMessage())))
				.defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
	}

	@GetMapping(value = "/canvas/feed", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<FeedMessage> canvas() {
		return canvasService.updates();
	}

	@GetMapping(value = "/canvas/full", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<Pixel[][]> canvasFull() {
		return canvasService.getFullCanvas();
	}

}
