package com.esw.linesplit.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.esw.linesplit.LineSplit;

import java.awt.Dimension;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		//config.width  = 360; // LAPTOP
		//config.height = 640; // LAPTOP
		config.width  = 720;  // DESKTOP
		config.height = 1280; // DESKTOP
		config.samples = 16;
		new LwjglApplication(new LineSplit(config.width, config.height), config);
	}
}
