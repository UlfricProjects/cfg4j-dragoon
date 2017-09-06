package com.ulfric.dragoon.cfg4j;

import org.cfg4j.source.context.propertiesprovider.PropertiesProvider;

import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class UnflatteningPropertiesProvider implements PropertiesProvider {

	private final PropertiesProvider parent;

	public UnflatteningPropertiesProvider(PropertiesProvider parent) {
		Objects.requireNonNull(parent, "parent");

		this.parent = parent;
	}

	@Override
	public Properties getProperties(InputStream inputStream) {
		Properties properties = parent.getProperties(inputStream);
		if (properties == null || properties.isEmpty()) {
			return properties;
		}
		properties = PropertiesHelper.unflattened(properties);
		PropertiesHelper.replacePlaceholdersWithEnvironmentVariables(properties);
		return properties;
	}

}
