package com.carstendroesser.audiorecorder.models;

/**
 * Created by carstendrosser on 16.10.15.
 */
public class Category {

    private int mId;
    private String mName;
    private String mDescription;
    private int mIcon;
    private boolean mIsSelected = true;

    public Category(int pId, String pName, String pDescription, int pIcon) {
        mId = pId;
        mName = pName;
        mDescription = pDescription;
        mIcon = pIcon;
    }

    public int getId() {
        return mId;
    }

    public void setId(int pId) {
        mId = pId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String pName) {
        mName = pName;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String pDescription) {
        mDescription = pDescription;
    }

    public int getIcon() {
        return mIcon;
    }

    public void setIcon(int pIcon) {
        mIcon = pIcon;
    }

    public void setSelected(boolean pSelected) {
        mIsSelected = pSelected;
    }

    public boolean getIsSelected() {
        return mIsSelected;
    }

    public Category clone() {
        Category clone = new Category(mId, mName, mDescription, mIcon);
        clone.setSelected(mIsSelected);
        return clone;
    }

}
