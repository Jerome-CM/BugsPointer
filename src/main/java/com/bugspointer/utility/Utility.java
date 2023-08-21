package com.bugspointer.utility;

import com.bugspointer.configuration.CustomExceptions;
import com.bugspointer.entity.HomeLogger;
import com.bugspointer.entity.enumLogger.Action;
import com.bugspointer.entity.enumLogger.Adjective;
import com.bugspointer.entity.enumLogger.Raison;
import com.bugspointer.entity.enumLogger.What;
import com.bugspointer.repository.HomeLoggerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class Utility {

    private static HomeLoggerRepository homeLoggerRepository = null;

    public Utility(HomeLoggerRepository homeLoggerRepository) {
        this.homeLoggerRepository = homeLoggerRepository;
    }


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

    public static void saveLog(Long companyId, Action action, What what, String identifier, Adjective adjective, Raison raison) {

        HomeLogger logging = new HomeLogger();

        logging.setCompanyId(companyId);
        logging.setAction(action);
        logging.setWhat(what);
        logging.setIdentifier(identifier);
        logging.setAdjective(adjective);
        logging.setRaison(raison);

        try{
            homeLoggerRepository.save(logging);
        } catch (Exception e){
            log.error("Error when save a new HomeLogging : {}",e);
        }
    }

    public static String dateFormator(Date date, String format){
        if(date == null){
            return "--";
        } else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            return simpleDateFormat.format(date);
        }
    }

    public static LocalDate dateFormatToLocalDate(Date dateBefore, String dateBeforeOnStringType, String formatOrigin) throws CustomExceptions.GetLocalDateException, ParseException {

        Date date = dateBefore;
        String dateOnStringFormat = dateBeforeOnStringType;

        if(date != null && formatOrigin != null){

            if(formatOrigin.equals("yyyy-MM-dd")){
                String[] arrayTime = String.valueOf(dateBefore).split("-");

                int year = Integer.parseInt(arrayTime[0]);
                int month = Integer.parseInt(arrayTime[1]);
                int day = Integer.parseInt(arrayTime[2]);

                return LocalDate.of(year,month,day);
            }

            if(formatOrigin.equals("yyyy-MM-dd HH:mm:ss")){
                String[] arrayTimeDayHours = String.valueOf(dateBefore).split(" ");

                String[] arrayTime = String.valueOf(arrayTimeDayHours[0]).split("-");

                int year = Integer.parseInt(arrayTime[0]);
                int month = Integer.parseInt(arrayTime[1]);
                int day = Integer.parseInt(arrayTime[2]);

                return LocalDate.of(year,month,day);
            }

        } else if(dateOnStringFormat != null && formatOrigin != null){
            if(formatOrigin.equals("dd/MM")){
                SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd/MM");
                return LocalDate.of(LocalDate.now().getYear(), inputDateFormat.parse(dateOnStringFormat).getMonth() + 1, inputDateFormat.parse(dateOnStringFormat).getDate());
            }

        } else{
            throw new CustomExceptions.GetLocalDateException("Erreur liée à la date");
        }

        return null;

    }
}
