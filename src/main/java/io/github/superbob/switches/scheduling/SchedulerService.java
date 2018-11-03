package io.github.superbob.switches.scheduling;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import ratpack.exec.ExecController;
import ratpack.service.Service;
import ratpack.service.StartEvent;
import ratpack.service.StopEvent;

public class SchedulerService implements Service {
	private ScheduledFuture<?> scheduledFuture;
	private final int initialDelay;
	private final int period;
	private final TimeUnit timeUnit;
	private final Runnable command;

	public SchedulerService(Runnable command, int initialDelay, int period, TimeUnit timeUnit) {
		this.initialDelay = initialDelay;
		this.period = period;
		this.timeUnit = timeUnit;
		this.command = command;
	}

	@Override
	public void onStart(StartEvent event) {
		final ScheduledExecutorService executorService = event.getRegistry().get(ExecController.class).getExecutor();
		scheduledFuture = executorService.scheduleAtFixedRate(command, initialDelay, period, timeUnit);
	}

	@Override
	public void onStop(StopEvent event) {
		Optional.ofNullable(scheduledFuture).ifPresent(s -> s.cancel(false));
	}
}
