package com.application.recovery;

import com.application.user.User;
import com.application.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RecoveryService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public RecoveryService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Transactional
    public void resetPassword(String username, String password) {
        // password should be encoded in database
        String encodedPassword = bCryptPasswordEncoder
                .encode(password);
        userRepository.updatePassword(username, encodedPassword);
    }
}
