package org.example.todo.reminders.job;

import org.example.todo.reminders.service.AService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

public class AJob implements Job {

	@Autowired
	private AService aService;

	@Override
	public void execute(JobExecutionContext context) {
		aService.printTime();
	}
}