package com.example.place.server.auth;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import reactor.core.publisher.Mono;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;

/**
 * @author Simon Baslé
 */
@Service
public class TokenService implements ReactiveUserDetailsService {

	private static final int TOKEN_BYTE_SIZE = 16;

	private final Map<Object, Object> unvalidatedTokens = Caffeine.newBuilder()
	                                                              .expireAfterWrite(Duration.ofMinutes(5))
	                                                              .build()
	                                                              .asMap();

	private final Cache<String, UserDetails> userDetails = Caffeine.newBuilder()
	                                                               .expireAfterWrite(Duration.ofMinutes(30))
	                                                               .build();

	private final SecureRandom random = new SecureRandom();

	private final SignupProperties signupProperties;

	public TokenService(SignupProperties properties) {
		signupProperties = properties;
	}

	public String createToken(String userEmail) {
		Objects.requireNonNull(userEmail,"user email can't be null");
		byte bytes[] = new byte[TOKEN_BYTE_SIZE];
		random.nextBytes(bytes);
		String token = String.valueOf(Hex.encode(bytes));
		unvalidatedTokens.put(userEmail, token);
		return token;
	}

	private String get(String userEmail) {
		Objects.requireNonNull(userEmail,"user email can't be null");
		Object token = unvalidatedTokens.remove(userEmail);
		if (token == null) return null;
		return String.valueOf(token);
	}

	public boolean authenticate(String uid, String token) {
		if (uid == null || token == null) return false;
		String expectedToken = get(uid);
		if (expectedToken != null) {
			if (!expectedToken.equals(token)) return false;

			User.UserBuilder builder = User.withDefaultPasswordEncoder()
			                               .username(uid)
			                               .password(token);
			if (signupProperties.getAdmin() != null && signupProperties.getAdmin().equals(uid)) {
				builder = builder.roles("USER", "ADMIN");
			} else {
				builder = builder.roles("USER");
			}

			userDetails.put(uid, builder.build());
			return true;
		} else {
			UserDetails ud = userDetails.getIfPresent(uid);
			if (ud == null) return false;
			return PasswordEncoderFactories.createDelegatingPasswordEncoder().matches(token, ud.getPassword());
		}
	}

	@Override
	public Mono<UserDetails> findByUsername(String username) {
		UserDetails original = userDetails.getIfPresent(username);
		if (original == null) {
			return Mono.empty();
		}
		return Mono.just(User.withUserDetails(original).build());
	}
}
