package com.carstendroesser.audiorecorder.server;

import android.content.Context;
import android.media.MediaPlayer;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.models.Record;
import com.carstendroesser.audiorecorder.utils.DatabaseHelper;
import com.carstendroesser.audiorecorder.utils.FormatUtils;
import com.carstendroesser.audiorecorder.utils.Preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by carstendrosser on 25.01.18.
 */

public class WifiTransferServer extends NanoHTTPD {

    private static final String WEBSITE_TITLE = "TO_BE_REPLACED_WEBSITE_TITLE";
    private static final String TITLE = "TO_BE_REPLACED_TITLE";
    private static final String HEADER = "TO_BE_REPLACED_HEADER_RECORDINGS";
    private static final String SUBHEADER = "TO_BE_REPLACED_SUBHEADER";
    private static final String RECORDINGS = "TO_BE_REPLACED_RECORDINGS";
    private static final String FOOTER = "TO_BE_REPLACED_FOOTER_TEXT";
    private static final String APPNAME = "TO_BE_REPLACED_APPNAME";

    private static final String RECORD_ID = "TO_BE_REPLACED_ID";
    private static final String RECORD_NAME = "TO_BE_REPLACED_NAME";
    private static final String RECORD_FILESIZE = "TO_BE_REPLACED_FILESIZE";
    private static final String RECORD_DURATION = "TO_BE_REPLACED_DURATION";
    private static final String RECORD_CATEGORY = "TO_BE_REPLACED_CATEGORY";
    private static final String RECORD_DATE = "TO_BE_REPLACED_DATE";

    private Context mContext;

    public WifiTransferServer(Context pContext, int pPort) {
        super(pPort);
        mContext = pContext;
    }

    public WifiTransferServer(Context pContext, String pHostname, int pPort) {
        super(pHostname, pPort);
        mContext = pContext;
    }

    @Override
    public Response serve(IHTTPSession pSession) {
        if (pSession.getUri().startsWith("/DOWNLOAD")) {
            int id = Integer.parseInt(pSession.getUri().replace("/DOWNLOAD", ""));
            Record record = DatabaseHelper.getInstance(mContext).getRecordById(id);
            try {
                File file = new File(record.getPath());
                final Response resp = newFixedLengthResponse(
                        Response.Status.OK,
                        "application/octet-stream",
                        new FileInputStream(file),
                        file.length());
                resp.addHeader("Content-Disposition", "attachment; filename=\"" + record.getNameWithExtension());
                return resp;
            } catch (final Exception pException) {
                pException.printStackTrace();
                return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "ERROR");
            }
        }

        return newFixedLengthResponse(createWebsite());
    }

    private String createWebsite() {
        String tContents = "";

        try {
            InputStream stream = mContext.getAssets().open("website.txt");

            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            tContents = new String(buffer);
        } catch (IOException e) {
            // Handle exceptions here
        }

        ArrayList<Record> records = DatabaseHelper.getInstance(mContext).getRecords(Preferences.getRecordingsSorting(mContext));

        StringBuilder recordings = new StringBuilder();

        for (Record record : records) {
            recordings.append(getHtmlForRecord(record));
        }

        tContents = tContents.replace(WEBSITE_TITLE, mContext.getString(R.string.wifi_transfer));
        tContents = tContents.replace(TITLE, mContext.getString(R.string.app_name));
        tContents = tContents.replace(HEADER, mContext.getString(R.string.wifi_transfer));
        tContents = tContents.replace(SUBHEADER, mContext.getString(R.string.all_recordings));
        tContents = tContents.replace(RECORDINGS, recordings.toString());
        tContents = tContents.replace(FOOTER, mContext.getString(R.string.website_footer));
        tContents = tContents.replace(APPNAME, mContext.getString(R.string.app_name));

        return tContents;
    }

    private String getHtmlForRecord(Record pRecord) {
        String recordHtml =
                "<div>\n" +
                        "   <div class=\"new_line\">\n" +
                        "       <div class=\"alignleft\">\n" +
                        "           <a href='DOWNLOADTO_BE_REPLACED_ID' download>TO_BE_REPLACED_NAME</a>\n" +
                        "           <span class=\"secondary_textview\"> (TO_BE_REPLACED_FILESIZE)</span>\n" +
                        "       </div>\n" +
                        "       <div class=\"alignright\">\n" +
                        "           <span class=\"secondary_textview\">TO_BE_REPLACED_DURATION</span>\n" +
                        "       </div>\n" +
                        "   </div>\n" +
                        "   <div class=\"new_line\">\n" +
                        "       <div class=\"alignleft\">\n" +
                        "           <span class=\"secondary_textview\">TO_BE_REPLACED_CATEGORY</span>\n" +
                        "       </div>\n" +
                        "       <div class=\"alignright\">\n" +
                        "           <span class=\"secondary_textview\">TO_BE_REPLACED_DATE</span>\n" +
                        "       </div>\n" +
                        "   </div>\n" +
                        "</div>\n" +
                        "<div class='line_thin new_line'></div>\n";

        try {
            final MediaPlayer player = new MediaPlayer();
            player.setDataSource(pRecord.getPath());
            player.prepare();
            recordHtml = recordHtml.replace(RECORD_DURATION, FormatUtils.toReadableDuration(player.getDuration()));
        } catch (IOException e) {
            recordHtml = recordHtml.replace(RECORD_DURATION, "n/a");
            e.printStackTrace();
        }

        recordHtml = recordHtml.replace(RECORD_NAME, pRecord.getName());
        recordHtml = recordHtml.replace(RECORD_CATEGORY, pRecord.getCategory().getName());
        recordHtml = recordHtml.replace(RECORD_DATE, FormatUtils.toReadableDate(pRecord.getLastModified()));
        recordHtml = recordHtml.replace(RECORD_FILESIZE, FormatUtils.toReadableSize(pRecord.getFileSize()));
        recordHtml = recordHtml.replace(RECORD_ID, "" + pRecord.getId());

        return recordHtml;
    }

}
