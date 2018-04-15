package com.example.place.server.data;

/**
 * @author Simon Baslé
 */
public class GridUpdate implements FeedMessage {

	public enum UpdateInstruction {
		CLEAR,
		RELOAD;
	}

	private final int width;
	private final int height;
	private final UpdateInstruction instruction;

	public GridUpdate(int width, int height, UpdateInstruction instruction) {
		this.width = width;
		this.height = height;
		this.instruction = instruction;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public UpdateInstruction getInstruction() {
		return instruction;
	}
}
