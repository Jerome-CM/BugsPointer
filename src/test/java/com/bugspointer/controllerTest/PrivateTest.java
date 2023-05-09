package com.bugspointer.controllerTest;

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
public class PrivateTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Private privateController;

    @Test
    void contextLoads()throws Exception {
        assertThat(privateController).isNotNull();
    }

    /* Logged page */

    @Test
    public void getDashboardCompanyWithLoginTest() throws Exception {
        mockMvc.perform(get("/app/private/dashboard").with(userValue()))
                .andExpect(status().isOk());
    }

    @Test
    public void getAccountUserWithLoginTest() throws Exception {
        mockMvc.perform(get("/app/private/account").with(userValue()))
                .andExpect(status().isOk());
    }

    /*@Test
    public void getNotificationUserWithLoginTest() throws Exception {
        mockMvc.perform(get("/app/private/notifications").with(userValue()))
                .andExpect(status().isOk());
    }*/

    /*@Test
    public void getInvoicesWithLoginTest() throws Exception {
        mockMvc.perform(get("/app/private/invoices").with(userValue()))
                .andExpect(status().isOk());
    }*/

    /*@Test
    public void getNewBugListWithLoginTest() throws Exception {
        mockMvc.perform(get("/app/private/newBugList").with(userValue()))
                .andExpect(status().isOk());
    }*/

    /*@Test
    public void getNewBugReportWithLoginTest() throws Exception {
        mockMvc.perform(get("/app/private/newBugReport").with(userValue()))
                .andExpect(status().isOk());
    }*/

   /* @Test
    public void getPendingBugListWithLoginTest() throws Exception {
        mockMvc.perform(get("/app/private/pendingBugList").with(userValue()))
                .andExpect(status().isOk());
    }*/

    /*@Test
    public void getPendingBugReportWithLoginTest() throws Exception {
        mockMvc.perform(get("/app/private/pendingBugReport").with(userValue()))
                .andExpect(status().isOk());
    }*/

    /*@Test
    public void getSolvedBugListWithLoginTest() throws Exception {
        mockMvc.perform(get("/app/private/solvedBugList").with(userValue()))
                .andExpect(status().isOk());
    }*/

    /*@Test
    public void getSolvedBugReportWithLoginTest() throws Exception {
        mockMvc.perform(get("/app/private/solvedBugReport").with(userValue()))
                .andExpect(status().isOk());
    }*/

    /* Unlogged page */

    @Test
    public void getDashboardCompanyWithoutLoginTest() throws Exception {
        mockMvc.perform(get("/app/private/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getAccountCompanyWithoutLoginTest() throws Exception {
        mockMvc.perform(get("/app/private/account"))
                .andExpect(status().isUnauthorized());
    }

    /*@Test
    public void getNotificationCompanyWithoutLoginTest() throws Exception {
        mockMvc.perform(get("/app/private/notifications"))
                .andExpect(status().isUnauthorized());
    }*/

    /*@Test
    public void getInvoicesWithoutLoginTest() throws Exception {
        mockMvc.perform(get("/app/private/invoices"))
                .andExpect(status().isUnauthorized());
    }*/

    /*@Test
    public void getNewBugListWithoutLoginTest() throws Exception {
        mockMvc.perform(get("/app/private/newBugList"))
                .andExpect(status().isUnauthorized());
    }*/

    /*@Test
    public void getNewBugReportWithoutLoginTest() throws Exception {
        mockMvc.perform(get("/app/private/newBugReport"))
                .andExpect(status().isUnauthorized());
    }*/

   /* @Test
    public void getPendingBugListWithoutLoginTest() throws Exception {
        mockMvc.perform(get("/app/private/pendingBugList"))
                .andExpect(status().isUnauthorized());
    }*/

    /*@Test
    public void getPendingBugReportWithoutLoginTest() throws Exception {
        mockMvc.perform(get("/app/private/pendingBugReport"))
                .andExpect(status().isUnauthorized());
    }*/

    /*@Test
    public void getSolvedBugListWithoutLoginTest() throws Exception {
        mockMvc.perform(get("/app/private/solvedBugList"))
                .andExpect(status().isUnauthorized());
    }*/

    /*@Test
    public void getSolvedBugReportWithoutLoginTest() throws Exception {
        mockMvc.perform(get("/app/private/solvedBugReport"))
                .andExpect(status().isUnauthorized());
    }*/


}
