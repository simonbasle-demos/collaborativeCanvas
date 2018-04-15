package com.example.place.server.canvas;

import com.example.place.server.data.Pixel;
import com.example.place.server.rate.RateLimitingService;
import com.example.place.server.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Simon Baslé
 */
@RestController
public class CanvasController {

	private static final Logger LOG = LoggerFactory.getLogger(CanvasController.class);

	private final CanvasService       canvasService;

	@Autowired //yeah don't really need that anymore
	public CanvasController(CanvasService canvasService) {
		this.canvasService = canvasService;
	}

	@GetMapping(value = "/canvas/full", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<Pixel[][]> canvasFull() {
		return canvasService.getFullCanvas();
	}

}
