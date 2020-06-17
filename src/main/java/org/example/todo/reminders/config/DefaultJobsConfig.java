package org.example.todo.reminders.config;

import org.example.todo.reminders.job.DatabaseCleanupJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class DefaultJobsConfig {

	@Bean
	public JobDetail databaseCleanupJobDetails() {
		return JobBuilder.newJob(DatabaseCleanupJob.class).withIdentity("databaseCleanup")
				.storeDurably().build();
	}

	@Bean
	public Trigger databaseCleanupTrigger(JobDetail databaseCleanupJobDetails) {
		return TriggerBuilder.newTrigger().forJob(databaseCleanupJobDetails)
				.withIdentity("databaseCleanupTrigger")
				.withSchedule(CronScheduleBuilder.cronSchedule("0 0 3 1/1 * ? *")
						.withMisfireHandlingInstructionFireAndProceed()
				.inTimeZone(TimeZone.getTimeZone("UTC")))
				.build();
	}
}