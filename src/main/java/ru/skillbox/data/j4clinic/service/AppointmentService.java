package ru.skillbox.data.j4clinic.service;

import ru.skillbox.data.j4clinic.model.Appointment;
import ru.skillbox.data.j4clinic.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AppointmentService {
	private final AppointmentRepository repository;

	public AppointmentService(AppointmentRepository repository) {
		this.repository = repository;
	}

	public Appointment createAppointment(String patientFullName,
			String doctorFullName,
			String doctorPosition,
			LocalDateTime appointmentTime,
			String comment) {
		validateNonEmpty(patientFullName, "patient_full_name");
		validateNonEmpty(doctorFullName, "doctor_full_name");
		validateNonEmpty(doctorPosition, "doctor_position");
		validateAppointmentTime(appointmentTime);

		Appointment a = new Appointment();
		a.setId(UUID.randomUUID());
		a.setPatientFullName(patientFullName.trim());
		a.setDoctorFullName(doctorFullName.trim());
		a.setDoctorPosition(doctorPosition.trim());
		a.setAppointmentTime(appointmentTime);
		a.setCreatedAt(LocalDateTime.now());
		a.setComment(comment == null || comment.isBlank() ? null : comment.trim());

		repository.insert(a);
		return a;
	}

	public Optional<Appointment> getById(UUID id) {
		return repository.findById(id);
	}

	public List<Appointment> listAll() {
		return repository.findAll();
	}

	public boolean updateAppointment(UUID id,
			String doctorFullName,
			String doctorPosition,
			LocalDateTime appointmentTime,
			String comment) {
		Optional<Appointment> existingOpt = repository.findById(id);
		if (existingOpt.isEmpty()) {
			return false;
		}
		Appointment existing = existingOpt.get();

		if (doctorFullName != null) {
			validateNonEmpty(doctorFullName, "doctor_full_name");
			existing.setDoctorFullName(doctorFullName.trim());
		}
		if (doctorPosition != null) {
			validateNonEmpty(doctorPosition, "doctor_position");
			existing.setDoctorPosition(doctorPosition.trim());
		}
		if (appointmentTime != null) {
			validateAppointmentTime(appointmentTime);
			existing.setAppointmentTime(appointmentTime);
		}
		existing.setComment(comment == null ? existing.getComment() : (comment.isBlank() ? null : comment.trim()));

		return repository.update(existing);
	}

	public boolean delete(UUID id) {
		return repository.deleteById(id);
	}

	private void validateNonEmpty(String value, String fieldName) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException("Field '" + fieldName + "' must be non-empty");
		}
	}

	private void validateAppointmentTime(LocalDateTime dateTime) {
		if (dateTime == null) {
			throw new IllegalArgumentException("appointment_time is required");
		}
		if (dateTime.isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("appointment_time must be in the future");
		}
	}
}
