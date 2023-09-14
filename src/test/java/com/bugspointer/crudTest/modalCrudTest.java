package com.bugspointer.crudTest;

import com.bugspointer.entity.Bug;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.EnumEtatBug;
import com.bugspointer.repository.BugRepository;
import com.bugspointer.repository.CompanyRepository;
import com.bugspointer.service.IModal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.jdbc.Sql;

import java.util.Date;
import java.util.List;
import java.util.Optional;

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
        Optional<Company> companyOpt = companyRepository.findById(1L);

        if(companyOpt.isPresent()){
            bug.setCompany(companyOpt.get());
            bug.setUrl("https://www.sitedetest.com/pageErreur");
            bug.setDateCreation(new Date());
            bug.setDescription("La vidéo ne se lance pas");
            bug.setEtatBug(EnumEtatBug.NEW);
            bug.setCodeLocation("<div><h1>Mon titre</h1><div>");
            bug.setAdresseIp("192.168.0.1");
            bug.setScreenSize("1920 x 1080");
            bug.setBrowser("Chromium v113");
            bug.setOs("MacOS");

            bug = bugRepository.save(bug);

            List<Bug> bugListAfter = (List<Bug>) bugRepository.findAll();

            assertEquals( bugListBefore.size()+1, bugListAfter.size());
        }


    }

}
