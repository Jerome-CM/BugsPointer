package com.bugspointer.utilityTest;

import com.bugspointer.utility.Utility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@SpringBootTest
public class UtilityTest {
    @Autowired
    private Utility utility;


    @Test
    public void createPublicKeyTest(){
        String key = utility.createPublicKey();
        System.out.println(key);
        assertTrue("", key.length()==25);

    }
}
