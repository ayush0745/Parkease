package com.parkease.analytics.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.parkease.analytics.repository")
@EnableTransactionManagement
public class AnalyticsConfig {
}