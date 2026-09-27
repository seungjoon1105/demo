package com.example.demo.model.service;

import org.springframework.stereotype.Service;
import com.example.demo.model.domain.TestDB;
import com.example.demo.model.service.repository.TestRepository;

@Service
public class TestService {
    private final TestRepository testRepository;

    public TestService(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    public java.util.List<TestDB> findAll() {
        return testRepository.findAll();
    }

    public TestDB findByName(String name) {
        return testRepository.findByName(name);
    }

    public TestDB save(TestDB testDB) {
        return testRepository.save(testDB);
    }
}