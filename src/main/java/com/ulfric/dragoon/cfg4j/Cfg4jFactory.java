package com.ulfric.dragoon.cfg4j;

import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.compose.FallbackConfigurationSource;
import org.cfg4j.source.files.FilesConfigurationSource;
import org.cfg4j.source.reload.ReloadStrategy;

import com.ulfric.dragoon.Factory;
import com.ulfric.dragoon.ObjectFactory;
import com.ulfric.dragoon.application.Container;
import com.ulfric.dragoon.extension.inject.Inject;
import com.ulfric.dragoon.stereotype.Stereotypes;

import java.lang.reflect.Field;
import java.nio.file.FileSystem;
import java.nio.file.Path;

public class Cfg4jFactory implements Factory {

	@Inject
	private ObjectFactory creator;

	@Inject
	private FileSystem fileSystem;

	@Override
	public <T> T request(Class<T> type) {
		throw noParameters();
	}

	@Override
	public <T> T request(Class<T> type, Object... parameters) {
		if (parameters.length != 2) {
			throw noParameters();
		}

		Path folder = folder(parameters[0]);
		Field field = (Field) parameters[1];

		Settings settings = Stereotypes.getFirst(field, Settings.class);

		Path file = folder.resolve(getFileName(settings, field));
		Path absoluteFile = file.toAbsolutePath();

		ConfigurationSource source = new FilesConfigurationSource(new SingleConfigFilesProvider(absoluteFile));
		ConfigurationSource defaults =
				new SpecificClassPathConfigurationSource(type.getClassLoader(), new SingleConfigFilesProvider(file));
		ConfigurationProvider provider = new ConfigurationProviderBuilder()
				.withConfigurationSource(new FallbackConfigurationSource(source, defaults))
				.withReloadStrategy(reload(settings))
				.build();

		return provider.bind("", type);
	}

	private ReloadStrategy reload(Settings settings) {
		Reload reload = settings.reload();

		return creator.request(ImprovedPeriodicalReloadStrategy.class, reload.period(), reload.unit());
	}

	private String getFileName(Settings settings, Field field) {
		return name(settings, field) + '.' + extension(settings);
	}

	private String name(Settings settings, Field field) {
		String name = settings.value();
		if (name.isEmpty()) {
			return field.getName();
		}
		return name;
	}

	private String extension(Settings settings) {
		String extension = settings.extension();
		if (extension.isEmpty()) {
			return SettingsExtension.DEFAULT_FILE_EXTENSION;
		}
		return extension;
	}

	private Path folder(Object owning) {
		Path root = defaultFolder();
		if (owning == null) {
			return root;
		}

		Container container = Container.getOwningContainer(owning);
		if (container == null) {
			return root;
		}

		String name = container.getName();
		if (name == null) {
			return root;
		}

		return root.resolve(name.toLowerCase()); // TODO add hyphens
	}

	private Path defaultFolder() {
		return fileSystem.getPath("settings");
	}

	private RuntimeException noParameters() {
		throw new UnsupportedOperationException("parameters array required size 2");
	}

}
