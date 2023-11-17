package com.application.registration;

import com.application.user.User;
import com.application.user.UserRepository;
import com.application.user_data.UserData;
import com.application.user_data.UserDataRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class RegisterService {
    private final UserRepository userRepository;
    private final UserDataRepository userDataRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final List<String> defaultProfilePicture = List.of(
            "reddit_avatar_blue.jpg",
            "reddit_avatar_brown.png",
            "reddit_avatar_green.png",
            "reddit_avatar_orange.png",
            "reddit_avatar_pink.png",
            "reddit_avatar_purple.png",
            "trashdove.jpg"
    );

    @Autowired
    public RegisterService(UserRepository userRepository, UserDataRepository userDataRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.userDataRepository = userDataRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Transactional
    public User registerAccount(String username, String password) {
        // password should be encoded in database
        String encodedPassword = bCryptPasswordEncoder
                .encode(password);

        userDataRepository.save(new UserData(username, "profile-picture/default/" + defaultProfilePicture.get(new Random().nextInt(defaultProfilePicture.size())), null));

        return userRepository.save(new User(username, encodedPassword));
    }
}
