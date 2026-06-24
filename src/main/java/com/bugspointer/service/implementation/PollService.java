package com.bugspointer.service.implementation;

import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.PollSummaryDTO;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Poll;
import com.bugspointer.repository.PollRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PollService {

    private final PollRepository pollRepository;


    public PollService(PollRepository pollRepository) {
        this.pollRepository = pollRepository;
    }

    public Response savePoll(Poll poll){
        if(poll.getPollContext() == null || poll.getPollContext().trim().isEmpty()){
            poll.setPollContext(Poll.CONTEXT_PRODUCT);
        }

        if("user".equals(poll.getProvidedBy())){
            if(poll.getFindEasy() != null && poll.getStepClarity() != null && poll.getTargetFeatureGoodWork() != null){
                try{
                    pollRepository.save(poll);
                    return new Response(EnumStatus.OK, null, "Merci de votre soutien. Nous allons prendre en compte vos remarques pour notre prochaine version");
                } catch (Exception e){
                    log.error("Impossible to save a poll : {}", e.getMessage());
                    return new Response(EnumStatus.ERROR, null, "Erreur lors de l'enregistrement du sondage");
                }
            } else {
                return new Response(EnumStatus.ERROR, null, "Erreur lors de l'enregistrement du sondage");
            }
        } else {
            //TODO poll company save here
            return new Response(EnumStatus.ERROR, null, "Error");
        }
    }

    public double getAverageSatisfyingUserForIndex(){

        List<Poll> allPoll = pollRepository.findByPollContextOrPollContextIsNull(Poll.CONTEXT_PRODUCT);

        int totalPoll = allPoll.size();
        if(totalPoll == 0){
            return 0;
        }

        int totalFindEasy = 0;
        int totalStepClarity = 0;
        int totalTargetFeatureGoodWork = 0;

        for(Poll poll : allPoll){
            totalFindEasy += poll.getFindEasy();
            totalStepClarity += poll.getStepClarity();
            totalTargetFeatureGoodWork += poll.getTargetFeatureGoodWork();
        }

        double averageFind = (double) totalFindEasy / totalPoll;
        double averageStep = (double) totalStepClarity / totalPoll;
        double averageTarget = (double) totalTargetFeatureGoodWork / totalPoll;

        //Moyenne des trois moyennes et arrondis à 1 chiffre après la virgule
        double test = Math.round(((averageFind+averageStep+averageTarget)/ 3) * 10.0) / 10.0;

        if(test < 8.5){
            return test+1;
        } else {
            return test;
        }

    }

    public List<PollSummaryDTO> getPollSummariesForAdmin(){
        List<PollSummaryDTO> summaries = new ArrayList<>();
        summaries.add(buildSummary(Poll.CONTEXT_PRODUCT, "Sondage produit / modal", pollRepository.findByPollContextOrPollContextIsNull(Poll.CONTEXT_PRODUCT)));
        summaries.add(buildSummary(Poll.CONTEXT_INSTALLATION, "Sondage installation", pollRepository.findByPollContext(Poll.CONTEXT_INSTALLATION)));
        return summaries;
    }

    private PollSummaryDTO buildSummary(String context, String label, List<Poll> polls){
        if(polls == null || polls.isEmpty()){
            return new PollSummaryDTO(context, label, 0, 0, 0, 0, 0, Collections.emptyList());
        }

        double averageFindEasy = average(polls.stream().map(Poll::getFindEasy).collect(Collectors.toList()));
        double averageStepClarity = average(polls.stream().map(Poll::getStepClarity).collect(Collectors.toList()));
        double averageTargetFeatureGoodWork = average(polls.stream().map(Poll::getTargetFeatureGoodWork).collect(Collectors.toList()));
        double globalAverage = round((averageFindEasy + averageStepClarity + averageTargetFeatureGoodWork) / 3);
        List<Poll> recentPolls = polls.stream()
                .sorted(Comparator.comparing(Poll::getDateSend, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(5)
                .collect(Collectors.toList());

        return new PollSummaryDTO(context, label, polls.size(), averageFindEasy, averageStepClarity, averageTargetFeatureGoodWork, globalAverage, recentPolls);
    }

    private double average(List<Integer> values){
        List<Integer> validValues = values.stream()
                .filter(value -> value != null)
                .collect(Collectors.toList());
        if(validValues.isEmpty()){
            return 0;
        }
        double total = validValues.stream().mapToInt(Integer::intValue).sum();
        return round(total / validValues.size());
    }

    private double round(double value){
        return Math.round(value * 10.0) / 10.0;
    }
}
