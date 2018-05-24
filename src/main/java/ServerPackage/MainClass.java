package ServerPackage;

import HandlerPackage.FirebaseHandler;
import HandlerPackage.Welcome;
import ModulesPackage.Employee;
import ModulesPackage.Test;
import ModulesPackage.TreeDrawingTest;
import org.apache.log4j.BasicConfigurator;

import java.io.*;
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


        try {

            String prg = "import sys";
            String path = "/home/personaitaben/PersonaPyEngine/MainModule/MainEngine.py";
            String args1 = "-o str -i \"https://firebasestorage.googleapis.com/v0/b/photos-40b3a.appspot.com/o/im.jpg?alt=media&token=09bae7d9-e0da-4b73-a1b0-47f5a9766a4b\"";
//            BufferedWriter out = new BufferedWriter(new FileWriter(path));
//            out.write(prg);
//            out.write("print(\"f\")");
//            out.close();
            Process p = Runtime.getRuntime().exec("python3"+ " "+path + args1);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while (true){
                final String ret = in.readLine();
                System.out.println("value is : " + ret);
                if(ret == null) break;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        for(int i=0;i<10;i++)
            System.out.println("\n");

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
