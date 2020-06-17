package org.example.todo.reminders.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.StoredProcedureQuery;

public class DatabaseCleanupJob implements Job {

	@Autowired
	private EntityManager entityManager;

	@Override
	public void execute(JobExecutionContext jobExecutionContext) {
		StoredProcedureQuery query = entityManager
				.createStoredProcedureQuery("DeleteJobsWithNoTriggers");
		query.execute();

	}
}
