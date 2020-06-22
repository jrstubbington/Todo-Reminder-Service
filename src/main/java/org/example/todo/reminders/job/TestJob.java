package org.example.todo.reminders.job;

import lombok.extern.slf4j.Slf4j;
import org.example.todo.accounts.generated.controller.UserManagementApi;
import org.example.todo.accounts.generated.dto.ResponseContainerUserDto;
import org.example.todo.accounts.generated.dto.UserDto;
import org.example.todo.accounts.generated.dto.UserProfileDto;
import org.example.todo.common.exceptions.ResourceNotFoundException;
import org.example.todo.preferences.generated.controller.UserPreferencesManagementApi;
import org.example.todo.preferences.generated.dto.ResponseContainerUserPreferenceDto;
import org.example.todo.preferences.generated.dto.UserPreferenceDto;
import org.example.todo.reminders.config.Preference;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Slf4j
public class TestJob implements Job {

	@Autowired
	private UserPreferencesManagementApi userPreferencesManagementApi;

	@Autowired
	private UserManagementApi userManagementApi;

	@Override
	public void execute(JobExecutionContext context) {
		JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
		//TODO: change these to a set of UUIDs so that "watchers" could be notified as well
		// and notifications aren't duplicated when it's created by and assigned to the same person

		UUID assignedToUserUuid = (UUID) jobDataMap.get("assignedToUserUuid");
		UUID createdByUserUuid = (UUID) jobDataMap.get("createdByUserUuid");
		UUID workspaceUuid = (UUID) jobDataMap.get("workspaceUuid");
		log.info("Job firing created by {}, assigned to {}, in workspace {}", createdByUserUuid, assignedToUserUuid, workspaceUuid);

		//TODO: Getting the user preferences should be done when the job is created and set via JobData
		// Yeah but then if a job is created but a user updates their preferences it won't be correct?
		// Update job data when notification preferences are changed?
		UserPreferenceDto assignedToUserPreference = getUserPreferenceByName(assignedToUserUuid, Preference.EMAIL_REMINDERS.toString());
		UserPreferenceDto createdByUserPreference = getUserPreferenceByName(createdByUserUuid, Preference.EMAIL_REMINDERS.toString());

		log.info("ASSIGNED TO PREFERENCE: {}", assignedToUserPreference);
		log.info("CREATED BY PREFERENCE: {}", createdByUserPreference);

		if (assignedToUserPreference.getUserSelection()) {
			ResponseContainerUserDto responseContainerUserDto = userManagementApi.getUserByUUID(assignedToUserUuid);
			if (!responseContainerUserDto.getData().isEmpty()) {
				UserDto userDto = responseContainerUserDto.getData().get(0);
				UserProfileDto userProfileDto= userDto.getUserProfile();
				log.info("SENDING EMAIL TO USER {} {} AT ADDRESS {}", userProfileDto.getFirstName(), userProfileDto.getLastName(), userProfileDto.getEmail());
			}

		}
	}

	private UserPreferenceDto getUserPreferenceByName(UUID uuid, String name) { //TODO: convert to enum
		UserPreferenceDto userPreference = null;
		try {
			ResponseContainerUserPreferenceDto responseContainerUserPreferenceDto = userPreferencesManagementApi.getSpecificPreferenceForUserUuidByName(uuid, name);
			if (!responseContainerUserPreferenceDto.getData().isEmpty()) {
				userPreference = responseContainerUserPreferenceDto.getData().get(0);
			}
		}
		catch (ResourceNotFoundException e) {
			log.debug("{}", e.getMessage());
			log.trace("Error:", e);
		}
		catch (RestClientException e) {
			log.info("Failed to do the thing");
			log.debug("Error:", e);
		}
		return userPreference;
	}
}