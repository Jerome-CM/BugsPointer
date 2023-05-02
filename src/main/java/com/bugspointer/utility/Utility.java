package com.bugspointer.utility;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class Utility {

    public String createPublicKey() {
        String[] chars = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
                "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c",
                "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
                "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

        StringBuilder publicKey = new StringBuilder();
        Random rand = new Random();

        for (int i = 0; i < 25; i++) {
            int index = rand.nextInt(chars.length);
            publicKey.append(chars[index]);
        }

        return publicKey.toString();
    }
}
