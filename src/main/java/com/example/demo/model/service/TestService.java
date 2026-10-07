package com.example.demo.model.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.model.domain.TestDB;
import com.example.demo.model.repository.TestRepository;

@Service
public class TestService {
    @Autowired // PDF 23쪽: 객체 의존성 주입
    private TestRepository testRepository;

    public List<TestDB> findAll() {
        return testRepository.findAll();
    }

    public TestDB findByName(String name) {
        return testRepository.findByName(name);
    }

}
