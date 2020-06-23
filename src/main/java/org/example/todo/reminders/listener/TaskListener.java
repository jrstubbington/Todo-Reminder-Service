package org.example.todo.reminders.listener;

import lombok.extern.slf4j.Slf4j;
import org.example.todo.common.kafka.KafkaOperation;
import org.example.todo.reminders.job.TestJob;
import org.example.todo.tasks.generated.dto.TaskDto;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Component
@Slf4j
public class TaskListener {

	@Autowired
	private Scheduler scheduler;

	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.info("{}", event.getSource());
		log.info("startup");
	}

//	@Scheduled(initialDelay = 30000, fixedDelay = 10000)
//	private void checkForJobWithUUIDKey() throws SchedulerException {
//		scheduler.getJobDetail(JobKey.jobKey("sampleJobA", "reminders"));
//		System.out.println("JOBDETAIL: "  + scheduler.getJobDetail(JobKey.jobKey("sampleJobA")));
//		System.out.println("JOBTRIGGERS: "  + scheduler.getTriggersOfJob(JobKey.jobKey("sampleJobA")));
//	}

	@KafkaListener(topics = "tasks")
	void listen(TaskDto taskDto,
	            Acknowledgment acknowledgment,
	            @Header(value = "operation") KafkaOperation kafkaOperation) throws SchedulerException {
		log.trace("Received Message: {}", taskDto);

		UUID taskUuid = taskDto.getUuid();
		JobKey jobKey = JobKey.jobKey(taskUuid.toString(), "reminders");
		TriggerKey triggerKey = TriggerKey.triggerKey(taskUuid.toString(), "reminders");

		if (Objects.isNull(taskDto.getReminderDate())) {
			log.debug("Task with UUID {} has no reminder date", taskUuid);
			scheduler.deleteJob(jobKey);
			acknowledgment.acknowledge();
			return;
		}

		//TODO: Ensure that reminder date is in the future.
		if (taskDto.getReminderDate().isBefore(OffsetDateTime.now())) {
			log.debug("Task with UUID {} has a reminder date that's in the past; at time {}", taskUuid, taskDto.getReminderDate());
			scheduler.deleteJob(jobKey);
			acknowledgment.acknowledge();
			return;
		}

		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put("createdByUserUuid", taskDto.getCreatedByUserUuid());
		jobDataMap.put("assignedToUserUuid", taskDto.getAssignedToUserUuid());
		jobDataMap.put("workspaceUuid", taskDto.getWorkspaceUuid());

		JobDetail jobDetail = JobBuilder.newJob(TestJob.class)
				.storeDurably()
				.withIdentity(jobKey)
				.usingJobData(jobDataMap)
				.build();

		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(triggerKey)
				.forJob(jobDetail)
				.startAt(Date.from(taskDto.getReminderDate().toInstant()))
				.withSchedule(SimpleScheduleBuilder.simpleSchedule())
				.build();


		switch (Objects.requireNonNull(kafkaOperation)) {
			case CREATE:
				log.debug("Adding task with uuid {}", taskUuid);
				scheduler.scheduleJob(jobDetail, trigger);
				break;
			case DELETE:
				log.debug("Removing job for task with uuid {}", taskUuid);
				scheduler.deleteJob(jobKey);
				break;
			case UPDATE:
				log.debug("Updating task with uuid {}", taskUuid);
				//Ensure the job actually exists first or create new one
				// user could have deleted a reminder and then added one back later
				JobDetail jobCheck = scheduler.getJobDetail(jobKey);
				if (Objects.isNull(jobCheck)) {
					log.debug("Creating new job for existing task");
					scheduler.scheduleJob(jobDetail, trigger);
				}
				else if (scheduler.getTriggersOfJob(jobKey).isEmpty()) {
					log.debug("Adding trigger for existing job for existing task");
					scheduler.scheduleJob(trigger);
				}
				else {
					log.debug("Updating trigger for existing job for existing task");
					scheduler.rescheduleJob(triggerKey, trigger);
				}
				break;
			default:
				log.info("Unrecognized Kafka Operation: {}", kafkaOperation);
				break;
		}
		acknowledgment.acknowledge();
	}
}
