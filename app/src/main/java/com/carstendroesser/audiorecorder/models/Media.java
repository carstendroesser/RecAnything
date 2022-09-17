package com.carstendroesser.audiorecorder.models;

/**
 * Created by carstendrosser on 16.10.15.
 */
public class Media {

    private int mId;
    private int mRecordId;
    private int mType;
    private String mPath;

    public Media(int pId, int pRecordId, int pType, String pPath) {
        mId = pId;
        mRecordId = pRecordId;
        mType = pType;
        mPath = pPath;
    }

    public int getId() {
        return mId;
    }

    public void setId(int pId) {
        mId = pId;
    }

    public int getRecordId() {
        return mRecordId;
    }

    public void setRecordId(int pRecordId) {
        mRecordId = pRecordId;
    }

    public int getType() {
        return mType;
    }

    public void setType(int pType) {
        mType = pType;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String pPath) {
        mPath = pPath;
    }

}
