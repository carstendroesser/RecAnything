package com.carstendroesser.audiorecorder.utils;

import java.io.IOException;

/**
 * Created by carstendrosser on 03.03.16.
 */
public class WavFile extends WritableFile {

    public enum Offset {
        RIFF(0),
        FILESIZE(4),
        WAVE(8),
        FMT(12),
        SUBCHUNKSIZE(16),
        AUDIOFORMAT(20),
        CHANNELS(22),
        SAMPLERATE(24),
        BYTERATE(28),
        BLOCKALIGN(32),
        BITSPERSAMPLE(34),
        DATAMARK(36),
        DATASIZE(40);

        int offset;

        Offset(int pOffset) {
            offset = pOffset;
        }
    }

    /**
     * Constructs a new WavFile.
     *
     * @param pFilePath the path of the file
     * @param pMode     rw
     * @throws IOException if something went wrong
     */
    public WavFile(String pFilePath, String pMode) throws IOException {
        super(pFilePath, pMode);
    }

    /**
     * Writes everything neccessary into the wave-header.
     * DO ONLY USE FOR NEW FILES!
     *
     * @throws IOException if something went wrong
     */
    public void writeHeaderData() throws IOException {
        setLength(0);
        writeRiff();
        //we do not know final filesize. write 0 instead.
        writeFilesize(0);
        writeWave();
        writeFMT();

        //subchunk-size. 16 for PCM. final.
        writeSubchunkSize(16);

        //audioformat. 1 for PCM. final.
        writeAudioFormat((short) 1);
        writeDataMark();
        writeDataSize(0);
    }

    /**
     * Writes RIFF into the header at the specific position.
     *
     * @throws IOException if something went wrong
     */
    public void writeRiff() throws IOException {
        long position = getFilePointer();
        seek(Offset.RIFF.offset);
        writeBytes("RIFF");
        seek(position);
    }

    /**
     * Writes the filesize. Usually the filesize is
     * payloadSize + headerSize (44) - 8
     *
     * @param pFilesize the filesize
     * @throws IOException if something went wrong
     */
    public void writeFilesize(int pFilesize) throws IOException {
        long position = getFilePointer();
        seek(Offset.FILESIZE.offset);
        writeInt(Integer.reverseBytes(pFilesize));
        seek(position);
    }

    /**
     * Writes WAVE into the header at the specific position.
     *
     * @throws IOException if something went wrong
     */
    public void writeWave() throws IOException {
        long position = getFilePointer();
        seek(Offset.WAVE.offset);
        writeBytes("WAVE");
        seek(position);
    }

    /**
     * Writes fmt into the header at the specific position.
     *
     * @throws IOException if something went wrong
     */
    public void writeFMT() throws IOException {
        long position = getFilePointer();
        seek(Offset.FMT.offset);
        writeBytes("fmt ");
        seek(position);
    }

    /**
     * Writes the subchunksize into the header at the specific position.
     * value 16 for PCM!
     *
     * @param pSubchunkSize the subchunksize
     * @throws IOException if something went wrong
     */
    public void writeSubchunkSize(int pSubchunkSize) throws IOException {
        long position = getFilePointer();
        seek(Offset.SUBCHUNKSIZE.offset);
        writeInt(Integer.reverseBytes(pSubchunkSize));
        seek(position);
    }

    /**
     * Writes the audioformat into the header at the specific position.
     * value 1 for PCM!
     *
     * @param pAudioFormat the audioformat-constant
     * @throws IOException if something went wrong
     */
    public void writeAudioFormat(short pAudioFormat) throws IOException {
        long position = getFilePointer();
        seek(Offset.AUDIOFORMAT.offset);
        writeShort(Short.reverseBytes((short) pAudioFormat));
        seek(position);
    }

    /**
     * Writes the channels into the header at the specific position.
     * 1 for mono, 2 for stereo.
     *
     * @param pChannels the amount of channels
     * @throws IOException if something went wrong
     */
    public void writeChannels(short pChannels) throws IOException {
        long position = getFilePointer();
        seek(Offset.CHANNELS.offset);
        writeShort(Short.reverseBytes(pChannels));
        seek(position);
    }

    /**
     * Writes the samplerate into the header at the specific position.
     *
     * @param pSampleRate the samplerate
     * @throws IOException if something went wrong
     */
    public void writeSampleRate(int pSampleRate) throws IOException {
        long position = getFilePointer();
        seek(Offset.SAMPLERATE.offset);
        writeInt(Integer.reverseBytes(pSampleRate));
        seek(position);
    }

    /**
     * Writes the byterate into the header at the specific position.
     * Byterate = samplerate * samples * channels / 8.
     *
     * @param pByteRate the value to write
     * @throws IOException if something went wrong
     */
    public void writeByteRate(int pByteRate) throws IOException {
        long position = getFilePointer();
        seek(Offset.BYTERATE.offset);
        writeInt(Integer.reverseBytes(pByteRate));
        seek(position);
    }

