package com.example.place.server.data;

import java.awt.Point;

import org.springframework.data.annotation.Id;

/**
 * @author Simon Baslé
 */
public class Pixel implements FeedMessage {

	@Id
	private Point coordinate;
	private Color color;

	protected Pixel() {
	}

	public Pixel(int x, int y, Color color) {
		this.coordinate = new Point(x, y);
		this.color = color;
	}

	public Pixel(Point coordinate, Color color) {
		this.coordinate = coordinate;
		this.color = color;
	}

	public int getX() {
		return coordinate.x;
	}

	public int getY() {
		return coordinate.y;
	}

	public Color getColor() {
		return color;
	}

	protected void setCoordinate(Point coordinate) {
		this.coordinate = coordinate;
	}

	protected void setColor(Color color) {
		this.color = color;
	}

	@Override
	public String toString() {
		return "[" + coordinate.x + "," + coordinate.y + "]=" + color;
	}
}
