package HandlerPackage;

import ModulesPackage.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.database.*;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
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
//            System.out.println(os);
            os.write(outputBytes);

//            System.out.println("1 " + con.getRequestMethod());
//            System.out.println("2 " + con.getResponseMessage());

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
                Employee employee =dataSnapshot.getValue(Employee.class);
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


    public String registerNewEmployee(Employee employee , LoginClass loginCredentials){


        final String[] returnVal = {FAIL};
        final Semaphore semaphoreChild = new Semaphore(0);

        // google auth - create new user entry using request method ...
        // getting relevant information from the loginCredentials Object

        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(loginCredentials.getUserEmail())
                .setPassword(loginCredentials.getPassword());

        try {
            // alright so userRecord holds the record of the new user , need to extract the UID in order the write it to the database !

            UserRecord userRecord = FirebaseAuth.getInstance().createUserAsync(request).get();

            //DB issue , need to hold the id inside the emoloyee object
            employee.setuId(userRecord.getUid());


            DatabaseReference  databaseReference = myRef.getReference()
                                                        .child("Employees")
                                                        .child(employee.getuId());

            // Init the values of diagnostics per social worker , we start from '0' //

            DatabaseReference initEmployeeStatistics = myRef.getReference()
                                                            .child("EmployeeTrack")
                                                            .child(employee.getuId());

            initEmployeeStatistics.setValue("0", new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError error, DatabaseReference ref) {

                }
            });

            databaseReference.setValue(employee, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError error, DatabaseReference ref) {
                    if(error == null){
                        returnVal[0] =SUCCESS;
                        semaphoreChild.release();

                    }
                }
            });

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }



        // makes the thread wait here till the operation is fully completed

        try {
            semaphoreChild.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return returnVal[0];
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


    // TODO: 14/01/2018 test this function 
    public List<EmployeePerformance> getEmployeePerformance() throws InterruptedException {

        final Semaphore semaphoreEmp = new Semaphore(0);
         final List<EmployeePerformance> statisticsEmp = new ArrayList<>();


        Query getStatistics = myRef.getReference()
                                    .child("EmployeesTrack");



        getStatistics.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    statisticsEmp.add(dataSnapshot.getValue(EmployeePerformance.class));
                    if(statisticsEmp.size() == snapshot.getChildrenCount()) semaphoreEmp.release();

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });


        semaphoreEmp.acquire();
        return statisticsEmp;
    }

    // TODO: 14/01/2018  test this function
    public String addNewDiagnostic (final Employee currentEmployee,String type){

        String testType =type+"DiagCounter";


        final long[] currentEmployeeCounter = {-1};

        final Query getEmployeeDiagnostics = myRef.getReference()
                                                .child("EmployeeTrack")
                                                .child(currentEmployee.getuId())
                                                .child(testType);

        final DatabaseReference updateValue = myRef.getReference().child("EmployeeTrack")
                                                            .child(currentEmployee.getuId())
                                                            .child(testType);



        getEmployeeDiagnostics.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                currentEmployeeCounter[0] = (long)snapshot.getValue();

                if(currentEmployeeCounter[0] != -1){

                    currentEmployeeCounter[0]+=1;

                    updateValue.setValue(currentEmployeeCounter[0], new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError error, DatabaseReference ref) {
                            System.out.println("The Employee "
                                            + currentEmployee.getName() +"has been successfully updated");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });



        return "s";
    }

    // TODO: 14/01/2018 test the functin .. sorting elements etc ...
    public List<TreeDrawingTest> getTreeDrawingTestByEmployee(Employee currentEmployee) throws InterruptedException {

        final Semaphore semaphoreD = new Semaphore(0);
        final List<TreeDrawingTest> tests = new ArrayList<>();

        Query testByEmployee = myRef.getReference()
                        .child("DiagnosticsSW")
                        .child("TreeDrawingTest")
                        .orderByChild("socialWorkerId")
                        .equalTo(currentEmployee.getId());


        testByEmployee.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println("All the relevant workers");
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){

                    System.out.println(dataSnapshot);
                    tests.add(dataSnapshot.getValue(TreeDrawingTest.class));

                    if(tests.size() == snapshot.getChildrenCount())
                        semaphoreD.release();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });



        semaphoreD.acquire();
        return tests;

    }




    public void write(){

        EmployeePerformance employeePerformance =
                new EmployeePerformance(
                        "mkrkRMdQsjWsH5YleMiJElRDRdF2",
                        "ita ben",
                        0,0,0);

        DatabaseReference df = myRef.getReference().child("EmployeesTrack")
                                    .child(employeePerformance.getEmpUid());


        df.setValue(employeePerformance, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                System.out.println("done bitch .....");
            }
        });



    }
    public String getCurrentUserUID() {
        return currentUserUID;
    }
}
