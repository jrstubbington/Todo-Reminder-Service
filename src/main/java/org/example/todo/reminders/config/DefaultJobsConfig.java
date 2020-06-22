package org.example.todo.reminders.config;

import org.apache.commons.lang3.time.DateUtils;
import org.example.todo.reminders.job.DatabaseCleanupJob;
import org.example.todo.reminders.job.TestJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

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
	public JobDetail testingApiJob() {
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put("createdByUserUuid", UUID.fromString("2d4aeb04-d7f4-4613-bbd2-54cda61d0e9d"));
		jobDataMap.put("assignedToUserUuid", UUID.fromString("2d4aeb04-d7f4-4613-bbd2-54cda61d0e9d"));
		jobDataMap.put("workspaceUuid", UUID.fromString("2d4aeb04-d7f4-4613-bbd2-54cda61d0e9d"));

		return JobBuilder.newJob(TestJob.class).withIdentity("testingjob")
				.storeDurably()
				.usingJobData(jobDataMap)
				.build();
	}

	@Bean
	public Trigger testingJobTrigger(JobDetail testingApiJob) {
		return TriggerBuilder.newTrigger().forJob(testingApiJob)
				.withIdentity("testingJobTrigger")
				.startAt(DateUtils.addSeconds(new Date(), 10))
				.withSchedule(SimpleScheduleBuilder.simpleSchedule())
				.build();
	}
}