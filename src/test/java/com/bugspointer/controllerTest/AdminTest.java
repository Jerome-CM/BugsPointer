package com.bugspointer.controllerTest;

import com.bugspointer.controller.Admin;
import com.bugspointer.controller.Private;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.modelmapper.internal.bytebuddy.matcher.ElementMatchers.is;
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
public class AdminTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Admin adminController;

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

    /*@Test
    public void getCompaniesListWithLoginTest() throws Exception {
        mockMvc.perform(get("/app/admin/companiesList").with(adminValue()))
                .andExpect(status().isOk());
    }*/

    /* Logged same user */

    @Test
    public void getDashboardAdminWithLoginUserTest() throws Exception {
        mockMvc.perform(get("/app/admin/dashboard").with(userValue()))
                .andExpect(status().isForbidden());
    }

    /*@Test
    public void getAddDAtaWithLoginUserTest() throws Exception {
        mockMvc.perform(get("/app/admin/addData").with(userValue()))
                .andExpect(status().isForbidden());
    }*/

    /*@Test
    public void getCompaniesListWithLoginUserTest() throws Exception {
        mockMvc.perform(get("/app/admin/companiesList").with(userValue()))
                .andExpect(status().isForbidden());
    }*/


    /* Unlogged  */


     @Test
    public void getDashboardAdminWithoutLoginTest() throws Exception {
        mockMvc.perform(get("/app/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    /*@Test
    public void getAddDAtaWithoutLoginTest() throws Exception {
        mockMvc.perform(get("/app/admin/addData"))
                .andExpect(status().isUnauthorized());
    }*/

    /*@Test
    public void getCompaniesListWithoutLoginTest() throws Exception {
        mockMvc.perform(get("/app/admin/companiesList"))
                .andExpect(status().isUnauthorized());
    }*/

}
