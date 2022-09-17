package com.carstendroesser.audiorecorder.models;

/**
 * Created by carstendrosser on 16.10.15.
 */
public class Note {

    private int mId;
    private int mRecordId;
    private String mText;

    public Note(int pId, int pRecordId, String pText) {
        mId = pId;
        mRecordId = pRecordId;
        mText = pText;
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

    public String getText() {
        return mText;
    }

    public void setText(String pText) {
        mText = pText;
    }

    public Note clone() {
        return new Note(mId, mRecordId, mText);
    }

}
