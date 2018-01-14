package ServerPackage;

import HandlerPackage.FirebaseHandler;
import HandlerPackage.Welcome;
import org.apache.log4j.BasicConfigurator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by benshmuel on 22/11/2017.
 */
public class MainClass {

    private final static String PERSONA_ADMIN_SDK ="persona-tech-firebase-adminsdk-6bqub-4ed83752b0.json";

    public static void main(String[] args) throws FileNotFoundException {


        //working on DB is below //

        //BasicConfigurator.configure();
        for(int i=0;i<20;i++)
            System.out.println("\n");


        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        System.out.println("**************************************************************");
        System.out.println("**************************************************************");
        System.out.println("Persona-Tech 2018 \u00AE");
        System.out.println("Server is Started");
        System.out.println("Current Server Time : " + sdf.format(cal.getTime()));
        System.out.println("**************************************************************");
        System.out.println("**************************************************************");


        FirebaseHandler firebaseHandler = new FirebaseHandler();


        // starting to listen to clients //

        try(ServerSocket serverSocket = new ServerSocket(8080)) {
            while(true) {
                new Welcome(serverSocket.accept(),firebaseHandler).start();
            }


        } catch(IOException e) {
            System.out.println("Server exception " + e.getMessage());
        }
    }


}
