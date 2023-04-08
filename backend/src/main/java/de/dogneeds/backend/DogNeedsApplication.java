package de.dogneeds.backend;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.IOException;

@SpringBootApplication
public class DogNeedsApplication {

    /**
     * Start main spring application
     *
     * @param args start arguments
     */
    public static void main(String[] args) throws IOException {
        SpringApplication.run(DogNeedsApplication.class, args);

        // initialize firebase

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(new FileInputStream(ResourceUtils.getFile("classpath:firebase.json"))))
                .setStorageBucket("{storageBucket}")
                .build();
        FirebaseApp.initializeApp(options);
    }

}
