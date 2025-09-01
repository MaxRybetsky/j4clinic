package ru.skillbox.data.j4clinic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
	private UUID id;
	private String patientFullName;
	private String doctorFullName;
	private String doctorPosition;
	private LocalDateTime appointmentTime;
	private LocalDateTime createdAt;
	private String comment;
}
