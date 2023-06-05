package com.bugspointer.utility;

import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class Utility {

    public String createPublicKey(int nbCar) {
        String[] chars = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
                "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c",
                "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
                "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

        StringBuilder publicKey = new StringBuilder();
        Random rand = new Random();

        for (int i = 0; i < nbCar; i++) {
            int index = rand.nextInt(chars.length);
            publicKey.append(chars[index]);
        }

        return publicKey.toString();
    }

    public static class domaineValidate{
        private static final String DOMAINE_PATTERN="^[w]{3}([.][0-9a-zA-Z-_]+)*[.][a-z]{2,6}$";

        private static final Pattern pattern = Pattern.compile(DOMAINE_PATTERN);

        public static boolean isValid(final String domaine) {
            Matcher matcher = pattern.matcher(domaine);
            return matcher.matches();
        }
    }
}
