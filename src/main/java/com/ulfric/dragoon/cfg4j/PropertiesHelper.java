package com.ulfric.dragoon.cfg4j;

import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertiesHelper {

	private static final Pattern ENVIRONMENT_VARIABLE = Pattern.compile("[$][{]([A-Z_]+)[}]");
	private static final Pattern DOT = Pattern.compile(".", Pattern.LITERAL);

	@SuppressWarnings("unchecked")
	public static void replacePlaceholdersWithEnvironmentVariables(Map<Object, Object> properties) {
		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			Object objectValue = entry.getValue();

			if (objectValue instanceof String) {
				String value = (String) objectValue;
				Matcher matcher = ENVIRONMENT_VARIABLE.matcher(value);
				if (matcher.matches()) {
					String variable = System.getenv(matcher.group(1));
					if (variable != null) {
						entry.setValue(variable);
					}
				}
			} else if (objectValue instanceof Map) {
				replacePlaceholdersWithEnvironmentVariables((Map<Object, Object>) objectValue);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static Properties unflattened(Properties properties) {
		Properties unflattened = new Properties();

		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			Object keyObject = entry.getKey();
			if (!(keyObject instanceof String)) {
				unflattened.put(keyObject, entry.getValue());
				continue;
			}

			Map<Object, Object> layer = unflattened;
			String[] parts = DOT.split((String) keyObject);
			for (int x = 0; x < parts.length - 1; x++) {
				String part = parts[x];
				layer = (Map<Object, Object>) layer.compute(part, (key, subpropertiesObject) -> {
					if (subpropertiesObject instanceof Map) {
						return subpropertiesObject;
					}

					if (subpropertiesObject == null) {
						return new Properties();
					}

					throw new IllegalArgumentException("Invalid property, must be map: " + key + " (" + part + ')');
				});
			}
			layer.put(parts[parts.length - 1], entry.getValue());
		}

		return unflattened;
	}

	private PropertiesHelper() {
	}

}
