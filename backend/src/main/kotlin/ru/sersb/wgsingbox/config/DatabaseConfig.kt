package ru.sersb.wgsingbox.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackages = ["ru.sersb.wgsingbox.repository"])
class DatabaseConfig
