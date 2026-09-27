package com.example.demo.model.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.domain.TestDB;

public interface TestRepository extends JpaRepository<TestDB, Long> {
    TestDB findByName(String name);
}
