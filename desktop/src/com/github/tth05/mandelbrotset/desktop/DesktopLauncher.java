package com.github.tth05.mandelbrotset.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.tth05.mandelbrotset.Main;
import com.github.tth05.mandelbrotset.Settings;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(Settings.width, Settings.height);
		config.setResizable(false);
		config.useVsync(true);
		config.setTitle("MandelbrotSet");
		new Lwjgl3Application(new Main(), config);
	}
}
