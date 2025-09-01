CREATE TABLE IF NOT EXISTS appointments (
	id UUID PRIMARY KEY,
	patient_full_name VARCHAR NOT NULL,
	doctor_full_name VARCHAR NOT NULL,
	doctor_position VARCHAR NOT NULL,
	appointment_time TIMESTAMP NOT NULL,
	created_at TIMESTAMP NOT NULL,
	comment TEXT NULL
); 