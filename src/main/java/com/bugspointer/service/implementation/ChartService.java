package com.bugspointer.service.implementation;

import com.bugspointer.configuration.CustomExceptions;
import com.bugspointer.dto.ChartResponse;
import com.bugspointer.dto.Dataset;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.EnumViewCounterPage;
import com.bugspointer.entity.ViewCounter;
import com.bugspointer.repository.CompanyRepository;
import com.bugspointer.repository.ViewCounterRepository;
import com.bugspointer.utility.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
public class ChartService {

    private final ViewCounterRepository viewCounterRepository;

    private final CompanyRepository companyRepository;

    public ChartService(ViewCounterRepository viewCounterRepository, CompanyRepository companyRepository) {
        this.viewCounterRepository = viewCounterRepository;
        this.companyRepository = companyRepository;
    }

    /**
     * Get all visits in counter table
     * @param pasteDate
     * @param actualDate
     * @return
     * @throws ParseException
     */
    private HashMap<EnumViewCounterPage, List<ViewCounter>> getAllVisitsFromLastestXDays(Date pasteDate, Date actualDate) throws ParseException {

        //Requête sql
        List<ViewCounter> counterList = viewCounterRepository.findViewCountersByDateViewBetween(pasteDate,actualDate);

        // Une liste par page à surveiller
        List<ViewCounter> dataForChartIndex = new ArrayList<>();
        List<ViewCounter> dataForChartTestpage = new ArrayList<>();
        List<ViewCounter> dataForChartNewUser = new ArrayList<>();
        List<ViewCounter> dataForChartPollUser = new ArrayList<>();
        List<ViewCounter> dataForChartPollCompany = new ArrayList<>();
        List<ViewCounter> dataForChartDownload = new ArrayList<>();

        // Un case par page
        for (ViewCounter view : counterList) {
            switch(view.getPage()){
                case INDEX: dataForChartIndex.add(view);
                break;
                case TESTPAGE: dataForChartTestpage.add(view);
                break;
                case NEWUSER: dataForChartNewUser.add(view);
                break;
                case POLLUSER: dataForChartPollUser.add(view);
                break;
                case POLLCOMPANY: dataForChartPollCompany.add(view);
                break;
                case DOWNLOAD: dataForChartDownload.add(view);
                break;
            }
        }

        // On formate pour le return
        HashMap<EnumViewCounterPage, List<ViewCounter>> allDataResponse = new HashMap<>();

        allDataResponse.put(EnumViewCounterPage.INDEX,dataForChartIndex);
        allDataResponse.put(EnumViewCounterPage.TESTPAGE,dataForChartTestpage);
        allDataResponse.put(EnumViewCounterPage.NEWUSER,dataForChartNewUser);
        allDataResponse.put(EnumViewCounterPage.POLLUSER,dataForChartPollUser);
        allDataResponse.put(EnumViewCounterPage.POLLCOMPANY,dataForChartPollCompany);
        allDataResponse.put(EnumViewCounterPage.DOWNLOAD,dataForChartDownload);

        return allDataResponse;

    }

