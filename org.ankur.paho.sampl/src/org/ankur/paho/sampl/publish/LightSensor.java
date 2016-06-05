package org.ankur.paho.sampl.publish;

import java.util.Vector;

import mraa.Aio;
import mraa.Platform;
import mraa.mraa;

public class LightSensor {

	static {
		System.out.println("Attempting to load mraa from "
				+ System.getProperty("java.library.path"));
		System.loadLibrary("mraajava");
	}

	public static int readLDR() {

		// check that we are running on Galileo or Edison
		Platform platform = mraa.getPlatformType();
		if (platform != Platform.INTEL_GALILEO_GEN1
				&& platform != Platform.INTEL_GALILEO_GEN2
				&& platform != Platform.INTEL_EDISON_FAB_C) {
			System.err.println("Unsupported platform, exiting");
			return 0;
		}

		Aio ldrPin = new Aio(3);

		return ldrPin.read();

	}

}