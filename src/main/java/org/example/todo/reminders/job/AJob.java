package org.example.todo.reminders.job;

import lombok.extern.slf4j.Slf4j;
import org.example.todo.reminders.service.AService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class AJob implements Job {

	@Autowired
	private AService aService;

	@Override
	public void execute(JobExecutionContext context) {
		aService.printTime();
	}
}