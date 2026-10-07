package com.example.demo.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.model.domain.TestDB;

@Repository // PDF 22쪽: 리포지토리 등록
public interface TestRepository extends JpaRepository<TestDB, Long> {
    TestDB findByName(String name);
}
