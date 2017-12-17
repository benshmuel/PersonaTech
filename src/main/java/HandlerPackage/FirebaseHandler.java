package HandlerPackage;

import ModulesPackage.Child;
import ModulesPackage.Employee;
import ModulesPackage.LoginClass;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.database.*;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;


/**
 * Created by benshmuel on 23/11/2017.
 */
public class FirebaseHandler {


    public static final String SUCCESS ="OK";
    public static final String FAIL ="BAD";
    private final static String PERSONA_ADMIN_SDK ="persona-tech-firebase-adminsdk-6bqub-4ed83752b0.json";
    private LoginClass currentUser;
    private FirebaseDatabase myRef;
    private final Semaphore semaphore = new Semaphore(0);
    private String currentUserUID;



    public FirebaseHandler() {
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
                    .setDatabaseUrl("https://persona-tech.firebaseio.com/")
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FirebaseApp.initializeApp(options);

        myRef = FirebaseDatabase.getInstance();


    }




    public void setCurrentUser(LoginClass currentUser) {
        this.currentUser = currentUser;
    }

    public String authenticateFireBase(){

        String returnValue=FAIL;
        try {
            HttpURLConnection con = (HttpURLConnection) new URL("https://www.googleapis.com/identitytoolkit/v3/relyingparty/verifyPassword?key=AIzaSyBpFjiyxALKk2u5O3pNRwR9BWK9VA3l3Ms").openConnection();
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestMethod("POST");
            con.connect();

            String username = currentUser.getUserEmail();
            String password = currentUser.getPassword();


            String message = "{\"email\":\""+username+"\",\"password\":\""+password+"\",\"returnSecureToken\":true}";
            byte[] outputBytes = message.getBytes("UTF-8");
            OutputStream os = con.getOutputStream();
            System.out.println(os);
            os.write(outputBytes);

            System.out.println("1 " + con.getRequestMethod());
            System.out.println("2 " + con.getResponseMessage());

            // login successfully ..
            if(con.getResponseMessage().equals(SUCCESS)){
                returnValue=SUCCESS;
                // getting the full response from google (about the user)
                InputStream inputStream = con.getInputStream();
                String out;
                if (inputStream != null) {

                    // converting the response to readable String in order to make it Json Object
                    out = org.apache.commons.io.IOUtils.toString(inputStream, "UTF-8");
                    // building Json in order to get data more easily
                    try {
                        JSONObject object = new JSONObject(out);
                        // in the JsonObject 'localId' hold the Uid of the current user , we will need it later to access DB
                        currentUserUID = object.get("localId").toString();
                        System.out.println("Json working :: " + object.get("localId"));
                    }catch (Exception D){}




                }




            }
            else {
                // user did not  login successfully
                returnValue=FAIL;

            }


            os.close();



        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return returnValue;


    }


    public Employee getUser(){
        final Employee[] employeeToReturn = new Employee[1];
        Query getUser =myRef.getReference()
                .child("Employees")
                .child(currentUserUID);
            getUser.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println(dataSnapshot);
                Employee employee =dataSnapshot.getValue(Employee.class);
                System.out.println(employee.toString());
                employeeToReturn[0] =employee;
                semaphore.release();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    return employeeToReturn[0];
    }

    public String registerNewChild(Child child){
       final Semaphore semaphoreChild = new Semaphore(0);
        final String[] returnVal = {FAIL};
        DatabaseReference dataRef =myRef.getReference()
                .child("Kindergartens")
                .child(child.getKindergarten())
                .child("Childrens")
                .child(child.getChildID());



                dataRef.setValue(child, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        System.out.println("HERE");
                        if(databaseError!=null)
                            returnVal[0] = FAIL;
                        else
                            returnVal[0] =SUCCESS;

                        semaphoreChild.release();
                    }
                });



        try {
            semaphoreChild.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return returnVal[0];
    }



    public String registerNewEmployee(Employee employee){

        // TODO: 08/12/2017 Create new user entry
//        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
//                .setEmail(employee.getUserEmail());

        return "";
    }



    public List<String> getKindergartenList () throws InterruptedException {

        Query getKindergarten = myRef.getReference()
                                    .child("Kindergartens");

        final List<String> kindergartens = new ArrayList<>();

        getKindergarten.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    kindergartens.add(ds.getKey());
                    if(kindergartens.size() == dataSnapshot.getChildrenCount()) semaphore.release();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        semaphore.acquire();
        return kindergartens;

    }

}
