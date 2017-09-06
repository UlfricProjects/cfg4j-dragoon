package com.ulfric.dragoon.cfg4j;

import com.ulfric.dragoon.application.Container;

public class Cfg4jContainer extends Container {

	public Cfg4jContainer() {
		install(SettingsExtension.class);
	}

}