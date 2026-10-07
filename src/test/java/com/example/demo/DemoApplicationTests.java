package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@org.springframework.test.context.TestExecutionListeners(listeners = {
    org.springframework.test.context.support.DependencyInjectionTestExecutionListener.class,
    org.springframework.test.context.transaction.TransactionalTestExecutionListener.class
})
@SpringBootTest
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
