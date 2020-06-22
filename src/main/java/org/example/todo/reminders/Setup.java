package org.example.todo.reminders;

import lombok.extern.slf4j.Slf4j;
import org.example.todo.preferences.generated.controller.GlobalPreferencesManagementApi;
import org.example.todo.preferences.generated.controller.PreferenceCategoryManagementApi;
import org.example.todo.preferences.generated.dto.PreferenceCategoryDto;
import org.example.todo.preferences.generated.dto.PreferenceDto;
import org.example.todo.preferences.generated.dto.ResponseContainerPreferenceCategoryDto;
import org.example.todo.reminders.config.Preference;
import org.example.todo.reminders.config.PreferenceCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@Profile("local")
public class Setup {

	@Autowired
	private GlobalPreferencesManagementApi globalPreferencesManagementApi;

	@Autowired
	private PreferenceCategoryManagementApi preferenceCategoryManagementApi;

	@Bean
	public CommandLineRunner demo() {
		return args -> {

			PreferenceDto remindersViaEmail = new PreferenceDto();
			remindersViaEmail.setDefaultSelection(true);
			remindersViaEmail.setName(Preference.EMAIL_REMINDERS.toString());
			remindersViaEmail.setDescription("Receive reminders via email");

			PreferenceDto remindersViaSms = new PreferenceDto();
			remindersViaSms.setDefaultSelection(true);
			remindersViaSms.setName(Preference.SMS_REMINDERS.toString());
			remindersViaSms.setDescription("Receive reminders via sms");


			PreferenceCategoryDto notificationsCategory = new PreferenceCategoryDto();
			notificationsCategory.setName(PreferenceCategory.NOTIFICATION.toString());

			ResponseContainerPreferenceCategoryDto preferenceCategoryDto = preferenceCategoryManagementApi.createPreferenceCategory(notificationsCategory);
			UUID preferenceCategoryUuid = null;
			if (!preferenceCategoryDto.getData().isEmpty()) {
				preferenceCategoryUuid = preferenceCategoryDto.getData().get(0).getUuid();
			}

			remindersViaEmail.setPreferenceCategoryUuid(preferenceCategoryUuid);
			remindersViaSms.setPreferenceCategoryUuid(preferenceCategoryUuid);

			globalPreferencesManagementApi.addNewPreference(remindersViaEmail);
			globalPreferencesManagementApi.addNewPreference(remindersViaSms);
		};
	}
}
