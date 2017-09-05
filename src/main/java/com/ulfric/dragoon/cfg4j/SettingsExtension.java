package com.ulfric.dragoon.cfg4j;

import com.ulfric.dragoon.Factory;
import com.ulfric.dragoon.extension.Extension;
import com.ulfric.dragoon.reflect.FieldProfile;

import java.util.Objects;

public class SettingsExtension extends Extension {

	public static final String DEFAULT_FILE_EXTENSION = "yml";

	private final FieldProfile fields;

	public SettingsExtension(Factory parent) {
		Objects.requireNonNull(parent, "parent");

		this.fields = FieldProfile.builder()
				.setFactory(parent.request(Cfg4jFactory.class))
				.setSendFieldToFactory(true)
				.setFlagToSearchFor(Settings.class)
				.build();
	}

	@Override
	public <T> T transform(T value) {
		fields.accept(value);
		return value;
	}

}
