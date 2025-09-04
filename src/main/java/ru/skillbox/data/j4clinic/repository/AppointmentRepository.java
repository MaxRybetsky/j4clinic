package ru.skillbox.data.j4clinic.repository;

import ru.skillbox.data.j4clinic.model.Appointment;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AppointmentRepository {
	private final DataSource dataSource;

	public AppointmentRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Optional<Appointment> findById(UUID id) {
		final String sql = "SELECT id, patient_full_name, doctor_full_name, doctor_position, appointment_time, created_at, comment FROM appointments WHERE id = ?";
		try (Connection connection = dataSource.getConnection();
			 PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setObject(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return Optional.empty();
				}
				return Optional.of(mapRow(rs));
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to fetch appointment by id", e);
		}
	}

	public List<Appointment> findAll() {
		final String sql = "SELECT id, patient_full_name, doctor_full_name, doctor_position, appointment_time, created_at, comment FROM appointments ORDER BY appointment_time ASC";
		List<Appointment> result = new ArrayList<>();
		try (Connection connection = dataSource.getConnection();
			 PreparedStatement ps = connection.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				result.add(mapRow(rs));
			}
			return result;
		} catch (SQLException e) {
			throw new RuntimeException("Failed to list appointments", e);
		}
	}

	public void insert(Appointment a) {
		final String sql = "INSERT INTO appointments (id, patient_full_name, doctor_full_name, doctor_position, appointment_time, created_at, comment) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (Connection connection = dataSource.getConnection();
			 PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setObject(1, a.getId());
			ps.setString(2, a.getPatientFullName());
			ps.setString(3, a.getDoctorFullName());
			ps.setString(4, a.getDoctorPosition());
			ps.setObject(5, a.getAppointmentTime());
			ps.setObject(6, a.getCreatedAt());
			ps.setString(7, a.getComment());
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Failed to insert appointment", e);
		}
	}

	public boolean update(Appointment a) {
		final String sql = "UPDATE appointments SET doctor_full_name = ?, doctor_position = ?, appointment_time = ?, comment = ? WHERE id = ?";
		try (Connection connection = dataSource.getConnection();
			 PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, a.getDoctorFullName());
			ps.setString(2, a.getDoctorPosition());
			ps.setObject(3, a.getAppointmentTime());
			ps.setString(4, a.getComment());
			ps.setObject(5, a.getId());
			int updated = ps.executeUpdate();
			return updated > 0;
		} catch (SQLException e) {
			throw new RuntimeException("Failed to update appointment", e);
		}
	}

	public boolean deleteById(UUID id) {
		final String sql = "DELETE FROM appointments WHERE id = ?";
		try (Connection connection = dataSource.getConnection();
			 PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setObject(1, id);
			int deleted = ps.executeUpdate();
			return deleted > 0;
		} catch (SQLException e) {
			throw new RuntimeException("Failed to delete appointment", e);
		}
	}

	private Appointment mapRow(ResultSet rs) throws SQLException {
		Appointment appointment = new Appointment();
		appointment.setId(rs.getObject("id", UUID.class));
		appointment.setPatientFullName(rs.getString("patient_full_name"));
		appointment.setDoctorFullName(rs.getString("doctor_full_name"));
		appointment.setDoctorPosition(rs.getString("doctor_position"));
		appointment.setAppointmentTime(rs.getObject("appointment_time", LocalDateTime.class));
		appointment.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
		appointment.setComment(rs.getString("comment"));
		return appointment;
	}
}
