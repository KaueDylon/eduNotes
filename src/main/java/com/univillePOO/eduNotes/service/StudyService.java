package com.univillePOO.eduNotes.service;

import com.univillePOO.eduNotes.entity.Study;
import com.univillePOO.eduNotes.entity.User;
import com.univillePOO.eduNotes.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository repository;

    public Study save(Study study){
        return repository.save(study);
    }

    public List<Study> findByUser(User user){
        return repository.findByUser(user);
    }

    public Study findById(Long id){
        return repository.findById(id)
                .orElseThrow();
    }

    public Study findByIdAndUser(
            Long id,
            User user){

        return repository
                .findByIdAndUser(id, user)
                .orElseThrow();
    }
}