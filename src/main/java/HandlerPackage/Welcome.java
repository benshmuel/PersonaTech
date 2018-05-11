package HandlerPackage;

import ModulesPackage.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class Welcome extends Thread {

    private Socket socket;
    private Employee currentEmployee; // stoped here
    private FirebaseHandler firebaseHandler ;

    public Welcome(Socket socket ,FirebaseHandler firebaseHandler) {
        this.socket = socket;
        this.firebaseHandler = firebaseHandler;
    }

    @Override
    public void run() {
        String client = "Unknown Client" ;
        System.out.println("Establishing new connection with "+client);
        System.out.println("Socket Information :  " + socket.toString());
        try {

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());


            while(true) {
                String action = (String) objectInputStream.readObject();
                switch (action) {
                    case "msg":
                        Message message = (Message) objectInputStream.readObject();
                        System.out.println(message.getName() + "  " + message.getAge());


                        System.out.println("Sending message back .... ");

                        objectOutputStream.writeObject("Server Response");
                        objectOutputStream.writeObject(message);


                        break;

                    case "login":

                        System.out.println("**************************************************************");
                        System.out.println("\n");
                        LoginClass currentUser = (LoginClass) objectInputStream.readObject();
                        System.out.println(client +" identify as " + currentUser.getUserEmail());
                        client = currentUser.getUserEmail();
                        System.out.println(currentUser.getUserEmail() +" is trying to log into the system ...");
                        System.out.println("\n");
                        System.out.println("**************************************************************");
                        firebaseHandler.setCurrentUser(currentUser);

                        if(firebaseHandler.authenticateFireBase().equals(FirebaseHandler.SUCCESS)){
                            System.out.println(client+" is successfully logged in !");
                            // message's header ( success )
                            objectOutputStream.writeObject(FirebaseHandler.SUCCESS);
                            // message's body ( the actual object)
                            currentEmployee = firebaseHandler.getUser();
                            objectOutputStream.writeObject(currentEmployee);
                        }

                        else {

                            // message's header ( fail )
                            objectOutputStream.writeObject(FirebaseHandler.FAIL);
                        }

                        break;

                    case "get Kindergartens" :
                        // for testing only (getting the right kindergartens)
                        System.out.println(client +" is asking for all Kindergartens");
                        System.out.println("\n");
                        try {
                            List<String > stringList =firebaseHandler.getKindergartenList();
                            objectOutputStream.writeObject(stringList);

                            System.out.println("Kindergartens that sent to the Client  : ");
                            System.out.println("\n");
                            for(String str : stringList){
                                System.out.println("----> " + str);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Add child":

                        System.out.println("handling "+client+"'s" +"request for adding new child to the system");
                        Child childToAdd = (Child)objectInputStream.readObject();
                        System.out.println("Adding new child to the system ... ");
                        System.out.println("\n");
                        if(firebaseHandler!=null) {
                            if (firebaseHandler.registerNewChild(childToAdd).equals(FirebaseHandler.SUCCESS)) {
                                objectOutputStream.writeObject(FirebaseHandler.SUCCESS);
                                System.out.println("Child added successfully");
                            }
                            else {
                                System.out.println("Error with adding new child - child didn't added successfully");
                                objectOutputStream.writeObject(FirebaseHandler.FAIL);}
                        }
                        else {
                            objectOutputStream.writeObject(FirebaseHandler.FAIL);
                        }

                        break;




                    case "Add employee" :

                        LoginClass loginCredentials =(LoginClass)objectInputStream.readObject();
                        Employee employeeToAdd = (Employee)objectInputStream.readObject();
                        System.out.println("\n");
                        System.out.println("The Client " + client +" requested to add new Employee ");
                        System.out.println("Employee information :: " + employeeToAdd.toString());
                        if(firebaseHandler!=null){

                            if(firebaseHandler.registerNewEmployee(employeeToAdd,loginCredentials).equals(FirebaseHandler.SUCCESS))
                                objectOutputStream.writeObject(FirebaseHandler.SUCCESS);
                            else
                                objectOutputStream.writeObject(FirebaseHandler.FAIL);



                        }
                        else
                            objectOutputStream.writeObject(FirebaseHandler.FAIL);

                        break;




                    case "get Employees" :
                        if(firebaseHandler!=null){
                            System.out.println("\n");
                            System.out.println("The Client " + client +" requested all employees  ");

                            objectOutputStream.writeObject(firebaseHandler.getEmployees());

                        }
                        break;

                    case "get EmployeeStatistics" :
                        if (firebaseHandler!=null){

                            System.out.println("\n");
                            System.out.println("The Client " + client +" requested all employees statistics ");
                            try {
                                List<EmployeePerformance> statistics = firebaseHandler.getEmployeePerformance();
                                objectOutputStream.writeObject(statistics);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }


                        }
                        else
                            objectOutputStream.writeObject(FirebaseHandler.FAIL);

                        break;



                    case "get TestBy_Type_Employee":

                        break;

                    case "get Diagnostic":
                        if(firebaseHandler!=null){
                            System.out.println("The Client " + client +" requested all employees Diagnostics ");

                            String type = (String)objectInputStream.readObject();
                            String currentEmployee  = (String) objectInputStream.readObject();
                            objectOutputStream.writeObject(firebaseHandler.getDiagnostics(currentEmployee,type));

                        }
                        else
                            objectOutputStream.writeObject(FirebaseHandler.FAIL);


                        break;
                    case "Add Diagnostic":

                        if (firebaseHandler!=null){


                            /**
                             * getting The relevant data from client side.
                             * @params type = kind of test
                             * @params current employee
                             * @params test = the actual test object
                             * */
                            String type = (String)objectInputStream.readObject();
                            Employee currentEmployee  = (Employee)objectInputStream.readObject();
                            Test test = (Test)objectInputStream.readObject();

                            if(firebaseHandler.addNewDiagnostic(currentEmployee,type,test).equals(FirebaseHandler.SUCCESS)){
                                objectOutputStream.writeObject(FirebaseHandler.SUCCESS);

                            }
                            else
                                objectOutputStream.writeObject(FirebaseHandler.FAIL);


                        }
                        else
                            objectOutputStream.writeObject(FirebaseHandler.FAIL);

                        break;



                    case "get childrensByKindergarten" :

                        String kindergarten = (String)objectInputStream.readObject();
                        System.out.println(client +" requested all the childrens from the " + kindergarten +" kindergarten");

                        if(firebaseHandler!=null){

                            List<Child> childrens = firebaseHandler.getChilderensByKindergarten(kindergarten);
                            objectOutputStream.writeObject(childrens);

                        }
                        else
                            objectOutputStream.writeObject(FirebaseHandler.FAIL);


                        break;


                    case "get childerNamesBycKindergarten":

                        String kindergarten_name = (String)objectInputStream.readObject();
                        System.out.println(client +" requested all the children names from the " + kindergarten_name +" kindergarten");
                        if(firebaseHandler!=null){
                            Map<String,String>childrenNamesList = firebaseHandler.getChilderenNamesByKindergarten(kindergarten_name);
                            objectOutputStream.writeObject(childrenNamesList);

                        }
                        else
                        {
                            objectOutputStream.writeObject(FirebaseHandler.FAIL);

                        }
                        break;
                }


            }


            //}


        } catch(IOException e) {
            System.out.println("Oops: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println("socket is about to close !");
                socket.close();
            } catch(IOException e) {
                // Oh, well!
            }
        }

    }

}