package com.example.demo.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
 // 최상단 서비스 클래스 연동 추가
import com.example.demo.model.domain.TestDB;
import com.example.demo.model.service.TestService; // 최상단 서비스 클래스 연동 추가



@Controller
public class DemoController {

    @Autowired
    TestService testService; // DemoController 클래스 아래 객체 주입

    @GetMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("data", " 반갑습니다.");
        return "hello";
    }

    @GetMapping("/testdb")
    public String getAllTestDBs(Model model) {
        java.util.List<TestDB> users = testService.findAll();

        if (users.isEmpty()) {
            TestDB test = new TestDB();
            test.setName("홍길동");
            testService.save(test);
            users = testService.findAll();
        }

        model.addAttribute("users", users);
        System.out.println("데이터 출력 디버그 : " + users);
        return "testdb";
    }
}