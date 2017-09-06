package com.ulfric.dragoon.cfg4j;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import org.cfg4j.provider.ConfigurationProvider;

import java.lang.reflect.Method;
import java.util.Objects;

public class Cfg4jInterceptor {

	private final ConfigurationProvider configurationProvider;
	private final String prefix;

	public Cfg4jInterceptor(ConfigurationProvider configurationProvider, String prefix) {
		Objects.requireNonNull(configurationProvider, "configurationProvider");
		Objects.requireNonNull(prefix, "prefix");

		this.configurationProvider = configurationProvider;
		this.prefix = prefix.isEmpty() ? "" : prefix + '.';
	}

	@RuntimeType
	public Object intercept(@Origin Method method) {
		return configurationProvider.getProperty(prefix + method.getName(), method::getGenericReturnType);
	}

}
