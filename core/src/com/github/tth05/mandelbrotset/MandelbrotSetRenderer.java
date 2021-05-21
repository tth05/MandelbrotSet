package com.github.tth05.mandelbrotset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import javafx.util.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MandelbrotSetRenderer {

    // MandelbrotSet inputs
    private double minY = -1.5;
    private double minX = -2;
    private double maxX = 1;
    private double maxY = 1.5;
    private boolean colorMode = true;

    private Sprite image;

    public MandelbrotSetRenderer() {
        initSprite();
    }

    private void initSprite() {
        if (image != null)
            image.getTexture().dispose();

        image = new Sprite(new Texture(new Pixmap(Settings.width, Settings.height, Pixmap.Format.RGBA8888)));
        image.setSize(Settings.width, Settings.height);
        image.setPosition(0, 0);
    }

    public void update() {
        if (image.getWidth() != Settings.width)
            initSprite();

        long start = System.nanoTime();

        CountDownLatch latch = new CountDownLatch(Settings.numThreads);
        List<Pair<Point, Pixmap>> list = Collections.synchronizedList(new ArrayList<>());

        int subSquareSize = Settings.getSubSquareSize();

        double divider = Math.sqrt(Settings.numThreads);
        int row = 0;
        int col = 0;
        for (int x = 0; x < Settings.width - 10; x += subSquareSize) {
            for (int y = 0; y < Settings.height - 10; y += subSquareSize) {
                drawSubPixmap(
                        x,
                        y,
                        subSquareSize,
                        subSquareSize,
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
            this.image.getTexture().draw(p.getValue(), p.getKey().x, p.getKey().y);
            //Comment out to make it even faster -> will cause a memory leak though
            p.getValue().dispose();
        });

        System.out.println("Frame took: " + ((System.nanoTime() - start) / 1_000_000_000d) + " seconds");
        System.out.println("Frames per second: " + Gdx.graphics.getFramesPerSecond());
    }

    public void drawSubPixmap(int startX, int startY, final int width, final int height, final double minX,
                              final double minY, final double maxX, final double maxY,
                              List<Pair<Point, Pixmap>> finished, CountDownLatch latch) {
        Settings.getExecutorService().execute(() -> {
            Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    double a0 = map(x + startX, startX, startX + width, minX, maxX) * ((maxX - minX) / (maxY - minY));
                    double b0 = map(y + startY, startY, startY + height, minY, maxY) * ((maxX - minX) / (maxY - minY));

                    double a = 0;
                    double b = 0;

                    int n = 0;

                    while (a * a + b * b < 4 && n < Settings.iterations) {
                        double tempA = a * a - b * b + a0;
                        double tempB = 2 * a * b + b0;
                        if (a == tempA && b == tempB) {
                            n = Settings.iterations;
                            break;
                        }

                        a = tempA;
                        b = tempB;
                        n++;
                    }

                    if (colorMode) {
                        if (n == Settings.iterations) {
                            pixmap.setColor(1f, 1f, 1f, 0f);
                        } else {
                            float brightness = (float) map(n, 0, Settings.iterations, 0, 1);
                            pixmap.setColor(1f, 1f, 1f, brightness);
                        }
                    } else {
                        float hue = (float) map(n, 0, Settings.iterations, 0, 255);
                        pixmap.setColor(Color.HSBtoRGB(hue, n == Settings.iterations ?
                                1f :
                                0.8f, 1f));
                    }

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

    public void dispose() {
        this.image.getTexture().dispose();
    }

    public void toggleColorMode() {
        this.colorMode = !this.colorMode;
    }

    public boolean zoomIn(double delta) {
        double tempMinY = minY + delta;
        double tempMinX = minX + delta;
        double tempMaxX = maxX + -delta;
        double tempMaxY = maxY + -delta;
        if (tempMaxX < tempMinX || tempMaxY < tempMinY) {
            return true;
        } else {
            minY = tempMinY;
            minX = tempMinX;
            maxX = tempMaxX;
            maxY = tempMaxY;
        }

        return false;
    }

    public void zoomOut(double delta) {
        minY += -delta;
        minX += -delta;
        maxX += delta;
        maxY += delta;
    }

    public void moveUp(double delta) {
        minY += -delta;
        maxY += -delta;
    }

    public void moveRight(double delta) {
        minX += delta;
        maxX += delta;
    }

    public void moveDown(double delta) {
        minY += delta;
        maxY += delta;
    }

    public void moveLeft(double delta) {
        minX += -delta;
        maxX += -delta;
    }

    public Sprite getImage() {
        return image;
    }

    public void reset() {
        minY = -1.5;
        minX = -2;
        maxX = 1;
        maxY = 1.5;
    }
}
