package com.bugspointer.controllerTest;

import com.bugspointer.controller.ApiFormUser;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Profile("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
public class ApiFormUserTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiFormUser apiFormUserController;

    @Test
    void contextLoads()throws Exception {
        assertThat(apiFormUserController).isNotNull();
    }

    // TODO Mettre en post avec body rempli
    /*@Test
    public void getModalTest() throws Exception {
        mockMvc.perform(get("/modal"))
                .andExpect(status().isOk());
    }*/
}
