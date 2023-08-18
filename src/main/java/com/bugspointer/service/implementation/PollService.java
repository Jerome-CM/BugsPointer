package com.bugspointer.service.implementation;

import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Poll;
import com.bugspointer.repository.PollRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class PollService {

    private final PollRepository pollRepository;


    public PollService(PollRepository pollRepository) {
        this.pollRepository = pollRepository;
    }

    public Response savePoll(Poll poll){
        if(poll.getProvidedBy().equals("user")){
            if(poll.getFindEasy() != null && poll.getStepClarity() != null && poll.getTargetFeatureGoodWork() != null){
                try{
                    pollRepository.save(poll);
                    return new Response(EnumStatus.OK, null, "Merci de votre soutient. Nous allons prendre en compte vos remarques pour notre prochaine version");
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

        List<Poll> allPoll = (List<Poll>) pollRepository.findAll();

        int totalPoll = allPoll.size();

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
}
