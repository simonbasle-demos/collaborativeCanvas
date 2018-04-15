package com.example.place.server.rate;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

/**
 * @author Simon Baslé
 */
@Service
public class RateLimitingService {

	private final RateLimitingProperties conf;

	public RateLimitingService(RateLimitingProperties conf) {
		this.conf = conf;
	}

	public void setDelay(Duration newDelay) {
		this.conf.setDelay(newDelay);
	}

	public Duration getDelay() {
		return conf.getDelay();
	}

	public boolean checkRate(LocalDateTime lastUpdate) {
		return lastUpdate.plus(getDelay()).isBefore(LocalDateTime.now());
	}
}
