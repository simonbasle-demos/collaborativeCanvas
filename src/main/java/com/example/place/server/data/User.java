package com.example.place.server.data;

import org.springframework.data.annotation.Id;

/**
 * @author Simon Baslé
 */
public class User {

	@Id
	public final String email;
	public long         lastUpdate;

	public User(String email, long lastUpdate) {
		this.email = email;
		this.lastUpdate = lastUpdate;
	}
}
