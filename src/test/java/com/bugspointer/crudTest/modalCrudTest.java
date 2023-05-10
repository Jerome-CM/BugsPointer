package com.bugspointer.crudTest;

import com.bugspointer.entity.Bug;
import com.bugspointer.entity.Company;
import com.bugspointer.repository.BugRepository;
import com.bugspointer.repository.CompanyRepository;
import com.bugspointer.service.IModal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@Profile("test")
@SpringBootTest
@Sql(value = "/data.sql",executionPhase = BEFORE_TEST_METHOD)
public class modalCrudTest {

    @Autowired
    private IModal modal;
    @Autowired
    private BugRepository bugRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    public void saveNewBug(){

        List<Bug> bugListBefore = (List<Bug>) bugRepository.findAll();

        Bug bug = new Bug();
        Company company = companyRepository.findByCompanyName("CompanyTest1").get();
        bug.setCompany(company);
        bug.setUrl("https://www.sitedetest.com/pageErreur");
        bug.setDescription("La vidéo ne se lance pas");

        bug = bugRepository.save(bug);

        List<Bug> bugListAfter = (List<Bug>) bugRepository.findAll();

        assertEquals( bugListBefore.size()+1, bugListAfter.size());

    }

}
