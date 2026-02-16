package com.hrms.hrm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "file.storage=local",
        "file.upload.dir=uploads/"
})
class HrmApplicationTests {

	@Test
	void contextLoads() {
	}

}
