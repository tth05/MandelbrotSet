package com.github.tth05.mandelbrotset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

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

    private final PixmapPool pixmapPool = new PixmapPool(Settings.numThreads, Settings.getSubSquareSize());
    ;

    private void initSprite() {
        if (image != null)
            image.getTexture().dispose();

        image = new Sprite(new Texture(new Pixmap(Settings.width, Settings.height, Pixmap.Format.RGBA8888)));
        image.setSize(Settings.width, Settings.height);
        image.setPosition(0, 0);
    }

    public void update() {
        int subSquareSize = Settings.getSubSquareSize();
        if (image == null || image.getWidth() != Settings.width || Settings.numThreads != pixmapPool.getCapacity()) {
            initSprite();
            pixmapPool.reset(Settings.numThreads, subSquareSize);
        }

        long start = System.nanoTime();

        CountDownLatch latch = new CountDownLatch(Settings.numThreads);
        List<Pair<Point, Pixmap>> list = Collections.synchronizedList(new ArrayList<>());

        double divider = Math.sqrt(Settings.numThreads);
        int row = 0;
        int col = 0;
        for (int x = 0; x < Settings.width - 10; x += subSquareSize) {
            for (int y = 0; y < Settings.height - 10; y += subSquareSize) {
                drawSubPixmap(
                        pixmapPool.take(),
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
            this.image.getTexture().draw(p.b(), p.a().x, p.a().y);
            pixmapPool.give(p.b());
        });

        System.out.println("Frame took: " + ((System.nanoTime() - start) / 1_000_000_000d) + " seconds");
        System.out.println("Frames per second: " + Gdx.graphics.getFramesPerSecond());
    }

    public void drawSubPixmap(Pixmap pixmap, int startX, int startY, final int width, final int height, final double minX,
                              final double minY, final double maxX, final double maxY,
                              List<Pair<Point, Pixmap>> finished, CountDownLatch latch) {
        Settings.getExecutorService().execute(() -> {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    double a0 = map(x + startX, startX, startX + width, minX, maxX) * ((maxX - minX) / (maxY - minY));
                    double b0 = map(y + startY, startY, startY + height, minY, maxY) * ((maxX - minX) / (maxY - minY));

                    // Is withing cardiod / bulb
                    {
                        var p = Math.pow(a0 - 0.25, 2) + Math.pow(b0, 2);
                        if (p * (p + a0 - 0.25) < Math.pow(b0, 2) / 4)
                            continue;
                    }

                    double a = 0;
                    double aOld = 0;
                    double b = 0;
                    double bOld = 0;
                    double a2 = 0;
                    double b2 = 0;

                    var n = 0;
                    var maxIterations = Settings.iterations;

                    var period = 0;

                    while (a2 + b2 < 4 && n < maxIterations) {
                        double tempA = a2 - b2 + a0;
                        b = 2 * a * b + b0;
                        a = tempA;

                        if (a == aOld && b == bOld) {
                            n = maxIterations;
                            break;
                        }

                        a2 = a * a;
                        b2 = b * b;

                        // Periodicity check
                        if (period++ > 20) {
                            period = 0;
                            aOld = a;
                            bOld = b;
                        }

                        n++;
                    }

                    if (colorMode) {
                        if (n == maxIterations) {
                            pixmap.setColor(1f, 1f, 1f, 0f);
                        } else {
                            float brightness = (float) map(n, 0, maxIterations, 0, 1);
                            pixmap.setColor(1f, 1f, 1f, brightness);
                        }
                    } else {
                        float hue = (float) map(n, 0, maxIterations, 0, 255);
                        pixmap.setColor(Color.HSBtoRGB(hue, n == maxIterations ?
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

    record Pair<K, V>(K a, V b) {}
}
