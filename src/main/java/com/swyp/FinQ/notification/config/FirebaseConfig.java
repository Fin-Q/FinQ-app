package com.swyp.FinQ.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(FirebaseProperties.class)
@ConditionalOnProperty(prefix = "firebase", name = "enabled", havingValue = "true")
public class FirebaseConfig {

    private static final String APP_NAME = "finq";

    @Bean(destroyMethod = "delete")
    public FirebaseApp firebaseApp(FirebaseProperties properties) {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(loadCredentials(properties.credentialsBase64()))
                .build();

        return FirebaseApp.initializeApp(options, APP_NAME);
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

    GoogleCredentials loadCredentials(String credentialsBase64) {
        if (credentialsBase64 == null || credentialsBase64.isBlank()) {
            throw new IllegalStateException(
                    "Firebase credentials must be configured when Firebase is enabled"
            );
        }

        try {
            byte[] credentialsJson = Base64.getDecoder().decode(credentialsBase64);
            return GoogleCredentials.fromStream(new ByteArrayInputStream(credentialsJson));
        } catch (IllegalArgumentException | IOException exception) {
            throw new IllegalStateException("Firebase credentials are invalid", exception);
        }
    }
}
