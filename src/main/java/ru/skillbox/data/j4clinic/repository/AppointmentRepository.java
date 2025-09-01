package ru.skillbox.data.j4clinic.repository;

import ru.skillbox.data.j4clinic.model.Appointment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AppointmentRepository {
	private final JdbcTemplate jdbcTemplate;
	private static final RowMapper<Appointment> ROW_MAPPER_BY_BEAN = BeanPropertyRowMapper.newInstance(Appointment.class);
	private static final RowMapper<Appointment> ROW_MAPPER_MANUAL = (resultSet, rowNum) -> {
		Appointment appointment = new Appointment();

		appointment.setId(resultSet.getObject("id", UUID.class));
		appointment.setPatientFullName(resultSet.getString("patient_full_name"));
		appointment.setDoctorFullName(resultSet.getString("doctor_full_name"));
		appointment.setDoctorPosition(resultSet.getString("doctor_position"));
		appointment.setAppointmentTime(resultSet.getObject("appointment_time", LocalDateTime.class));
		appointment.setCreatedAt(resultSet.getObject("created_at", LocalDateTime.class));
		appointment.setComment(resultSet.getString("comment"));

		return appointment;
	};

	public AppointmentRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Optional<Appointment> findById(UUID id) {
		List<Appointment> list = jdbcTemplate.query(
			"SELECT * FROM appointments WHERE id = ?",
				ROW_MAPPER_BY_BEAN,
			id
		);
		if (list.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(list.get(0));
	}

	public List<Appointment> findAll() {
		return jdbcTemplate.query(
			"SELECT * FROM appointments ORDER BY appointment_time ASC",
				ROW_MAPPER_MANUAL
		);
	}

	public void insert(Appointment a) {
		jdbcTemplate.update(
			"INSERT INTO appointments (id, patient_full_name, doctor_full_name, doctor_position, appointment_time, created_at, comment) VALUES (?, ?, ?, ?, ?, ?, ?)",
			a.getId(),
			a.getPatientFullName(),
			a.getDoctorFullName(),
			a.getDoctorPosition(),
			a.getAppointmentTime(),
			a.getCreatedAt(),
			a.getComment()
		);
	}

	public boolean update(Appointment a) {
		int updated = jdbcTemplate.update(
			"UPDATE appointments SET doctor_full_name = ?, doctor_position = ?, appointment_time = ?, comment = ? WHERE id = ?",
			a.getDoctorFullName(),
			a.getDoctorPosition(),
			a.getAppointmentTime(),
			a.getComment(),
			a.getId()
		);
		return updated > 0;
	}

	public boolean deleteById(UUID id) {
		int deleted = jdbcTemplate.update("DELETE FROM appointments WHERE id = ?", id);
		return deleted > 0;
	}
}
