package com.github.tth05.mandelbrotset;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private MandelbrotSetRenderer renderer;

    private double speed = 0.0001;

    private SettingsScreen settingsScreen;

    @Override
    public void create() {
        this.renderer = new MandelbrotSetRenderer();
        this.batch = new SpriteBatch();
        this.settingsScreen = new SettingsScreen();
    }

    @Override
    public void render() {
        handleInput();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (this.settingsScreen.isVisible()) {
            this.settingsScreen.render(Gdx.graphics.getDeltaTime());
        } else {
            this.renderer.update();
            this.batch.begin();
            this.renderer.getImage().draw(this.batch);
            this.batch.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        this.batch = new SpriteBatch();
    }

    @Override
    public void dispose() {
        this.batch.dispose();
        this.renderer.dispose();
        System.exit(0);
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            if (this.settingsScreen.isVisible()) {
                this.settingsScreen.hide();
            } else {
                this.settingsScreen.show();
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && this.settingsScreen.isVisible()) {
            this.settingsScreen.hide();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            this.renderer.reset();
            this.speed = 0.0001d;
            return;
        }

        double mul = 1;
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            mul *= 10;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            mul *= 100;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            this.renderer.moveLeft(speed * mul);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            this.renderer.moveUp(speed * mul);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            this.renderer.moveRight(speed * mul);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            this.renderer.moveDown(speed * mul);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q) && this.renderer.zoomIn(speed * mul)) {
            this.speed /= 10;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            this.renderer.zoomOut(speed * mul);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            this.renderer.toggleColorMode();
        }
    }
}
