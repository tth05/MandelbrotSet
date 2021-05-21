package com.github.tth05.mandelbrotset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import java.util.function.IntConsumer;

public class SettingsScreen extends ScreenAdapter {

    private Stage stage = new Stage();

    private boolean visible;

    @Override
    public void show() {
        this.visible = true;
        Gdx.input.setInputProcessor(stage);

        initUI();
    }

    @Override
    public void hide() {
        this.visible = false;
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        if (isVisible()) {
            stage.act(delta);
            stage.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.dispose();
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        initUI();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    private void initUI() {
        stage.clear();

        Table table = new Table();
        Skin skin = new Skin(Gdx.files.local("/uiskin.json"));
        table.setSkin(skin);
        table.setFillParent(true);

        Label iterationsLabel = new Label("Iterations: ", skin);
        TextField iterationsTextField = new TextField(Settings.iterations + "", skin);
        iterationsTextField.addListener(event -> {
            if (!(event instanceof ChangeListener.ChangeEvent))
                return false;
            try {
                int iterations = Integer.parseInt(iterationsTextField.getText());
                if (iterations >= 1)
                    Settings.iterations = iterations;
            } catch (Exception ignored) {
            }
            return false;
        });
        table.add(iterationsLabel, iterationsTextField).row();

        IntConsumer applyWindowSize = (newSize) -> {
            if (newSize < 500)
                return;

            int change = newSize - Gdx.graphics.getWidth();

            Lwjgl3Window window = ((Lwjgl3Graphics) Gdx.graphics).getWindow();
            int x = window.getPositionX();
            int y = window.getPositionY();

            Settings.width = newSize;
            Settings.height = newSize;
            Gdx.graphics.setWindowedMode(Settings.width, Settings.height);
            this.resize(Settings.width, Settings.height);
            window.setPosition(x - (change / 2), y - (change / 2));
        };

        Label sizeLabel = new Label("Window size: ", skin);
        TextField sizeTextField = new TextField(Settings.width + "", skin);
        sizeTextField.addListener(event -> {
            if (!(event instanceof InputEvent))
                return false;

            InputEvent inputEvent = (InputEvent) event;
            if (inputEvent.getType() != InputEvent.Type.keyUp || inputEvent.getKeyCode() != Input.Keys.ENTER)
                return false;

            try {
                int newSize = Integer.parseInt(sizeTextField.getText());
                applyWindowSize.accept(newSize);
            } catch (Exception ignored) {
            }
            return false;
        });
        Button sizeApplyButton = new Button(new Label("Apply", skin), skin);
        sizeApplyButton.addListener(event -> {
            if (!(event instanceof ChangeListener.ChangeEvent))
                return false;
            try {
                int newSize = Integer.parseInt(sizeTextField.getText());
                applyWindowSize.accept(newSize);
            } catch (Exception ignored) {
            }
            return false;
        });

        table.add(sizeLabel, sizeTextField, sizeApplyButton).row();

        Label threadsLabel = new Label("Number of threads (" + Settings.numThreads + "): ", skin);
        Slider threadsSlider = new Slider(1, 40, 1, false, skin);
        threadsSlider.setValue((float) Math.sqrt(Settings.numThreads));
        threadsSlider.addListener(event -> {
            if (!(event instanceof ChangeListener.ChangeEvent))
                return false;

            Settings.numThreads = (int) Math.pow(threadsSlider.getValue(), 2);
            threadsLabel.setText("Number of threads (" + Settings.numThreads + "): ");
            return false;
        });

        table.add(threadsLabel, threadsSlider).row();


        Label continuousRenderingLabel = new Label("Continuous rendering: ", skin);
        CheckBox continuousRenderingCheckBox = new CheckBox(null, skin);
        continuousRenderingCheckBox.setChecked(Gdx.graphics.isContinuousRendering());
        continuousRenderingCheckBox.addListener(event -> {
            if (!(event instanceof ChangeListener.ChangeEvent))
                return false;
            Gdx.graphics.setContinuousRendering(continuousRenderingCheckBox.isChecked());
            return false;
        });
        table.add(continuousRenderingLabel, continuousRenderingCheckBox).row();

        stage.addActor(table);
    }

    public boolean isVisible() {
        return visible;
    }
}
