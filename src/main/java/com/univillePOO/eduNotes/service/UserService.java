package com.univillePOO.eduNotes.service;

import com.univillePOO.eduNotes.entity.User;
import com.univillePOO.eduNotes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public User register(User user){
        return repository.save(user);
    }

    public User login(String email,String password){

        return repository.findByEmail(email)
                .filter(u -> u.getPassword().equals(password))
                .orElse(null);
    }
}