package ru.skillbox.data.j4clinic.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
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
    private static final Logger log = LoggerFactory.getLogger(AppointmentRepository.class);
    private final JdbcTemplate jdbcTemplate;

    /**
     * Автоматически созданный RowMapper
     */
    private static final RowMapper<Appointment> ROW_MAPPER_BY_BEAN = BeanPropertyRowMapper.newInstance(Appointment.class);

    /**
     * RowMapper, созданный вручную с явным указанием маппингов.
     */
    private static final RowMapper<Appointment> ROW_MAPPER_MANUAL = (resultSet, rowNum) -> {
        Appointment appointment = new Appointment();

        appointment.setId(resultSet.getObject("a_id", UUID.class));
        appointment.setPatientFullName(resultSet.getString("p_fio"));
        appointment.setDoctorFullName(resultSet.getString("d_fio"));
        appointment.setDoctorPosition(resultSet.getString("d_pos"));
        appointment.setAppointmentTime(resultSet.getObject("app_time", LocalDateTime.class));
        appointment.setCreatedAt(resultSet.getObject("cr_time", LocalDateTime.class));
        appointment.setComment(resultSet.getString("comm"));

        return appointment;
    };

    @Autowired
    public AppointmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Appointment> findById(UUID id) {
        try {
            String sql = "SELECT * FROM appointments WHERE id = ?";

            Appointment appointment = jdbcTemplate.queryForObject(
                    sql,
                    ROW_MAPPER_BY_BEAN,
                    id
            );

            return Optional.ofNullable(appointment);
        } catch (EmptyResultDataAccessException e) {
            log.error("Error: there is no such entity", e);
            return Optional.empty();
        }
    }

    public List<Appointment> findAll() {
        String sql = """
                SELECT app.id AS a_id,
                		app.patient_full_name AS p_fio,
                		app.doctor_full_name AS d_fio,
                		app.doctor_position AS d_pos,
                		app.appointment_time AS app_time,
                		app.created_at AS cr_time,
                		app.comment AS comm
                FROM appointments app
                ORDER BY appointment_time ASC
                """;

        return jdbcTemplate.query(
                sql,
                ROW_MAPPER_MANUAL
        );
    }

    public void insert(Appointment appointment) {
        String sql = """
                	INSERT INTO appointments (id, patient_full_name,
                							  doctor_full_name, doctor_position,
                							  appointment_time, created_at, comment)
                	VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                appointment.getId(),
                appointment.getPatientFullName(),
                appointment.getDoctorFullName(),
                appointment.getDoctorPosition(),
                appointment.getAppointmentTime(),
                appointment.getCreatedAt(),
                appointment.getComment()
        );
    }

    public boolean update(Appointment appointment) {
        String sql = """
                	UPDATE appointments
                	SET doctor_full_name = ?, doctor_position = ?,
                		appointment_time = ?, comment = ?
                	WHERE id = ?
                """;
        int updated = jdbcTemplate.update(
                sql,
                appointment.getDoctorFullName(),
                appointment.getDoctorPosition(),
                appointment.getAppointmentTime(),
                appointment.getComment(),
                appointment.getId()
        );
        return updated > 0;
    }

    public boolean deleteById(UUID id) {
        String sql = "DELETE FROM appointments WHERE id = ?";

        int deleted = jdbcTemplate.update(sql, id);

        return deleted > 0;
    }
}
