package com.ulfric.dragoon.cfg4j;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(FIELD)
public @interface Settings {

	String value() default "";

	String extension() default SettingsExtension.DEFAULT_FILE_EXTENSION;

	Reload reload() default @Reload;

}
