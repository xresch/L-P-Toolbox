package com.pengtoolbox.cfw.tests.schedule;

import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.stats.StatsMethodSamplingTask;

public class TestSchedule {

	public static boolean isExecuted = false;
	
	//@Test
	public void testSchedule() throws InterruptedException {
		
		ScheduledFuture<?> future = CFW.Schedule.runPeriodically(5, 1, new Runnable() {
			int count = 1;
			@Override
			public void run() {
				System.out.println("Task: "+count++);
				

			}
		});
		
		ScheduledFuture<?> terminator = CFW.Schedule.runPeriodically(10, 1, new Runnable() {
			@Override
			public void run() {
				System.out.println("Terminate");
				future.cancel(true);
			}
		});
		
		while(!future.isDone()) {
			Thread.sleep(500L);
		}
	}
	
	//@Test
	public void testScheduleWeekly() throws InterruptedException {
		
		// set the time to a past date time to check if it works
		CFW.Schedule.scheduleWeekly(Calendar.MONDAY, 19, 46, 40, new TimerTask() {

			@Override
			public void run() {
				System.out.println("Executed on: "+new Date().toLocaleString());
				TestSchedule.isExecuted = true;
			}
			
		});
		
		while(!isExecuted) {
			Thread.sleep(500L);
		}
	}
	
	/******************************************************************
	 * Check if the time is increased above current time correctly
	 * for the next scheduled execution.
	 * @throws InterruptedException
	 ******************************************************************/
	//@Test
	public void testScheduleJumpIncrease() throws InterruptedException {
		
		// set the time to a past date time to check if it works
		CFW.Schedule.scheduleTimed((int)TimeUnit.MINUTES.toSeconds(1), Calendar.MONDAY, 19, 46, 40, new TimerTask() {

			@Override
			public void run() {
				System.out.println("Executed on: "+new Date().toLocaleString());
				TestSchedule.isExecuted = true;
			}
			
		});
		
		while(!isExecuted) {
			Thread.sleep(500L);
		}
	}
	
	/******************************************************************
	 * Check Thread Sampling Task
	 * @throws InterruptedException
	 ******************************************************************/
	@Test
	public void testThreadSamplingTask() throws InterruptedException {
		
		ScheduledFuture<?> future = CFW.Schedule.runPeriodically(0, 1, new StatsMethodSamplingTask());
		
		ScheduledFuture<?> terminator = CFW.Schedule.runPeriodically(10, 1, new Runnable() {
			@Override
			public void run() {
				System.out.println("Terminate");
				future.cancel(true);
			}
		});
		
		while(!future.isDone()) {
			Thread.sleep(500L);
		}
	}
}
