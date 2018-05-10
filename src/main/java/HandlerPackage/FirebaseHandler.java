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


    public String registerNewEmployee(final Employee employee , LoginClass loginCredentials){


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

            //DB issue , need to hold the id inside the employee object
            employee.setuId(userRecord.getUid());


            DatabaseReference  databaseReference = myRef.getReference()
                                                        .child("Employees")
                                                        .child(employee.getuId());

            // Init the values of diagnostics per social worker , we start from '0' //

            DatabaseReference initEmployeeStatistics = myRef.getReference()
                                                            .child("EmployeesTrack")
                                                            .child(employee.getuId());

            final EmployeePerformance employeePerformance =
                    new EmployeePerformance(employee.getuId()
                    ,employee.getName(),0,
                            0,0);
            initEmployeeStatistics.setValue(employeePerformance, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError error, DatabaseReference ref) {

                    System.out.println("Added new employee successfully !!");
                    System.out.println("Employee details");
                    System.out.println(employeePerformance.toString());
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
    public String addNewDiagnostic (final Employee currentEmployee, final String testType , Test test) throws InterruptedException {

        String test_Type =testType+"DiagCounter";
        String DiagnosticType = testType+"DrawingTest";
        final Semaphore semaphoreA = new Semaphore(0);
        final Semaphore semaphoreB = new Semaphore(0);
        final boolean[] flagUpadte = {false};
        final boolean[] flagSetNew = { false };

        final long[] currentEmployeeCounter = {-1};

        final Query getEmployeeDiagnostics = myRef.getReference()
                                                .child("EmployeesTrack")
                                                .child(currentEmployee.getuId())
                                                .child(test_Type);

        final DatabaseReference updateValue = myRef.getReference().child("EmployeesTrack")
                                                            .child(currentEmployee.getuId())
                                                            .child(test_Type);


        DatabaseReference setNewDiagnostic = myRef.getReference()
                                                    .child("DiagnosticsSW")
                                                    .child(DiagnosticType)
                                                    .child(currentEmployee.getuId())
                                                    .child(test.getTestId());

        setNewDiagnostic.setValue(test, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                if(error ==null) {
                    System.out.println(testType + " Test has been successfully added");
                    flagSetNew[0] =true;
                    semaphoreA.release();

                }
                else
                    System.out.println(testType+" Test has been unsuccessfully added --- ERROR");
            }
        });


        getEmployeeDiagnostics.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                currentEmployeeCounter[0] = (long)snapshot.getValue();

                if(currentEmployeeCounter[0] != -1){

                    currentEmployeeCounter[0]+=1;

                    updateValue.setValue(currentEmployeeCounter[0], new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError error, DatabaseReference ref) {
                            if(error ==null) {
                                System.out.println("The Employee "
                                        + currentEmployee.getName() + "has been successfully updated");
                                flagUpadte[0] =true;
                                semaphoreB.release();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });


        semaphoreA.acquire();
        semaphoreB.acquire();

        if(flagSetNew[0] && flagUpadte[0]) return SUCCESS;
        else return FAIL;
    }

    public List<Test> getDiagnostics(String currentEmployee , final String testType) throws InterruptedException {

        final Semaphore semaphoreD = new Semaphore(0);
        final List<Test> tests = new ArrayList<>();

        Query testByEmployee = myRef.getReference()
                        .child("DiagnosticsSW")
                        .child(testType+"DrawingTest")
                        .child(currentEmployee);


        testByEmployee.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.getChildrenCount() == 0) semaphoreD.release();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){

                    if(testType.equals("tree"))
                        tests.add(dataSnapshot.getValue(TreeDrawingTest.class));
                    else if (testType.equals("house"))
                        tests.add(dataSnapshot.getValue(HouseDrawingTest.class));
                    else  if(testType.equals("person"))
                        tests.add(dataSnapshot.getValue(PersonDrawingTest.class));



                    if(tests.size() == snapshot.getChildrenCount())
                        semaphoreD.release();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                semaphoreD.release();

            }


        });



        semaphoreD.acquire();
        return tests;

    }


    public List<Child> getChilderensByKindergarten(String kindergarten) throws InterruptedException {

        final List<Child> children = new ArrayList<>();
        final Semaphore semaphoreChild = new Semaphore(0);

        Query getChildrens = myRef.getReference()
                                    .child("Kindergartens")
                                    .child(kindergarten)
                                    .child("Childrens");

        getChildrens.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {


                for(DataSnapshot ds : snapshot.getChildren()){


                    children.add(ds.getValue(Child.class));
                    if(children.size() == snapshot.getChildrenCount()) semaphoreChild.release();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });




        semaphoreChild.acquire();
        return children;

    }


    public List<String> getChilderenNamesByKindergarten(String kindergarten) throws InterruptedException {

        final List<String> children = new ArrayList<>();
        final Semaphore semaphoreChild = new Semaphore(0);

        Query getChildrens = myRef.getReference()
                .child("Kindergartens")
                .child(kindergarten)
                .child("Childrens");

        getChildrens.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {


                for(DataSnapshot ds : snapshot.getChildren()){

                    Child child = ds.getValue(Child.class);
                    System.out.println(child.getChildName());
                    children.add(child.getChildName());

                    if(children.size() == snapshot.getChildrenCount()) semaphoreChild.release();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });




        semaphoreChild.acquire();
        return children;

    }








    public List<Employee> getEmployees () throws InterruptedException {
        final Semaphore semaphoreEmp = new Semaphore(0);

        final List<Employee> employees = new ArrayList<>();
        Query getEmployees = myRef.getReference()
                .child("Employees")
                .orderByChild("userType").equalTo("SocialWorker");

        getEmployees.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){

                    employees.add(dataSnapshot.getValue(Employee.class));

                    if(employees.size() == snapshot.getChildrenCount()){
                        semaphoreEmp.release();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });



        semaphoreEmp.acquire();

        return employees;

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
