package com.github.tth05.mandelbrotset.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.github.tth05.mandelbrotset.Main;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = Main.WIDTH;
		config.height = Main.HEIGHT;
		config.resizable = false;
		config.title = "MandelbrotSet";
		config.vSyncEnabled = true;
		new LwjglApplication(new Main(), config);
	}
}
