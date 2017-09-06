package com.ulfric.dragoon.cfg4j;

import org.cfg4j.source.context.propertiesprovider.PropertiesProvider;
import org.cfg4j.source.context.propertiesprovider.PropertiesProviderSelector;

public class UnflatteningPropertiesProviderSelector extends PropertiesProviderSelector {

	public UnflatteningPropertiesProviderSelector(PropertiesProvider propertiesProvider,
	        PropertiesProvider yamlProvider, PropertiesProvider jsonProvider) {
		super(new UnflatteningPropertiesProvider(propertiesProvider), new UnflatteningPropertiesProvider(yamlProvider),
		        new UnflatteningPropertiesProvider(jsonProvider));
	}

}
