package com.ulfric.dragoon.cfg4j;

import org.cfg4j.source.reload.ReloadStrategy;
import org.cfg4j.source.reload.Reloadable;

import com.ulfric.dragoon.extension.inject.Inject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImprovedPeriodicalReloadStrategy implements ReloadStrategy {

	private static final ScheduledExecutorService POOL = Executors.newScheduledThreadPool(1, runnable -> {
			Thread thread = new Thread(runnable, "cfg4j-reload");
			thread.setDaemon(true);
			thread.setPriority(Thread.NORM_PRIORITY - 1);
			return thread;
		});
	private static final ConcurrentMap<Reloadable, ScheduledFuture<?>> TASKS = new ConcurrentHashMap<>(1);

	@Inject
	private Logger logger;

	private final long period;
	private final TimeUnit unit;

	public ImprovedPeriodicalReloadStrategy(Long period, TimeUnit unit) {
		this.period = period;
		this.unit = unit;
	}

	@Override
	public void register(Reloadable resource) {
		Runnable reload = () -> {
			try {
				resource.reload();
			} catch (Exception failure) {
				logger.log(Level.WARNING,
						"Periodical resource reload failed. Will retry in " + period + ' ' + unit,
						failure);
			}
		};

		POOL.scheduleAtFixedRate(resource::reload, period, period, unit);
		reload.run();
	}

	@Override
	public void deregister(Reloadable resource) {
		ScheduledFuture<?> task = TASKS.remove(resource);
		if (task != null) {
			task.cancel(false);
		}
	}

}
