package org.example.todo.reminders.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.util.UUID;

@Slf4j
public class TestJob implements Job {

	@Override
	public void execute(JobExecutionContext context) {
		JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
		UUID assignedToUserUuid = (UUID) jobDataMap.get("assignedToUserUuid");
		UUID createdByUserUuid = (UUID) jobDataMap.get("createdByUserUuid");
		UUID workspaceUuid = (UUID) jobDataMap.get("workspaceUuid");
		log.info("Job firing created by {}, assigned to {}, in workspace {}", createdByUserUuid, assignedToUserUuid, workspaceUuid);
	}
}