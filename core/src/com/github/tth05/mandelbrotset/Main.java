package com.github.tth05.mandelbrotset;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import javafx.util.Pair;

import java.awt.Color;
import java.awt.Point;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends ApplicationAdapter {
	private SpriteBatch batch;
	private Sprite img;
	public static final int WIDTH = 1000;
	public static final int HEIGHT = 1000;
	//Float to prevent integer division
	private final float THREADS = /*(float) Math.pow(Math.round(Math.sqrt(Runtime.getRuntime().availableProcessors())), 2)*/400;
	private final ExecutorService pool = Executors.newFixedThreadPool((int) THREADS);

	@Override
	public void create() {
		img = new Sprite(new Texture(new Pixmap(WIDTH, HEIGHT, Pixmap.Format.RGBA8888)));
		img.setSize(WIDTH, HEIGHT);
		img.setPosition(0, 0);

		long time = System.currentTimeMillis();
		updateMandelbrotSet();
		System.out.println((System.currentTimeMillis() - time) / 1000d);

		batch = new SpriteBatch();

		Gdx.graphics.setContinuousRendering(false);
		Gdx.graphics.requestRendering();
	}

	double i = 0;
	double r = 0;

	@Override
	public void render() {
		handleInput();

		long time = System.currentTimeMillis();
		updateMandelbrotSet();
		i++;
		r += (System.currentTimeMillis() - time) / 1000d;
		System.out.println(r / i);

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		img.draw(batch);
		batch.end();
	}

	@Override
	public void dispose() {
		batch.dispose();
		img.getTexture().dispose();
	}

	private double speed = 0.0001;

	private void handleInput() {
		double mul = 1;
		if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
			mul *= 10;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
			mul *= 100;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			minX += -speed * mul;
			maxX += -speed * mul;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			minY += -speed * mul;
			maxY += -speed * mul;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			minX += speed * mul;
			maxX += speed * mul;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			minY += speed * mul;
			maxY += speed * mul;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
			double tempMinY = minY + speed * mul;
			double tempMinX = minX + speed * mul;
			double tempMaxX = maxX + -speed * mul;
			double tempMaxY = maxY + -speed * mul;
			if (tempMaxX < tempMinX || tempMaxY < tempMinY) {
				speed /= 10;
			} else {
				minY = tempMinY;
				minX = tempMinX;
				maxX = tempMaxX;
				maxY = tempMaxY;
			}
		}
		if (Gdx.input.isKeyPressed(Input.Keys.E)) {
			minY += -speed * mul;
			minX += -speed * mul;
			maxX += speed * mul;
			maxY += speed * mul;
		}
	}

	//Used for mandelbrotset
	private double minY = -1.5;
	private double minX = -2;
	private double maxX = 1;
	private double maxY = 1.5;
	private final int iterations = 5000;

	//List of pixmaps from each thread
	private final List<Pair<Point, Pixmap>> list = new CopyOnWriteArrayList<>();

	//Size of each square for each thread
	private final int size = (int) Math.round(Math.sqrt((WIDTH * HEIGHT) / THREADS));

	public void updateMandelbrotSet() {
		CountDownLatch latch = new CountDownLatch((int) THREADS);

		double divider = Math.sqrt(THREADS);
		int row = 0;
		int col = 0;
		for (int x = 0; x < WIDTH - 10; x += size) {
			for (int y = 0; y < HEIGHT - 10; y += size) {
				drawSubPixmap(
						x,
						y,
						size,
						size,
						minX + col * ((maxX - minX) / divider),
						minY + row * ((maxY - minY) / divider),
						minX + (col + 1) * ((maxX - minX) / divider),
						minY + (row + 1) * ((maxY - minY) / divider),
						list, latch);
				row++;
			}
			row = 0;
			col++;
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		list.forEach((p) -> {
			img.getTexture().draw(p.getValue(), p.getKey().x, p.getKey().y);
			//Comment out to make it even faster
			p.getValue().dispose();
		});
		list.clear();
	}

	public void drawSubPixmap(int startX, int startY, final int width, final int height, final double minX,
							  final double minY, final double maxX, final double maxY,
							  List<Pair<Point, Pixmap>> finished, CountDownLatch latch) {
		pool.execute(() -> {
			Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					double a0 = map(x + startX, startX, startX + width, minX, maxX) * ((maxX - minX) / (maxY - minY));
					double b0 = map(y + startY, startY, startY + height, minY, maxY) * ((maxX - minX) / (maxY - minY));

					double a = 0;
					double b = 0;

					int n = 0;

					while (a * a + b * b < 4 && n < iterations) {
						double tempA = a * a - b * b + a0;
						double tempB = 2 * a * b + b0;
						if (a == tempA && b == tempB) {
							n = iterations;
							break;
						}

						a = tempA;
						b = tempB;
						n++;
					}

//					if (n == iterations) {
//						pixmap.setColor(1f, 1f, 1f, 0f);
//					} else {
//						float brightness = (float) map(n, 0, iterations, 0, 1);
//						pixmap.setColor(1f, 1f, 1f, brightness);
//					}
					float hue = (float) map(n, 0, iterations, 0, 255);
					pixmap.setColor(Color.HSBtoRGB(hue, n == iterations ?
							1f :
							0.8f, 1f));
					pixmap.drawPixel(x, y);
				}
			}
			finished.add(new Pair<>(new Point(startX, startY), pixmap));
			latch.countDown();
		});
	}

	private double map(double n, double start1, double end1, double start2, double end2) {
		return (n - start1) / (end1 - start1) * (end2 - start2) + start2;
	}
}