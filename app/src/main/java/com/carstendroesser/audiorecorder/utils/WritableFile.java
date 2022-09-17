package com.carstendroesser.audiorecorder.utils;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by carstendrosser on 13.12.17.
 */

public class WritableFile extends RandomAccessFile {

    protected String mPath;

    public WritableFile(String pFilePath, String pMode) throws IOException {
        super(pFilePath, pMode);
        mPath = pFilePath;
    }

    /**
     * Used to append record-data.
     *
     * @param pBuffer the data to append
     * @throws IOException if something went wrong
     */
    public void appendBytes(byte[] pBuffer) throws IOException {
        seek(length());
        write(pBuffer);
    }

    /**
     * Used to append record-data.
     *
     * @param pByte the byte to append
     * @throws IOException if something went wrong
     */
    public void appendByte(byte pByte) throws IOException {
        seek(length());
        write(pByte);
    }

    /**
     * The path of this wavfile.
     *
     * @return the path of this wavfile
     */
    public String getPath() {
        return mPath;
    }

}
