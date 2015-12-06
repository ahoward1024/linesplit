package com.esw.linesplit.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.esw.linesplit.LineSplit;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 504;
		config.height = 896;
		config.samples = 16;
		new LwjglApplication(new LineSplit(config.width, config.height), config);
	}
}
