package com.application.user_data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/user-data")
public class UserDataController {
    private final UserDataService userDataService;

    private final static List<String> IMAGE_EXTENSION = List.of("jpeg", "jpg", "gif", "png");

    @Autowired
    public UserDataController(UserDataService userDataService) {
        this.userDataService = userDataService;
    }

    @GetMapping("")
    public UserData getUserData(@RequestParam String username) throws ResponseStatusException {

        return userDataService.getUserData(username);
    }

    @GetMapping("/profile-picture")
    public byte[] getProfilePicture(@RequestParam String username) throws ResponseStatusException {

        return userDataService.getProfilePicture(username);
    }

    @PutMapping("/profile-picture")
    public void updateProfilePicture(MultipartFile file, Principal principal) throws ResponseStatusException, IOException {
        List<String> filename = List.of(file.getOriginalFilename().split("\\."));

        if(filename.size() == 1 || !IMAGE_EXTENSION.contains(filename.get(filename.size() - 1)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile picture supported extension: jpeg, jpg, gif, png");


        if(file.getSize() > 20000.0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile picture size larger than 20KB not supported");


        userDataService.updateProfilePicture(principal.getName(), file);
    }

    @PutMapping("/status-message")
    public void updateStatusMessage(@RequestBody Map<String, Object> body, Principal principal) throws ResponseStatusException{

        userDataService.updateStatusMessage(principal.getName(), body.get("statusMessage").toString());
    }
}
