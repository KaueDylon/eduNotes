package com.univillePOO.eduNotes.repository;

import com.univillePOO.eduNotes.entity.Study;
import com.univillePOO.eduNotes.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyRepository
        extends JpaRepository<Study, Long> {

    List<Study> findByUser(User user);

    Optional<Study> findByIdAndUser(
            Long id,
            User user
    );
}