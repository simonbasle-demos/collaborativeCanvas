package com.example.place.server.admin;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

import com.example.place.server.canvas.CanvasRepository;
import com.example.place.server.canvas.CanvasService;
import com.example.place.server.data.Color;
import com.example.place.server.data.Pixel;
import com.example.place.server.rate.RateLimitingService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Simon Baslé
 */
@RestController
public class AdminController {

	private final CanvasRepository pixelRepository;
	private final CanvasService    canvasService;
	private final RateLimitingService rateLimitingService;

	public AdminController(CanvasRepository repository, CanvasService service,
			RateLimitingService limitingService) {
		pixelRepository = repository;
		canvasService = service;
		rateLimitingService = limitingService;
	}

	@GetMapping("/admin/clearDB")
	public Mono<String> clearDB() {
		return Mono.when(pixelRepository.deleteAll(), canvasService.clearCanvas().then())
		           .then(Mono.fromRunnable(canvasService::resendCanvas))
		           .thenReturn("Deleted Canvas from DB");
	}

	@GetMapping("/admin/clear")
	public Mono<String> clear() {
		return canvasService.clearCanvas()
		                    .then(Mono.fromRunnable(canvasService::resendCanvas))
		                    .thenReturn("Cleared Canvas");
	}

	@GetMapping("/admin/rate")
	public String getRate() {
		Duration d = rateLimitingService.getDelay();
		long converted = d.toHours();
		if (converted > 0) return converted + "h";

		converted = d.toMinutes();
		if (converted > 0) return converted + "m";

		if (d.compareTo(Duration.ofSeconds(1)) > 0) return d.getSeconds() + "s";

		return d.toMillis() + "ms";
	}

	@PutMapping(value = "/admin/rate", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity setRate(@RequestBody Map<String, Object> json) {
		Object d = json.get("delay");

		if (d instanceof Integer) {
			Integer delay = (Integer) d;
			rateLimitingService.setDelay(Duration.ofMillis(delay));
			return ResponseEntity.ok(delay + "ms");
		}
		return ResponseEntity.badRequest().build();
	}

	private static final Pattern SET_DELAY_PATTERN = Pattern.compile("([0-9]+)(s|m|h|ms)");

	@Nullable
	public static Duration parseDelay(String delay) {
		Matcher matcher = SET_DELAY_PATTERN.matcher(delay);
		if (matcher.matches()) {
			Integer delayPart = Integer.parseInt(matcher.group(1));
			String type = matcher.group(2);

			if (delayPart == 0) {
				return Duration.ZERO;
			}

			switch (type) {
				case "s":
					return Duration.ofSeconds(delayPart);
				case "ms":
					return Duration.ofMillis(delayPart);
				case "h":
					return Duration.ofHours(delayPart);
				case "m":
					return Duration.ofMinutes(delayPart);
				default:
					return Duration.ZERO;
			}
		}
		return null;
	}

	@GetMapping(value = "/admin/rate/{delay}")
	public ResponseEntity setRateConvenience(@PathVariable String delay) {
		Duration newDelay = parseDelay(delay);
		if (newDelay != null) {
			rateLimitingService.setDelay(newDelay);
			return ResponseEntity.ok("set " + delay + ", interpreted as " + newDelay.toMillis() + "ms");
		}
		return ResponseEntity.badRequest().body("incorrect delay format " + delay);
	}

	@GetMapping("/admin/paint")
	public Mono<Void> paint(@RequestParam int x,
			@RequestParam int y,
			@RequestParam int r,
			@RequestParam int g,
			@RequestParam int b,
			@RequestParam(required = false, defaultValue = "255") int a) {
		return canvasService.setPixelAt(x, y, new Color(r, g, b, a));
	}

	@GetMapping("/admin/paintSpring")
	public Mono<String> paintSpringLogo() {
		final BufferedImage springLogo;
		try {
			springLogo = ImageIO.read(this.getClass().getClassLoader()
					.getResourceAsStream("images/spring.png"));
		}
		catch (IOException e) {
			return Mono.error(e);
		}

		final int width = springLogo.getWidth();
		final int height = springLogo.getHeight();

		return Flux.<Pixel>create(sink -> {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int pixel = springLogo.getRGB(x, y);
					int alpha = (pixel >> 24) & 0xff;
					int red = (pixel >> 16) & 0xff;
					int green = (pixel >> 8) & 0xff;
					int blue = (pixel) & 0xff;

					sink.next(new Pixel(x, y,
							alpha == 0 ? Color.WHITE :
									new Color(red, green, blue, alpha)));
				}
			}
			sink.complete();
		})
		           .flatMap(pixel -> canvasService.preparePixelAt(
				           pixel.getX(),
				           pixel.getY(),
				           pixel.getColor()))
		           .then(Mono.fromRunnable(canvasService::resendCanvas))
		           .then(Mono.just("painted"));
	}

}
