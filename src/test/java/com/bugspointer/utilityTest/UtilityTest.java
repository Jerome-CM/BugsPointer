package com.bugspointer.utilityTest;

import com.bugspointer.utility.Utility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@SpringBootTest
public class UtilityTest {
    @Autowired
    private Utility utility;

    @Test
    public void createPublicKeyTest(){
        String key = utility.createPublicKey(25);
        assertTrue("", key.length()==25);
    }

    @Test
    public void dateFormatorTest(){
        assertEquals("", "30/06/2000", Utility.dateFormator(new Date("06/30/2000"), "dd/MM/yyyy"));
    }
}
