package com.ulfric.dragoon.cfg4j;

import org.cfg4j.source.context.filesprovider.ConfigFilesProvider;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;

public final class SingleConfigFilesProvider implements ConfigFilesProvider {

	private final Iterable<Path> path;

	public SingleConfigFilesProvider(Path path) {
		Objects.requireNonNull(path, "path");

		this.path = Collections.singleton(path);
	}

	@Override
	public Iterable<Path> getConfigFiles() {
		return path;
	}

}