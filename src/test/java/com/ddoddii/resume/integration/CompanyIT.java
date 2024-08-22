package com.ddoddii.resume.integration;

import com.ddoddii.resume.dto.company.CompanyNameDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

@Sql(executionPhase = BEFORE_TEST_CLASS,scripts = {"classpath:sql/company_name.sql", "classpath:sql/company_dept.sql", "classpath:sql/company_job.sql"})
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompanyIT {
    private  static final String MYSQL_IMAGE = "mysql:8.0.39";

    @Container
    static JdbcDatabaseContainer MY_SQL_CONTAINER = new MySQLContainer(MYSQL_IMAGE)
            .withDatabaseName("resume-plus");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MY_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MY_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MY_SQL_CONTAINER::getPassword);
    }

    @Autowired
    private TestRestTemplate testRestTemplate;

    @DisplayName("성공: 회사 이름 리스팅")
    @Test
    void testCompanyName(){
        CompanyNameDTO[] companyNameDTOS = testRestTemplate.getForObject("/api/company/name", CompanyNameDTO[].class );
        List<CompanyNameDTO> list = Arrays.asList(companyNameDTOS);
        Assertions.assertEquals(213, list.size());
    }

    @DisplayName("성공: 회사 부서 리스팅")
    @Test
    void testCompanyDept(){
        CompanyNameDTO[] companyNameDTOS = testRestTemplate.getForObject("/api/company/department", CompanyNameDTO[].class );
        List<CompanyNameDTO> list = Arrays.asList(companyNameDTOS);
        Assertions.assertEquals(18, list.size());
    }

    @DisplayName("성공: 회사 직무 리스팅")
    @Test
    void testCompanyJob(){
        CompanyNameDTO[] companyNameDTOS = testRestTemplate.getForObject("/api/company/job", CompanyNameDTO[].class );
        List<CompanyNameDTO> list = Arrays.asList(companyNameDTOS);
        Assertions.assertEquals(406, list.size());
    }


}
