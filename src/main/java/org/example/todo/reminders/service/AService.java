package org.example.todo.reminders.service;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class AService {

	@Autowired
	private Scheduler scheduler;

	@Scheduled(initialDelay = 1000, fixedDelay = 10000)
	public void showAllJobsWithTriggers() throws SchedulerException {
		for (String groupName : scheduler.getJobGroupNames()) {
			for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

				String jobName = jobKey.getName();
				String jobGroup = jobKey.getGroup();

				//get job's trigger
				List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
				if (triggers.isEmpty()) {
					scheduler.deleteJob(jobKey);
					continue;
				}
				for (Trigger trigger : triggers) {
					Date nextFireTime = trigger.getNextFireTime();
					log.info("[jobName] : {} [groupName] : {} - {}", jobName, jobGroup, nextFireTime);
				}
			}
		}
	}

	public void printTime() {
		log.info("The current time is {}", OffsetDateTime.now());
	}
}
