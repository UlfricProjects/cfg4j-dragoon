package com.ulfric.dragoon.cfg4j;

import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.context.environment.Environment;
import org.cfg4j.source.context.environment.MissingEnvironmentException;
import org.cfg4j.source.context.filesprovider.ConfigFilesProvider;
import org.cfg4j.source.context.propertiesprovider.PropertiesProvider;
import org.cfg4j.source.context.propertiesprovider.PropertiesProviderSelector;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class SpecificClassPathConfigurationSource implements ConfigurationSource {

	private final ClassLoader classLoader;
	private final ConfigFilesProvider configFilesProvider;
	private final PropertiesProviderSelector selector;

	public SpecificClassPathConfigurationSource(ClassLoader classLoader, ConfigFilesProvider configFilesProvider,
	        PropertiesProviderSelector selector) {
		Objects.requireNonNull(classLoader, "classLoader");
		Objects.requireNonNull(configFilesProvider, "configFilesProvider");
		Objects.requireNonNull(selector, "selector");

		this.classLoader = classLoader;
		this.configFilesProvider = configFilesProvider;
		this.selector = selector;
	}

	@Override
	public Properties getConfiguration(Environment environment) {
		Properties properties = new Properties();

		Path pathPrefix = Paths.get(environment.getName());

		URL url = classLoader.getResource(pathPrefix.toString());
		if (url == null && !environment.getName().isEmpty()) {
			throw new MissingEnvironmentException("Directory doesn't exist: " + environment.getName());
		}

		List<Path> paths = new ArrayList<>();
		for (Path path : configFilesProvider.getConfigFiles()) {
			paths.add(pathPrefix.resolve(path));
		}

		for (Path path : paths) {
			try (InputStream input = classLoader.getResourceAsStream(path.toString())) {

				if (input == null) {
					throw new IllegalStateException("Unable to load properties from classpath: " + path);
				}

				PropertiesProvider provider = selector.getProvider(path.getFileName().toString());
				properties.putAll(provider.getProperties(input));

			} catch (IOException exception) {
				throw new UncheckedIOException("Unable to load properties from classpath: " + path, exception);
			}
		}

		return properties;
	}

	@Override
	public void reload() {
	}

	@Override
	public void init() {
	}

}
