package com.bugspointer.repository;

import com.bugspointer.entity.Poll;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollRepository extends CrudRepository<Poll, Long> {

    List<Poll> findByPollContext(String pollContext);

    List<Poll> findByPollContextOrPollContextIsNull(String pollContext);

}