    /**
     * Writes the blockalign into the header at the specific position.
     * Blockalign = channels * samples / 8.
     *
     * @param pBlockAlign the value to write
     * @throws IOException if something went wrong
     */
    public void writeBlockAlign(short pBlockAlign) throws IOException {
        long position = getFilePointer();
        seek(Offset.BLOCKALIGN.offset);
        writeShort(Short.reverseBytes(pBlockAlign));
        seek(position);
    }

    /**
     * Writes the bitsPerSample-value into the header at the specific position.
     * bitsPerSample = samples.
     *
     * @param pBitsPerSample the samples/bit
     * @throws IOException if something went wrong
     */
    public void writeBitsPerSample(short pBitsPerSample) throws IOException {
        long position = getFilePointer();
        seek(Offset.BITSPERSAMPLE.offset);
        writeShort(Short.reverseBytes(pBitsPerSample));
        seek(position);
    }

    /**
     * Writes the data-mark into the header at the specific position.
     *
     * @throws IOException if something went wrong
     */
    public void writeDataMark() throws IOException {
        long position = getFilePointer();
        seek(Offset.DATAMARK.offset);
        writeBytes("data");
        seek(position);
    }

    /**
     * Writes the datasize into the header at the specific position.
     * Datasize = payloadsize.
     *
     * @param pDataSize the datasize to write
     * @throws IOException if something went wrong
     */
    public void writeDataSize(int pDataSize) throws IOException {
        long position = getFilePointer();
        seek(Offset.DATASIZE.offset);
        writeInt(Integer.reverseBytes(pDataSize));
        seek(position);
    }

    /**
     * Seeks to the position where the riff-bytes should be.
     * Useful to check if riff-bytes are correct.
     *
     * @return a string containing the content of the bytes
     * at the position of the riff-bytes
     * @throws IOException
     */
    public String getRiff() throws IOException {
        long position = getFilePointer();
        seek(Offset.RIFF.offset);
        char r = (char) readByte();
        char i = (char) readByte();
        char f = (char) readByte();
        char f2 = (char) readByte();
        seek(position);
        return "" + r + i + f + f2;
    }

    /**
     * Seeks to the position where the size is written and
     * reads the correct bytes.
     *
     * @return the filesize. usually payloadSize + headerSize (44) - 8
     * @throws IOException if something went wrong
     */
    public int getFileSize() throws IOException {
        long position = getFilePointer();
        seek(Offset.FILESIZE.offset);
        int filesize = Integer.reverseBytes(readInt());
        seek(position);
        return filesize;
    }

    /**
     * Seeks to the position where wave should be written to and reads
     * the specific bytes.
     *
     * @return a string containing the chars written to these bytes
     * @throws IOException if something went wrong
     */
    public String getHeaderPartWave() throws IOException {
        long position = getFilePointer();
        seek(Offset.WAVE.offset);
        char w = (char) readByte();
        char a = (char) readByte();
        char v = (char) readByte();
        char e = (char) readByte();
        seek(position);
        return "" + w + a + v + e;
    }

    /**
     * Seeks to the position where FMT should be written to and reads
     * the specific bytes.
     *
     * @return a string containing the chars written to these bytes
     * @throws IOException if something went wrong
     */
    public String getHeaderPartFMT() throws IOException {
        long position = getFilePointer();
        seek(Offset.FMT.offset);
        char f = (char) readByte();
        char m = (char) readByte();
        char t = (char) readByte();
        char space = (char) readByte();
        seek(position);
        return "" + f + m + t + space;
    }

    /**
     * Seeks to the position where the subchunksize is written to and
     * reads it content.
     *
     * @return the subchunksize, 16 for PCM
     * @throws IOException
     */
    public int getSubChunkSize() throws IOException {
        long position = getFilePointer();
        seek(Offset.SUBCHUNKSIZE.offset);
        int value = readInt();
        seek(position);
        return Integer.reverseBytes(value);
    }

    /**
     * Seeks to the position to where the audioformat is written.
     * 1 for PCM.
     *
     * @return a value representing PCM (1) or something else
     * @throws IOException if something went wrong
     */
    public short getAudioFormat() throws IOException {
        long position = getFilePointer();
        seek(Offset.AUDIOFORMAT.offset);
        short value = readShort();
        seek(position);
        return Short.reverseBytes(value);
    }

    /**
     * Seeks to the position to where the channelcount is written
     * and reads it's value.
     *
     * @return 1 for mono, 2 for stereo
     * @throws IOException
     */
    public short getChannels() throws IOException {
        long position = getFilePointer();
        seek(Offset.CHANNELS.offset);
        short value = readShort();
        seek(position);
        return Short.reverseBytes(value);
    }

