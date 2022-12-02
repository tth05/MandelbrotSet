package com.github.tth05.mandelbrotset;

import com.badlogic.gdx.graphics.Pixmap;

import java.util.ArrayList;
import java.util.List;

public class PixmapPool {

    private int count;
    private List<Pixmap> pixmaps;

    public PixmapPool(int count, int pixmapSize) {
        reset(count, pixmapSize);
    }

    public void give(Pixmap pixmap) {
        pixmap.setColor(0);
        pixmap.fill();
        pixmaps.add(pixmap);
    }

    public Pixmap take() {
        if (pixmaps.isEmpty())
            throw new IllegalStateException("No more pixmaps available");
        return pixmaps.remove(pixmaps.size() - 1);
    }

    public void reset(int count, int pixmapSize) {
        this.count = count;

        if (this.pixmaps != null)
            this.pixmaps.forEach(Pixmap::dispose);
        this.pixmaps = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
            pixmaps.add(new Pixmap(pixmapSize, pixmapSize, Pixmap.Format.RGBA8888));
    }

    public int getCapacity() {
        return this.count;
    }
}
