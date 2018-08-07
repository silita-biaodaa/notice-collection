package com.snatch.model;

/**
 * Created by dh on 2018/3/5.
 */
public class Area {
    private String id;

    private String name;

    private String name_abbr;

    private int grade;//0=省级，1=市级，2=县区级

    private String parent_id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName_abbr() {
        return name_abbr;
    }

    public void setName_abbr(String name_abbr) {
        this.name_abbr = name_abbr;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }
}