    /**
     * Seeks to the position to where the samplerate is written
     * and reads it's value.
     *
     * @return the samplerate for this wave-file
     * @throws IOException if something went wrong
     */
    public int getSampleRate() throws IOException {
        long position = getFilePointer();
        seek(Offset.SAMPLERATE.offset);
        int value = readInt();
        seek(position);
        return Integer.reverseBytes(value);
    }

    /**
     * Seeks to the position to where the byterate is written
     * and reads it's value.
     *
     * @return the byterate for this wave-file
     * @throws IOException if something went wrong
     */
    public int getByterate() throws IOException {
        long position = getFilePointer();
        seek(Offset.BYTERATE.offset);
        int value = readInt();
        seek(position);
        return Integer.reverseBytes(value);
    }

    /**
     * Seeks to the position to where the blockalign is written
     * and reads it's value.
     *
     * @return the blockalign
     * @throws IOException
     */
    public short getBlockAlign() throws IOException {
        long position = getFilePointer();
        seek(Offset.BLOCKALIGN.offset);
        short value = readShort();
        seek(position);
        return Short.reverseBytes(value);
    }

    /**
     * Seeks to the position to where the bitsPerSample are written
     * and reads it's value.
     *
     * @return the bitsPerSample-rate
     * @throws IOException if something went wrong
     */
    public short getBitsPerSample() throws IOException {
        long position = getFilePointer();
        seek(Offset.BITSPERSAMPLE.offset);
        short value = readShort();
        seek(position);
        return Short.reverseBytes(value);
    }

    /**
     * Seeks to the position to where DATA should be written and
     * reads the bytes' content.
     *
     * @return a string hopefully containing "data"
     * @throws IOException if something went wrong
     */
    public String getDataMark() throws IOException {
        long position = getFilePointer();
        seek(Offset.DATAMARK.offset);
        char d = (char) readByte();
        char a = (char) readByte();
        char t = (char) readByte();
        char a2 = (char) readByte();
        seek(position);
        return "" + d + a + t + a2;
    }

    /**
     * Seeks to the position to where the datasize is written
     * and reads the value.
     *
     * @return the size of the data of this wave-file
     * @throws IOException if something went wrong
     */
    public int getDataSize() throws IOException {
        long position = getFilePointer();
        seek(Offset.DATASIZE.offset);
        int value = readInt();
        seek(position);
        return Integer.reverseBytes(value);
    }

    public int getFrameCount() throws IOException {
        return getDataSize() / getFrameSize();
    }

    public int getFrameSize() throws IOException {
        return (getBitsPerSample() * getChannels()) / 8;
    }

    public int getFramesPerSecond() throws IOException {
        int bytesPerSecond = (getSampleRate() * getBitsPerSample() * getChannels() / 8);
        return bytesPerSecond / getFrameSize();
    }

    @Override
    public String toString() {
        try {
            return "Riff: " + getRiff()
                    + "\nFilesize: " + getFileSize()
                    + "\nWAVE: " + getHeaderPartWave()
                    + "\nFMT : " + getHeaderPartFMT()
                    + "\nSubchunksize: " + getSubChunkSize()
                    + "\nAudioFormat: " + getAudioFormat()
                    + "\nChannels: " + getChannels()
                    + "\nSamplerate: " + getSampleRate()
                    + "\nByteRate: " + getByterate()
                    + "\nBlockalign: " + getBlockAlign()
                    + "\nBitsPerSample: " + getBitsPerSample()
                    + "\nDatamark: " + getDataMark()
                    + "\nDatasize: " + getDataSize();
        } catch (IOException e) {
            return "error reading all values";
        }
    }

    /**
     * Copies the headerdata of another wavfile to this wavfile.
     *
     * @param pWavFile the wavfile to take the header from
     * @throws IOException if an error occurs
     */
    public void copyHeaderDataFrom(WavFile pWavFile) throws IOException {
        writeRiff();
        writeFilesize(pWavFile.getFileSize());
        writeWave();
        writeFMT();
        writeSubchunkSize(pWavFile.getSubChunkSize());
        writeAudioFormat(pWavFile.getAudioFormat());
        writeChannels(pWavFile.getChannels());
        writeSampleRate(pWavFile.getSampleRate());
        writeByteRate(pWavFile.getByterate());
        writeBlockAlign(pWavFile.getBlockAlign());
        writeBitsPerSample(pWavFile.getBitsPerSample());
        writeDataMark();
        writeDataSize(pWavFile.getDataSize());
    }

    /**
     * Sets the filepointer to the given frame.
     *
     * @param pFrameNumber the frame to seek to
     * @throws IOException if an error occurs
     */
    public void seekToFrame(int pFrameNumber) throws IOException {
        seek(44 + getFrameSize() * pFrameNumber);
    }

}
