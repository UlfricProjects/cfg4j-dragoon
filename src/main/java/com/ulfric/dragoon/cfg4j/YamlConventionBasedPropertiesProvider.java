package com.ulfric.dragoon.cfg4j;

import org.cfg4j.source.context.propertiesprovider.YamlBasedPropertiesProvider;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YamlConventionBasedPropertiesProvider extends YamlBasedPropertiesProvider {

	private static final Pattern YAML_CONVENTION = Pattern.compile("-([a-zA-Z])");

	@Override
	public Properties getProperties(InputStream inputStream) {
		Properties properties = new Properties();
		Properties superProperties = super.getProperties(inputStream);

		for (Map.Entry<Object, Object> entry : superProperties.entrySet()) {
			Object objectKey = entry.getKey();
			if (!(objectKey instanceof String)) {
				properties.put(objectKey, entry.getValue());
				continue;
			}

			String key = (String) objectKey;
			String convention = getYamlConventionName(key);

			properties.put(convention, entry.getValue());
		}

		return properties;
	}

	private String getYamlConventionName(String name) {
		StringBuilder string = new StringBuilder();
		Matcher matcher = YAML_CONVENTION.matcher(name);

		int position = 0;
		while (matcher.find()) {
			string.append(name, position, matcher.start());
			position = matcher.end();
			if (matcher.start(1) >= 0) {
				string.append(matcher.group(1).toUpperCase());
			}
		}
		string.append(name, position, name.length());

		return string.toString();
	}

}
