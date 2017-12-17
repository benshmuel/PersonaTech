package ServerPackage;

import HandlerPackage.FirebaseHandler;
import HandlerPackage.Welcome;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by benshmuel on 22/11/2017.
 */
public class MainClass {

    private final static String PERSONA_ADMIN_SDK ="persona-tech-firebase-adminsdk-6bqub-4ed83752b0.json";

    public static void main(String[] args) throws FileNotFoundException {


        //working on DB is below //


        System.out.println("Server is Started");


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
