package io.github.superbob.switches.scheduling;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ratpack.exec.ExecController;
import ratpack.registry.Registry;
import ratpack.service.internal.DefaultEvent;

class SchedulerServiceTest
{
	private static final int PERIOD = 5;
	private static final int INITIAL_DELAY = 10;
	private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
	private Runnable command;
	private SchedulerService schedulerService;
	private Registry registry;
	private ExecController execController;
	private ScheduledExecutorService scheduledExecutorService;
	private ScheduledFuture scheduledFuture;

	@BeforeEach
	void setUp()
	{
		command = mock(Runnable.class);
		schedulerService = new SchedulerService(command, INITIAL_DELAY, PERIOD, TIME_UNIT);
		registry = mock(Registry.class);
		execController = mock(ExecController.class);
		scheduledExecutorService = mock(ScheduledExecutorService.class);
		scheduledFuture = mock(ScheduledFuture.class);

		when(registry.get(any(getExecControllerClass()))).thenReturn(execController);
		when(execController.getExecutor()).thenReturn(scheduledExecutorService);
		when(scheduledExecutorService.scheduleAtFixedRate(any(), anyLong(), anyLong(), any())).thenReturn(scheduledFuture);
	}

	@DisplayName("Should schedule command when onStart is called")
	@Test
	void onStartSchedules()
	{
		schedulerService.onStart(new DefaultEvent(registry, false));

		verify(registry).get(ExecController.class);
		verify(execController).getExecutor();
		verify(scheduledExecutorService).scheduleAtFixedRate(command, INITIAL_DELAY, PERIOD, TIME_UNIT);
		verifyZeroInteractions(scheduledFuture);
		verifyNoMoreInteractions(registry, execController, scheduledExecutorService);
	}

	@DisplayName("Should cancel future when onStop is called")
	@Test
	void onStopCancels()
	{
		schedulerService.onStart(new DefaultEvent(registry, false));
		schedulerService.onStop(new DefaultEvent(registry, false));

		verify(registry).get(ExecController.class);
		verify(execController).getExecutor();
		verify(scheduledExecutorService).scheduleAtFixedRate(command, INITIAL_DELAY, PERIOD, TIME_UNIT);
		verify(scheduledFuture).cancel(false);
		verifyNoMoreInteractions(registry, execController, scheduledExecutorService, scheduledFuture);
	}

	@SuppressWarnings("unchecked")
	private Class<Class<ExecController>> getExecControllerClass()
	{
		return (Class<Class<ExecController>>)(Class<?>)Class.class;
	}
}
