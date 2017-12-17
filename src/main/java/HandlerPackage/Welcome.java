package HandlerPackage;

import ModulesPackage.Child;
import ModulesPackage.Employee;
import ModulesPackage.LoginClass;
import ModulesPackage.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
        System.out.println("Client  is Connected !! ... ");
        System.out.println("Socket Information :  " + socket.toString() );
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

                        LoginClass currentUser = (LoginClass) objectInputStream.readObject();
                        System.out.println("The user " + currentUser.getUserEmail() +" is inside the server");
                        firebaseHandler.setCurrentUser(currentUser);

                        if(firebaseHandler.authenticateFireBase().equals(FirebaseHandler.SUCCESS)){
                            System.out.println("user is successfully logged in");

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



                        // for testing only (getting the right kindergartens)
//                        try {
//                            List<String > stringList =firebaseHandler.getKindergartenList();
//                            System.out.println("Kindergartens :: ");
//                            for(String str : stringList){
//                                System.out.println(" -- > " + str);
//                            }
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }

                        break;

                    case "Add child":

                        Child childToAdd = (Child)objectInputStream.readObject();
                        System.out.println("About to add new child to the system");
                        if(firebaseHandler!=null) {
                            if (firebaseHandler.registerNewChild(childToAdd).equals(FirebaseHandler.SUCCESS)) {
                                objectOutputStream.writeObject(FirebaseHandler.SUCCESS);
                            }
                            else objectOutputStream.writeObject(FirebaseHandler.FAIL);
                        }
                        else {
                            objectOutputStream.writeObject(FirebaseHandler.FAIL);
                        }

                        break;




                    case "Add employee" :

                        Employee employeeToAdd = (Employee)objectInputStream.readObject();
                        System.out.println("got new employee to add to the system");
                        if(firebaseHandler!=null){

                        }
                        break;

                }


            }


            //}


        } catch(IOException e) {
            System.out.println("Oops: " + e.getMessage());
        } catch (ClassNotFoundException e) {
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