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
		user.lastUpdate = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
		return canvasService.setPixelAt(x, y, color)
		                    .then(userRepository.save(user))
		                    .then();
	}
}
