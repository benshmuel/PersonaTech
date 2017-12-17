package ModulesPackage;

import java.io.Serializable;

/**
 * Created by benshmuel on 03/12/2017.
 */
public class Employee implements Serializable {

    private int age;
    private String name;
    private String uId;
    private String userType;
    private String userEmail;
    private static final long serialVersionUID = 1113L;

    public Employee() {
    }

    public Employee(String uId, String userType, int age, String name , String userEmail) {
        this.uId = uId;
        this.userType = userType;
        this.age = age;
        this.name = name;
        this.userEmail = userEmail;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    @Override
    public String toString() {
        return "The Employee is : \n"+getName()+"\n"+getAge()+"\n"+getUserType()+"\n"+getuId()+"\n";
    }
}
