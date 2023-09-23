package com.application.user_data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_data")
public class UserData {
    @Id
    @Column(name = "username", nullable=false)
    private String username;

    /**
     * S3 object name, UUID
     */
    @Column(name = "profile_picture")
    private String profilePicture;

    @Column(name = "status_message")
    private String statusMessage;

}
