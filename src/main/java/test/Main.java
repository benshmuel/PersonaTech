package test;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import org.apache.log4j.BasicConfigurator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by benshmuel on 14/12/2017.
 */
public class Main {
    private final static String PERSONA_ADMIN_SDK ="persona-tech-firebase-adminsdk-6bqub-4ed83752b0.json";
    private final static String PERSONA_DB_URL = "https://persona-tech.firebaseio.com/";

    public static void main(String[] args) {

        //BasicConfigurator.configure();



        FileInputStream serviceAccount = null;
        try {
            serviceAccount = new FileInputStream(PERSONA_ADMIN_SDK);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        FirebaseOptions options = null;
        try {
            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(PERSONA_DB_URL)
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }

     FirebaseApp.initializeApp(options);


        System.out.println("Hidi");

        new te();



    }



    }