    /**
     * Formate day for visits label
     * @param dateToStart
     * @param dayBeforeInit
     * @return
     */
    private List<String> getDayLabels(Date dateToStart, int dayBeforeInit){

        List<String> labelsForXaxis = new ArrayList<>();
        List<String> dateOfProvisoryLabels = new ArrayList<>();

        // Date actuelle = dateToStart
        LocalDate localStartDate = dateToStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Format pour les objets LocalDate
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (int i = dayBeforeInit; i >= 0; i--){
            int dayToSubstract = dayBeforeInit - i;
            if(dayToSubstract == 0){
                dateOfProvisoryLabels.add(dateFormat.format(Date.from(localStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant())));
            } else {
                // Retirer x jours = dayBefore
                LocalDate beforeDays = localStartDate.minusDays(dayToSubstract);
                dateOfProvisoryLabels.add(dateFormat.format(Date.from(beforeDays.atStartOfDay(ZoneId.systemDefault()).toInstant())));
            }
        }

        Collections.sort(dateOfProvisoryLabels, (dateStr1, dateStr2) -> {
            try {
                Date date1 = dateFormat.parse(dateStr1);
                Date date2 = dateFormat.parse(dateStr2);
                return date1.compareTo(date2);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        });

        for (String label : dateOfProvisoryLabels){
            label = label.substring(5);
            String[] parts = label.split("-");

            int month = Integer.parseInt(parts[0]);
            int day = Integer.parseInt(parts[1]);

            String labelFinally = day + "/" + month;
            labelsForXaxis.add(labelFinally);
        }

        return labelsForXaxis;
    }

    /**
     * Get values for visits chart
     * @param counterList
     * @param labelsForXaxis
     * @param nameOfData
     * @return
     */
    private Dataset getValuesForVisitsChart(List<ViewCounter> counterList, List<String> labelsForXaxis, String nameOfData){
        
        Dataset dataset = new Dataset();
        List<Integer> values = new ArrayList<>();
        dataset.setNameOfData(nameOfData);

        // Ajouter les data dans l'ordre de l'axe X


        for( String label : labelsForXaxis){

            try {
                LocalDate localDateForLabel = Utility.dateFormatToLocalDate(null, label,"dd/MM");

                Integer nbrVisit = 0;

                for( ViewCounter counter : counterList){

                    // LocalDate pour la date du ViewCounter

                    LocalDate localDateForViewCounter = Utility.dateFormatToLocalDate(counter.getDateView(), null, "yyyy-MM-dd");

                    // Match de la date
                    if(localDateForViewCounter.equals(localDateForLabel)){
                        nbrVisit++;
                    }
                }

                values.add(nbrVisit);

            } catch (ParseException e) {
                e.printStackTrace();
            } catch (CustomExceptions.GetLocalDateException e) {
                throw new RuntimeException(e);
            }
        }
        dataset.setValue(values);

        return dataset;
    }

    /**
     * Get start and paste in Date format
     * @param daysToSubtract
     * @return Date
     * @throws ParseException
     */
    private Map<String, Date> getRangeDate(int daysToSubtract) throws ParseException {
        // Date actuelle
        LocalDate localStartDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Retirer X jours
        LocalDate pastXDays = localStartDate.minusDays(daysToSubtract);

        // Formatter pour les objets LocalDate pour Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String pasteDateString = dateFormat.format(Date.from(pastXDays.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        Date actualDate = new Date();

        Date pasteDate = new SimpleDateFormat("yyyy-MM-dd").parse(pasteDateString);

        Map<String,Date> rangeDate = new HashMap<>();
        rangeDate.put("pasteDate", pasteDate);
        rangeDate.put("actualDate",actualDate);

        return rangeDate;

    }

    /**
     * ChartResponse constructor for a metrics page
     * @param daysToSubtract
     * @return
     * @throws ParseException
     */
    public ChartResponse getDataForViewForLastestXdaysForVisits(int daysToSubtract) throws ParseException {
        Map<String,Date> rangeDate = getRangeDate(daysToSubtract);

        HashMap<EnumViewCounterPage, List<ViewCounter>> allDataResponse = getAllVisitsFromLastestXDays(rangeDate.get("pasteDate"),rangeDate.get("actualDate"));
        List<ViewCounter> dataForChartIndex = allDataResponse.get(EnumViewCounterPage.INDEX);
        List<ViewCounter> dataForChartTestpage = allDataResponse.get(EnumViewCounterPage.TESTPAGE);
        List<ViewCounter> dataForChartNewuser = allDataResponse.get(EnumViewCounterPage.NEWUSER);
        List<ViewCounter> dataForChartPolluser = allDataResponse.get(EnumViewCounterPage.POLLUSER);
        List<ViewCounter> dataForChartPollcompany = allDataResponse.get(EnumViewCounterPage.POLLCOMPANY);
        List<ViewCounter> dataForChartDownload = allDataResponse.get(EnumViewCounterPage.DOWNLOAD);

        List<String> labelsForXaxis = getDayLabels(rangeDate.get("actualDate"), daysToSubtract);
        List<Dataset> datasets = new ArrayList<>();

        Dataset datasetIndex = getValuesForVisitsChart(dataForChartIndex, labelsForXaxis, "Index");
        Dataset datasetTestpage = getValuesForVisitsChart(dataForChartTestpage, labelsForXaxis, "TestPage");
        Dataset datasetNewuser = getValuesForVisitsChart(dataForChartNewuser, labelsForXaxis, "NewUser");
        Dataset datasetPolluser = getValuesForVisitsChart(dataForChartPolluser, labelsForXaxis, "PollUser");
        Dataset datasetPollcompany = getValuesForVisitsChart(dataForChartPollcompany, labelsForXaxis, "PollCompany");
        Dataset datasetDownload = getValuesForVisitsChart(dataForChartDownload, labelsForXaxis, "Download");

        datasets.add(datasetIndex);
        datasets.add(datasetTestpage);
        datasets.add(datasetNewuser);
        datasets.add(datasetPolluser);
        datasets.add(datasetPollcompany);
        datasets.add(datasetDownload);

        ChartResponse chartResponse = new ChartResponse();

        chartResponse.setChartName("Index");
        chartResponse.setLabels(labelsForXaxis);
        chartResponse.setDatasets(datasets);

        return chartResponse;
    }

    /**
     *
     * @param pasteDate
     * @param actualDate
     * @return
     */
    private List<Company> getAllCompaniesFromLastestXDays(Date pasteDate, Date actualDate){

        List<Company> companiesList = companyRepository.findCompaniesByDateCreationBetween(pasteDate, actualDate);

        return companiesList;
    }

    /**
     * Dataset for new users
     * @param daysToSubtract
     * @param nameOfData
     * @param companies
     * @param labelsForXaxis
     * @return
     * @throws ParseException
     */
    public Dataset getValuesForNewUsers(int daysToSubtract, String nameOfData, List<Company> companies, List<String> labelsForXaxis) throws ParseException {
        Map<String,Date> rangeDate = getRangeDate(daysToSubtract);

        Dataset dataset = new Dataset();

        dataset.setNameOfData(nameOfData);

        List<Integer> values = new ArrayList<>();


        for( String label : labelsForXaxis){

            try {
                LocalDate localDateForLabel = Utility.dateFormatToLocalDate(null, label,"dd/MM");

                Integer nbrNewUser = 0;

                for( Company company : companies){

                    // LocalDate pour la date du NewUser

                    LocalDate localDateForNewUser = Utility.dateFormatToLocalDate(company.getDateCreation(), null, "yyyy-MM-dd HH:mm:ss");

                    // Match de la date
                    if(localDateForNewUser.equals(localDateForLabel)){
                        nbrNewUser++;
                    }
                }

                values.add(nbrNewUser);

            } catch (ParseException e) {
                e.printStackTrace();
            } catch (CustomExceptions.GetLocalDateException e) {
                throw new RuntimeException(e);
            }
        }
        dataset.setValue(values);





        return dataset;
    }

    /**
     * Dataset for all users
     * @param daysToSubtract
     * @param nameOfData
     * @param companies
     * @param labelsForXaxis
     * @return
     * @throws ParseException
     */
    public Dataset getValuesForAllUsers(int daysToSubtract, String nameOfData, List<Company> companies, List<String> labelsForXaxis) throws ParseException {
        Map<String,Date> rangeDate = getRangeDate(daysToSubtract);

        Dataset dataset = new Dataset();

        dataset.setNameOfData(nameOfData);

        List<Integer> values = new ArrayList<>();

        for( String label : labelsForXaxis){

            try {
                LocalDate localDateForLabel = Utility.dateFormatToLocalDate(null, label,"dd/MM");

                Integer nbrCompanies = 0;

                for( Company company : companies){

                    // LocalDate pour la date du DateCreation
                    LocalDate localDateForDateCreation = Utility.dateFormatToLocalDate(company.getDateCreation(), null, "yyyy-MM-dd HH:mm:ss");

                    // Match de la date
                    if(localDateForDateCreation.equals(localDateForLabel) || localDateForDateCreation.isBefore(localDateForLabel)){
                        nbrCompanies++;
                    }
                }

                values.add(nbrCompanies);

            } catch (ParseException e) {
                e.printStackTrace();
            } catch (CustomExceptions.GetLocalDateException e) {
                throw new RuntimeException(e);
            }
        }
        dataset.setValue(values);





        return dataset;
    }
    public ChartResponse getDataForViewForLastestXdaysForUsers(int daysToSubtract) throws ParseException {
        Map<String,Date> rangeDate = getRangeDate(daysToSubtract);

        List<Company> allDataResponse = getAllCompaniesFromLastestXDays(rangeDate.get("pasteDate"),rangeDate.get("actualDate"));

        List<String> labelsForXaxis = getDayLabels(rangeDate.get("actualDate"), daysToSubtract);
        List<Dataset> datasets = new ArrayList<>();

        Dataset datasetNewUsers = getValuesForNewUsers(daysToSubtract, "New",allDataResponse, labelsForXaxis);
        Dataset datasetAllUsers = getValuesForAllUsers(daysToSubtract, "All",allDataResponse, labelsForXaxis);


        datasets.add(datasetNewUsers);
        datasets.add(datasetAllUsers);

        ChartResponse chartResponse = new ChartResponse();

        chartResponse.setChartName("User");
        chartResponse.setLabels(labelsForXaxis);
        chartResponse.setDatasets(datasets);

        return chartResponse;
    }


    /* ********* *\
    *    Notice   *
    \* ********* */
    
    /*
    Créer une methode pour avoir les données de la base
    Créer une methode pour un Dataset
    Créer une methode pour le ChartResponse ( une methode ChartResponse par graph ) et faire appel au Dataset ( un dataset / ligne sur le graph )
    en fournissant les données, les labels, le nom du graphique et le daysToSubtract)
    */
}
