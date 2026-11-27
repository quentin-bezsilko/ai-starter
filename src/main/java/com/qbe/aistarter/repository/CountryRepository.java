package com.qbe.aistarter.repository;

import com.qbe.aistarter.entity.CountryEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<CountryEntity, Long> {

    Optional<CountryEntity> findByNameIgnoreCase(String name);
}
