package ModulesPackage;

import java.io.Serializable;

/**
 * Created by benshmuel on 03/12/2017.
 */
public class Child implements Serializable {
    private static final long serialVersionUID = 1313L;

    private String kindergarten;
    private String childName;
    private int childAge;
    private String childID;

    public Child(String kindergarten, String childName, int childAge) {
        this.kindergarten = kindergarten;
        this.childName = childName;
        this.childAge = childAge;
    }

    public String getKindergarten() {
        return kindergarten;
    }

    public void setKindergarten(String kindergarten) {
        this.kindergarten = kindergarten;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public int getChildAge() {
        return childAge;
    }

    public void setChildAge(int childAge) {
        this.childAge = childAge;
    }

    public String getChildID() {
        return childID;
    }

    public void setChildID(String childID) {
        this.childID = childID;
    }
}
