package com.example.place.server.rate;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Simon Baslé
 */
@Configuration
@ConfigurationProperties(prefix = "rate-limiting")
public class RateLimitingProperties {

	private Duration delay;

	public Duration getDelay() {
		return delay;
	}

	public void setDelay(Duration delay) {
		this.delay = delay;
	}
}
