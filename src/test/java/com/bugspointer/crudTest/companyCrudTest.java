package com.bugspointer.crudTest;

import com.bugspointer.dto.AuthRegisterCompanyDTO;
import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Company;
import com.bugspointer.repository.CompanyRepository;
import com.bugspointer.service.ICompany;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@Profile("test")
@SpringBootTest
@Sql(value = "/data.sql",executionPhase = BEFORE_TEST_METHOD)
public class companyCrudTest {

    @Autowired
    private ICompany iCompany;
    @Autowired
    private CompanyRepository companyRepository;

    @Test
    public void saveCompanyTest(){
        List<Company> listCompany = (List<Company>) companyRepository.findAll();
        int countCompany = listCompany.size();
        AuthRegisterCompanyDTO company = new AuthRegisterCompanyDTO();
        company.setCompanyName("Comp3");
        company.setMail("comp3@comp.fr");
        company.setConfirmMail("comp3@comp.fr");
        company.setPassword("test");
        company.setConfirmPassword("test");

        Response response = iCompany.saveCompany(company);
        List<Company> listCompanyAfterSave = (List<Company>) companyRepository.findAll();
        int countCompanyAfterSave = listCompanyAfterSave.size();
        assertEquals(countCompany+1, countCompanyAfterSave);
        assertEquals(response.getStatus(), EnumStatus.OK);
    }
}
