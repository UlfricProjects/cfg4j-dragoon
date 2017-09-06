package com.ulfric.dragoon.cfg4j;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.matcher.ElementMatchers;

import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.GenericTypeInterface;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.context.environment.DefaultEnvironment;
import org.cfg4j.source.context.environment.Environment;
import org.cfg4j.source.context.environment.MissingEnvironmentException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import com.ulfric.dragoon.reflect.Classes;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;

public class ImprovedConfigurationProvider implements ConfigurationProvider { // TODO cleanup class

	private static final Gson GSON = new Gson();

	private final ConfigurationSource configurationSource;
	private final Environment environment = new DefaultEnvironment();
	private final Map<Type, Object> properties = new HashMap<>();

	public ImprovedConfigurationProvider(ConfigurationSource configurationSource) {
		Objects.requireNonNull(configurationSource, "configurationSource");

		this.configurationSource = configurationSource;
	}

	@Override
	public Properties allConfigurationAsProperties() {
		try {
			return configurationSource.getConfiguration(environment);
		} catch (IllegalStateException | MissingEnvironmentException exception) {
			throw new IllegalStateException("Couldn't fetch configuration from configuration source", exception);
		}
	}

	@Override
	public <T> T bind(String prefix, Class<T> type) {
		try {
			return Classes.extend(type)
				.method(isConfigMethod())
				.intercept(MethodDelegation.to(new Cfg4jInterceptor(this, prefix)))
				.make()
				.load(type.getClassLoader())
				.getLoaded()
				.newInstance();
		} catch (InstantiationException | IllegalAccessException exception) {
			throw new IllegalStateException("Failed to bind " + type, exception);
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProperty(String key, GenericTypeInterface type) {
		Type realType = type.getType();
		if (realType instanceof Class) {
			return getProperty(key, (Class<T>) realType);
		}

		return (T) properties.computeIfAbsent(realType, ignore -> {
			Object property = getProperty(key);
			JsonElement json = GSON.toJsonTree(property, realType);
			return GSON.fromJson(json, realType);
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProperty(String key, Class<T> type) {
		Object value = properties.computeIfAbsent(type, ignore -> {
			Class<? extends T> extension = getExtension(type);

			Object property = getProperty(key);
			JsonElement json = GSON.toJsonTree(property);
			return GSON.fromJson(json, extension);
		});

		if (type.isPrimitive()) {
			return (T) value;
		}
		return type.cast(value);
	}

	private <T> Class<? extends T> getExtension(Class<T> type) {
		if (!type.isInterface()) {
			return type;
		}

		DynamicType.Builder<? extends T> extension = Classes.extend(type);
		for (Method method : type.getMethods()) {
			if (!isConfigMethod(method)) {
				continue;
			}

			extension = extension.defineField(method.getName(), method.getReturnType())
					.method(ElementMatchers.is(method))
					.intercept(FieldAccessor.ofField(method.getName()));
		}
		return extension.make().load(type.getClassLoader()).getLoaded();
	}

	private boolean isConfigMethod(Method method) {
		return method.getDeclaringClass() != Object.class && Modifier.isAbstract(method.getModifiers());
	}

	private Junction<MethodDescription> isConfigMethod() {
		return ElementMatchers.isPublic().and(ElementMatchers.isAbstract()).and(ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class)));
	}

	private Object getProperty(String key) {
		try {
			Object property = allConfigurationAsProperties().get(key);

			if (property == null) {
				throw new NoSuchElementException("No configuration with key: " + key);
			}

			return property;
		} catch (IllegalStateException exception) {
			throw new IllegalStateException("Couldn't fetch configuration from configuration source for key: " + key,
			        exception);
		}
	}

	public void reload() {
		properties.clear();
		configurationSource.reload();
	}

}
