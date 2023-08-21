package com.application.registration;

import com.application.user.User;
import com.application.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegisterService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public RegisterService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    /**
     * INSERT user into database
     */
    public User save(String username, String password) {
        // password should be encoded in database
        String encodedPassword = bCryptPasswordEncoder
                .encode(password);
        return userRepository.save(new User(username, encodedPassword));
    }
}
