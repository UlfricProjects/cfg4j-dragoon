package com.ulfric.dragoon.cfg4j;

import com.ulfric.dragoon.Factory;
import com.ulfric.dragoon.extension.Extension;
import com.ulfric.dragoon.extension.inject.Inject;
import com.ulfric.dragoon.reflect.FieldProfile;
import com.ulfric.dragoon.reflect.LazyFieldProfile;

public class SettingsExtension extends Extension {

	public static final String DEFAULT_FILE_EXTENSION = "yml";

	private final LazyFieldProfile fields = new LazyFieldProfile(this::createFieldProfile);

	@Inject
	private Factory parent;

	private boolean loading;

	private FieldProfile createFieldProfile() {
		loading = true;
		FieldProfile field = FieldProfile.builder()
				.setFactory(parent.request(Cfg4jFactory.class))
				.setSendFieldToFactory(true)
				.setFlagToSearchFor(Settings.class)
				.build();
		loading = false;
		return field;
	}

	@Override
	public <T> T transform(T value) {
		if (!loading) {
			fields.accept(value);
		}
		return value;
	}

}
