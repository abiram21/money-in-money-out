package com.transaction;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class TransactionApplicationTests {

	@Autowired
	private TransactionApplication application;

	@Test
	void contextLoads() {
		assertNotNull(application);
	}
}
