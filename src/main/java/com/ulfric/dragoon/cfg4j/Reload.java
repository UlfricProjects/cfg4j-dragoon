package com.ulfric.dragoon.cfg4j;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.util.concurrent.TimeUnit;

@Retention(RUNTIME)
public @interface Reload {

	long period() default 2L;

	TimeUnit unit() default TimeUnit.MINUTES;

}