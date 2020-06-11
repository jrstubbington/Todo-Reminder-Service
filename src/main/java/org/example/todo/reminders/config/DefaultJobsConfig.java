package org.example.todo.reminders.config;

import org.apache.commons.lang3.time.DateUtils;
import org.example.todo.reminders.job.AJob;
import org.example.todo.reminders.job.DatabaseCleanupJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
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

	@Bean
	public JobDetail jobADetails() {
		return JobBuilder.newJob(AJob.class).withIdentity("sampleJobA")
				.storeDurably().build();
	}

	@Bean
	public Trigger jobATrigger(JobDetail jobADetails) {
		return TriggerBuilder.newTrigger().forJob(jobADetails)
				.withIdentity("sampleTriggerA")
//				.withSchedule(CronScheduleBuilder.cronSchedule("0/1 * * ? * * *"))
				.startAt(DateUtils.addMinutes(new Date(), 1))
				.withSchedule(SimpleScheduleBuilder.simpleSchedule()
						.withMisfireHandlingInstructionFireNow())
				.build();
	}
}