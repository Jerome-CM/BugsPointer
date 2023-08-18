package com.bugspointer.service.implementation;

import com.bugspointer.entity.EnumViewCounterPage;
import com.bugspointer.entity.ViewCounter;
import com.bugspointer.repository.ViewCounterRepository;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ViewCounterService {

    private final ViewCounterRepository viewCounterRepository;

    public ViewCounterService(ViewCounterRepository viewCounterRepository) {
        this.viewCounterRepository = viewCounterRepository;
    }

    public void addVisit(EnumViewCounterPage page){

        ViewCounter view = new ViewCounter(page, new Date());
        viewCounterRepository.save(view);

    }
}
