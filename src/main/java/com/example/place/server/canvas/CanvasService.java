package com.example.place.server.canvas;

import com.example.place.server.data.Color;
import com.example.place.server.data.FeedMessage;
import com.example.place.server.data.GridUpdate;
import com.example.place.server.data.Pixel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.TopicProcessor;

import org.springframework.stereotype.Service;

/**
 * @author Simon Baslé
 */
@Service
public class CanvasService {

	public static final int ROWS = 500;
	public static final int COLUMNS = 500;
	private final Flux<FeedMessage> updates;
	private final FluxSink<FeedMessage> updateSink;

	private Pixel[][] canvas;
	private final CanvasRepository repository;

	//TODO protect against coordinate overflow

	public CanvasService(CanvasRepository repository) {
		this.repository = repository;
		this.canvas = new Pixel[ROWS][COLUMNS];
		TopicProcessor<FeedMessage> processor = TopicProcessor.create();
		this.updateSink = processor.sink();
		this.updates = processor;

		clearCanvas().blockLast();
	}

	public Flux<FeedMessage> updates() {
		return updates;
	}

	public void resendCanvas() {
		updateSink.next(new GridUpdate(canvas[0].length, canvas.length,
				GridUpdate.UpdateInstruction.RELOAD));
	}

	public Flux<Pixel> clearCanvas() {
		return generateData()
				.doOnNext(pixel -> canvas[pixel.getX()][pixel.getY()] = pixel);
	}

	public Flux<Pixel> loadData() {
		return repository.findAll()
		                 .doOnSubscribe(sub -> System.out.println("Loading Canvas from DB"))
		                 .doOnNext(pixel -> canvas[pixel.getX()][pixel.getY()] = pixel)
		                 .doOnComplete(() -> System.out.println("Canvas Loaded"));
	}

	public Flux<Pixel> loadDataOrGenerate() {
		return repository.count()
		                 .flatMapMany(size  -> size == ROWS * COLUMNS
				                 ? repository.findAll()
				                             .doOnSubscribe(sub -> System.out.println("Loading Canvas from DB"))
				                 : repository.deleteAll()
				                             .thenMany(repository.saveAll(generateData()))
				                             .doOnSubscribe(sub -> System.out.println("Initializing Canvas in DB"))
		                 )
		                 .doOnNext(pixel -> canvas[pixel.getX()][pixel.getY()] = pixel)
		                 .doOnComplete(() -> System.out.println("Canvas Loaded"));
	}

	private static Flux<Pixel> generateData() {
		return Flux.create(sink -> {
			for (int i = 0; i < ROWS; i++) {
				for (int j = 0; j < COLUMNS; j++) {
					sink.next(new Pixel(i, j, Color.WHITE));
				}
			}
			sink.complete();
		});
	}

	public Mono<Pixel[][]> getFullCanvas() {
		return Mono.just(canvas);
	}

	public Pixel getPixelAt(int x, int y) {
		return canvas[x][y];
	}

	public Color getColorAt(int x, int y) {
		return canvas[x][y].getColor();
	}

	public Mono<Void> setPixelAt(int x, int y, Color color) {
		return preparePixelAt(x, y, color)
				.doOnNext(updateSink::next)
				.then();
	}

	public Mono<Pixel> preparePixelAt(int x, int y, Color color) {
		return repository.save(new Pixel(x, y, color))
		                 .doOnNext(pixel -> canvas[x][y] = pixel);
	}

}
