package com.bugspointer.controllerTest;

import com.bugspointer.controller.Admin;
import com.bugspointer.controller.Private;
import com.bugspointer.entity.Company;
import com.bugspointer.repository.CompanyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.modelmapper.internal.bytebuddy.matcher.ElementMatchers.is;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static com.bugspointer.CustomSecurityMockMvcRequestPostProcessors.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Profile("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
@Sql(value = "/data.sql",executionPhase = BEFORE_TEST_METHOD)
public class AdminTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Admin adminController;

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    void contextLoads()throws Exception {
        assertThat(adminController).isNotNull();
    }

    /* Logged page */

    @Test
    public void getDashboardAdminWithLoginTest() throws Exception {
        mockMvc.perform(get("/app/admin/dashboard").with(adminValue()))
                .andExpect(status().isOk());
    }

    /*@Test
    public void getAddDAtaWithLoginTest() throws Exception {
        mockMvc.perform(get("/app/admin/addData").with(adminValue()))
                .andExpect(status().isOk());
    }*/

    @Test
    public void getCompaniesListWithLoginTest() throws Exception {
        mockMvc.perform(get("/app/admin/companiesList").with(adminValue()))
                .andExpect(status().isOk());
    }


    @Test
    public void getCompanyDetailsWithLoginTest() throws Exception {
        Company ifCompany = new Company();
        Iterable<Company> companies = companyRepository.findAll();
        for (Company company : companies){
            ifCompany = company;
        }
        mockMvc.perform(get("/app/admin/companyDetails/"+ ifCompany.getCompanyId()).with(adminValue()))
                .andExpect(status().isOk());
    }

    /* Logged same user */

    @Test
    public void getDashboardAdminWithLoginUserTest() throws Exception {
        mockMvc.perform(get("/app/admin/dashboard").with(userValue()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/authentication"));
    }

    @Test
    public void getCompaniesListWithLoginUserTest() throws Exception {
        mockMvc.perform(get("/app/admin/companiesList").with(userValue()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/authentication"));
    }

    @Test
    public void getCompanyDetailsWithLoginUserTest() throws Exception {
        mockMvc.perform(get("/app/admin/companyDetails").with(userValue()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/authentication"));
    }

    /* Unlogged  */

    @Test
    public void getDashboardAdminWithoutLoginTest() throws Exception {
        mockMvc.perform(get("/app/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getCompaniesListWithoutLoginTest() throws Exception {
        mockMvc.perform(get("/app/admin/companiesList"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getCompanyDetailsWithoutLoginTest() throws Exception {
        mockMvc.perform(get("/app/admin/companyDetails"))
                .andExpect(status().isUnauthorized());

    }


    /*@Test
    public void getAddDAtaWithoutLoginTest() throws Exception {
        mockMvc.perform(get("/app/admin/addData"))
                .andExpect(status().isUnauthorized());
    }*/

}
