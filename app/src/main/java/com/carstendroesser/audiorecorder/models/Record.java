package com.carstendroesser.audiorecorder.models;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by carstendrosser on 16.10.15.
 */
public class Record {

    private int mId;
    private String mPath;
    private String mName;
    private int mCategoryId;
    private Category mCategory;
    private ArrayList<Media> mMediaList;
    private Note mNote;
    private long mTimestamp;

    public Record(int pId, String pPath, String pName, int pCategoryId, Category pCategory, long pTimestamp) {
        mId = pId;
        mPath = pPath;
        mName = pName;
        mCategoryId = pCategoryId;
        mCategory = pCategory;
        mMediaList = new ArrayList<>();
        mTimestamp = pTimestamp;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String pPath) {
        mPath = pPath;
    }

    public String getName() {
        return mName;
    }

    public String getNameWithExtension() {
        return getName() + mPath.substring(mPath.lastIndexOf("."));
    }

    public void setName(String pName) {
        mName = pName;
    }

    public int getCategoryId() {
        return mCategoryId;
    }

    public void setCategoryId(int pCategory) {
        mCategoryId = pCategory;
    }

    public Category getCategory() {
        return mCategory;
    }

    public void setCategory(Category pCategory) {
        mCategory = pCategory;
    }

    public void setMediaList(ArrayList<Media> pMediaList) {
        mMediaList = new ArrayList<>();
        mMediaList.addAll(pMediaList);
    }

    public ArrayList<Media> getMediaList() {
        return mMediaList;
    }

    public void setNote(Note pNote) {
        if (pNote == null) {
            mNote = null;
        } else {
            mNote = pNote.clone();
        }
    }

    public Note getNote() {
        return mNote;
    }

    public long getFileSize() {
        File file = new File(getPath());
        return file.length();
    }

    public long getLastModified() {
        File file = new File(getPath());
        return file.lastModified();
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long pTimestamp) {
        mTimestamp = pTimestamp;
    }

    public Record clone() {
        Category clonedCategory = new Category(mCategory.getId(),
                mCategory.getName(),
                mCategory.getDescription(),
                mCategory.getIcon());
        Record clonedRecord = new Record(mId, mPath, mName, mCategoryId, clonedCategory, mTimestamp);
        clonedRecord.setMediaList(mMediaList);
        clonedRecord.setNote(mNote);
        return clonedRecord;
    }

    public boolean isWavRecording() {
        return mPath.endsWith(".wav");
    }

}
