package ModulesPackage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by benshmuel on 03/12/2017.
 */
public class Child implements Serializable {
    private static final long serialVersionUID = 1313L;

    private String kindergarten;
    private String childName;
    private int childAge;
    private String childID;
    private String county;

    private List<Picture> pictures;
    public Child() {
    }

    public Child(String kindergarten, String childName, int childAge, String childID, String county) {
        this.kindergarten = kindergarten;
        this.childName = childName;
        this.childAge = childAge;
        this.childID = childID;
        this.county = county;
        this.pictures = new ArrayList<>();
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

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public List<Picture> getPictures() {
        return pictures;
    }

    public void setPictures(List<Picture> pictures) {
        this.pictures = pictures;
    }

    public List<String> getPicIdOfChild(String type){

        List<String> s = new ArrayList<>();
        for (Picture p: this.pictures) {

            if(type.equals(p.getType()) && !p.getTested()) {

                s.add(p.getPictureId());

            }

        }
        return s;
    }



    public String getPicUrlByPicId(String pid){

        for (Picture p: this.pictures) {

            if(p.getPictureId().equals(pid))

                return p.getPictureUrl();

        }
        return null;

    }

    public Picture getPicByPicId(String pid){

        for (Picture p: this.pictures) {

            if(p.getPictureId().equals(pid))

                return p;

        }
        return null;

    }

    public String toString(){

        return getClass().getName() + " kindergarten name = " + this.getKindergarten() + " child name = "
                + this.getChildName() + " child id = " + this.getChildID() + " age = " + this.getChildAge()
                + " County = " + this.getCounty();

    }



}
