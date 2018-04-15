package com.example.place.server.canvas;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.example.place.server.user.UserRepository;
import com.example.place.server.data.Color;
import com.example.place.server.data.User;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;

/**
 * @author Simon Baslé
 */
@Service
public class PaintService {

	private final UserRepository userRepository;
	private final CanvasService  canvasService;

	public PaintService(UserRepository repository, CanvasService canvasService) {
		userRepository = repository;
		this.canvasService = canvasService;
	}

	public Mono<Void> paint(int x, int y, Color color, User user) {
		//TODO mark the paint to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
		//TODO set the pixel and save the timestamp
		return Mono.empty();
	}
}
