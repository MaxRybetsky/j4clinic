package ru.skillbox.data.j4clinic.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

	@Value("${spring.datasource.url}")
	private String jdbcUrl;

	@Value("${spring.datasource.username}")
	private String username;

	@Value("${spring.datasource.password}")
	private String password;

	@Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
	private String driverClassName;

	@Value("${app.datasource.pool.max-size:10}")
	private int maxPoolSize;

	@Value("${app.datasource.pool.min-idle:2}")
	private int minIdle;

	@Value("${app.datasource.pool.idle-timeout-ms:600000}")
	private long idleTimeoutMs;

	@Value("${app.datasource.pool.max-lifetime-ms:1800000}")
	private long maxLifetimeMs;

	@Value("${app.datasource.pool.connection-timeout-ms:30000}")
	private long connectionTimeoutMs;

	@Bean
	public DataSource dataSource() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(jdbcUrl);
		config.setUsername(username);
		config.setPassword(password);
		config.setDriverClassName(driverClassName);
		config.setMaximumPoolSize(maxPoolSize);
		config.setMinimumIdle(minIdle);
		config.setIdleTimeout(idleTimeoutMs);
		config.setMaxLifetime(maxLifetimeMs);
		config.setConnectionTimeout(connectionTimeoutMs);
		config.setPoolName("clinic-app-hikari");
		return new HikariDataSource(config);
	}
}


