package com.bugspointer.utility;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

        private static final String DOMAINE_PATTERN="^[a-zA-Z0-9]+([.][a-zA-Z0-9]+)*[.][a-zA-Z]{2,6}$";
                                                //accepte monsite.extension ou ssdomaine.monsite.extension
        private static final Pattern pattern = Pattern.compile(DOMAINE_PATTERN);

        public static boolean isValid(final String domaine) {
            Matcher matcher = pattern.matcher(domaine);
            return matcher.matches();
        }
    }

    /**
     *
     * @param dateHandler format "2023-07-25"
     * @param nbr Année à rajouter
     * @return Si +1 => 25/07/2024
     */
    public static String handlerDateForYear(String dateHandler, int nbr){

        // Conversion de la chaîne en LocalDate
        LocalDate date = LocalDate.parse(dateHandler);

        // Formatage de la date en chaîne dans le nouveau format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate datePlusYear = date.plusYears(nbr);
        return datePlusYear.format(formatter);

    }
}
