package com.sdat_s4_sprint_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;

/**
 * Configures the application's DataSource by pulling credentials
 * from AWS Secrets Manager through AwsSecretsService.
 */
@Configuration
public class DataSourceConfig {

    private final AwsSecretsService awsSecretsService;

    public DataSourceConfig(AwsSecretsService awsSecretsService) {
        this.awsSecretsService = awsSecretsService;
    }

    @Bean
    public DataSource dataSource() {
        AwsSecretsService.DbCredentials creds = awsSecretsService.getDbCredentials();

        return DataSourceBuilder.create()
                .url(creds.jdbcUrl())
                .username(creds.username())
                .password(creds.password())
                .build();
    }
}

