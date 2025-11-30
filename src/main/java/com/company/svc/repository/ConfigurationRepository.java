package com.company.svc.repository;

import com.company.svc.model.Configuration;
import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

@Registered
public interface ConfigurationRepository extends JpaRepository<Configuration, UUID> {

    List<Configuration> findByApplicationAndEnvironment(String application, String environment);

    List<Configuration> findByApplicationAndEnvironmentAndName(String application, String environment, String name);

    List<Configuration> findAllByName(String name);
}
