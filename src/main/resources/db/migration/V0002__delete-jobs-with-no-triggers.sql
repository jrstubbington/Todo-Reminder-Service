DELIMITER $$

CREATE PROCEDURE DeleteJobsWithNoTriggers()
BEGIN
    DELETE
    	details
    FROM
    	reminders.QRTZ_JOB_DETAILS as details
    LEFT JOIN
    	reminders.QRTZ_TRIGGERS as qrtz_trg ON details.JOB_NAME = qrtz_trg.JOB_NAME
    WHERE
    	qrtz_trg.JOB_NAME IS NULL
    ;
END $$

DELIMITER ;