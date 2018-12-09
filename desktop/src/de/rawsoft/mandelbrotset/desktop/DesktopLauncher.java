package de.rawsoft.mandelbrotset.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import de.rawsoft.mandelbrotset.Main;

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
