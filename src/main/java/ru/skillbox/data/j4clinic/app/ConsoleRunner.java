package ru.skillbox.data.j4clinic.app;

import lombok.RequiredArgsConstructor;
import ru.skillbox.data.j4clinic.model.Appointment;
import ru.skillbox.data.j4clinic.service.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

@Component
@Order(1)
@RequiredArgsConstructor
public class ConsoleRunner implements CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(ConsoleRunner.class);
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final AppointmentService service;

	@Override
	public void run(String... args) {
		Scanner scanner = new Scanner(System.in);
		while (true) {
			printMenu();
			String choice = scanner.nextLine().trim();
			switch (choice) {
				case "1" -> createAppointment(scanner);
				case "2" -> viewAppointment(scanner);
				case "3" -> editAppointment(scanner);
				case "4" -> deleteAppointment(scanner);
				case "5" -> listAppointments();
				case "0" -> exit();
				default -> System.out.println("Неизвестная опция. Попробуйте снова.");
			}
			System.out.println();
		}
	}

	private void printMenu() {
		System.out.println("Выберите опцию:");
		System.out.println("1 - Создать запись на приём");
		System.out.println("2 - Просмотреть детали записи по ID");
		System.out.println("3 - Редактировать запись");
		System.out.println("4 - Удалить запись");
		System.out.println("5 - Список всех записей");
		System.out.println("0 - Выход");
		System.out.print("> ");
	}

	private void createAppointment(Scanner scanner) {
		try {
			String patientFullName = promptNonEmpty(scanner, "ФИО пациента");
			String doctorFullName = promptNonEmpty(scanner, "ФИО врача");
			String doctorPosition = promptNonEmpty(scanner, "Должность врача");
			LocalDateTime appointmentTime = promptDateTime(scanner);
			System.out.print("Комментарий (необязательно): ");
			String comment = blankToNull(scanner.nextLine());

			Appointment created = service.createAppointment(
					patientFullName,
					doctorFullName,
					doctorPosition,
					appointmentTime,
					comment
			);
			System.out.println("Создана запись с ID: " + created.getId());
		} catch (IllegalArgumentException ex) {
			System.out.println("Ошибка валидации: " + ex.getMessage());
		} catch (Exception ex) {
			System.out.println("Не удалось создать запись: " + ex.getMessage());
			log.error("Create appointment failed", ex);
		}
	}

	private void viewAppointment(Scanner scanner) {
		UUID id = promptUuid(scanner);
		Optional<Appointment> appt = service.getById(id);
		if (appt.isEmpty()) {
			System.out.println("Запись не найдена");
			return;
		}
		Appointment a = appt.get();
		System.out.println("ID: " + a.getId());
		System.out.println("Пациент: " + a.getPatientFullName());
		System.out.println("Врач: " + a.getDoctorFullName());
		System.out.println("Должность: " + a.getDoctorPosition());
		System.out.println("Время приёма: " + a.getAppointmentTime().format(DATE_TIME_FORMATTER));
		System.out.println("Создано: " + a.getCreatedAt().format(DATE_TIME_FORMATTER));
		System.out.println("Комментарий: " + (a.getComment() == null ? "" : a.getComment()));
	}

	private void editAppointment(Scanner scanner) {
		UUID id = promptUuid(scanner);
		Optional<Appointment> appt = service.getById(id);
		if (appt.isEmpty()) {
			System.out.println("Запись не найдена");
			return;
		}
		Appointment current = appt.get();
		System.out.println("Оставьте пустым, чтобы сохранить текущее значение.");
		System.out.println("Текущее ФИО врача: " + current.getDoctorFullName());
		System.out.print("Новое ФИО врача: ");
		String doctorFullName = keepOrNew(scanner.nextLine(), current.getDoctorFullName());

		System.out.println("Текущая должность врача: " + current.getDoctorPosition());
		System.out.print("Новая должность врача: ");
		String doctorPosition = keepOrNew(scanner.nextLine(), current.getDoctorPosition());

		System.out.println("Текущее время приёма: " + current.getAppointmentTime().format(DATE_TIME_FORMATTER));
		System.out.print("Новое время приёма (yyyy-MM-dd HH:mm): ");
		LocalDateTime appointmentTime = null;
		String dtStr = scanner.nextLine();
		if (dtStr != null && !dtStr.trim().isEmpty()) {
			try {
				appointmentTime = LocalDateTime.parse(dtStr.trim(), DATE_TIME_FORMATTER);
			} catch (DateTimeParseException ex) {
				System.out.println("Неверный формат даты/времени. Редактирование отменено.");
				return;
			}
		}

		System.out.println("Текущий комментарий: " + (current.getComment() == null ? "" : current.getComment()));
		System.out.print("Новый комментарий (пусто = очистить): ");
		String commentInput = scanner.nextLine();
		String comment = commentInput == null ? current.getComment() : (commentInput.isBlank() ? null : commentInput);

		try {
			boolean updated = service.updateAppointment(
					id,
					doctorFullName.equals(current.getDoctorFullName()) ? null : doctorFullName,
					doctorPosition.equals(current.getDoctorPosition()) ? null : doctorPosition,
					appointmentTime,
					comment
			);
			if (updated) {
				System.out.println("Запись обновлена.");
			} else {
				System.out.println("Ничего не было обновлено.");
			}
		} catch (IllegalArgumentException ex) {
			System.out.println("Ошибка валидации: " + ex.getMessage());
		} catch (Exception ex) {
			System.out.println("Не удалось обновить запись: " + ex.getMessage());
			log.error("Update appointment failed", ex);
		}
	}

	private void deleteAppointment(Scanner scanner) {
		UUID id = promptUuid(scanner);
		boolean deleted = service.delete(id);
		if (deleted) {
			System.out.println("Запись удалена.");
		} else {
			System.out.println("Запись не найдена.");
		}
	}

	private void listAppointments() {
		List<Appointment> list = service.listAll();
		if (list.isEmpty()) {
			System.out.println("Записей нет.");
			return;
		}
		for (Appointment a : list) {
			System.out.println(
					a.getId() + " | " +
					a.getPatientFullName() + " | " +
					a.getDoctorFullName() + " | " +
					a.getAppointmentTime().format(DATE_TIME_FORMATTER)
			);
		}
	}

	private void exit() {
		System.out.println("До свидания!");
		System.exit(0);
	}

	private String promptNonEmpty(Scanner scanner, String label) {
		while (true) {
			System.out.print(label + ": ");
			String value = scanner.nextLine();
			if (value != null && !value.trim().isEmpty()) {
				return value.trim();
			}
			System.out.println("Значение не должно быть пустым. Попробуйте снова.");
		}
	}

	private LocalDateTime promptDateTime(Scanner scanner) {
		while (true) {
			System.out.print("Время приёма (yyyy-MM-dd HH:mm): ");
			String value = scanner.nextLine();
			try {
				LocalDateTime dt = LocalDateTime.parse(value.trim(), DATE_TIME_FORMATTER);
				if (dt.isBefore(LocalDateTime.now())) {
					System.out.println("Время не должно быть в прошлом. Попробуйте снова.");
					continue;
				}
				return dt;
			} catch (Exception ex) {
				System.out.println("Неверный формат. Ожидается yyyy-MM-dd HH:mm. Попробуйте снова.");
			}
		}
	}

	private UUID promptUuid(Scanner scanner) {
		while (true) {
			System.out.print("ID записи (UUID): ");
			String value = scanner.nextLine();
			try {
				return UUID.fromString(value.trim());
			} catch (Exception ex) {
				System.out.println("Неверный UUID. Попробуйте снова.");
			}
		}
	}

	private String keepOrNew(String input, String current) {
		if (input == null || input.trim().isEmpty()) {
			return current;
		}
		return input.trim();
	}

	private String blankToNull(String s) {
		return (s == null || s.trim().isEmpty()) ? null : s.trim();
	}
}
