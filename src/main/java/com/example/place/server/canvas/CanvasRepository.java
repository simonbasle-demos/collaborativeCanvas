package com.example.place.server.canvas;

import java.awt.Point;

import com.example.place.server.data.Pixel;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * @author Simon Baslé
 */
public interface CanvasRepository extends ReactiveMongoRepository<Pixel, Point> {

}
