package com.bugspointer.controllerTest;

import com.bugspointer.controller.Home;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Profile("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
public class HomeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Home homeController;

    @Test
    void contextLoads()throws Exception {
        assertThat(homeController).isNotNull();
    }

    @Test
    public void getHomeTest() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    public void getfeaturesTest() throws Exception {
        mockMvc.perform(get("/features"))
                .andExpect(status().isOk());
    }

    @Test
    public void getDocumentationsTest() throws Exception {
        mockMvc.perform(get("/documentations"))
                .andExpect(status().isOk());
    }

    @Test
    public void getModalTest() throws Exception {
        mockMvc.perform(get("/modal"))
                .andExpect(status().isOk());
    }

}
