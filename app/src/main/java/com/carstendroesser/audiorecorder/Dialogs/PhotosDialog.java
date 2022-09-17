package com.carstendroesser.audiorecorder.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.adapters.PhotosAdapter;
import com.carstendroesser.audiorecorder.models.Record;
import com.carstendroesser.audiorecorder.utils.DatabaseHelper;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by carsten on 15.05.17.
 */

public class PhotosDialog extends AlertDialog {

    @Bind(R.id.photosDialogTitle)
    protected TextView mTitleTextView;
    @Bind(R.id.viewPager)
    protected ViewPager mSquaredViewPager;

    /**
     * Creates a new dialog to show media of the given record.
     *
     * @param pContext         we need that
     * @param pRecord          the record to load the media for
     * @param pPreselectedItem the preselected media
     * @param pListener        notified if a mediaitem was deleted
     */
    public PhotosDialog(Context pContext, final Record pRecord, int pPreselectedItem, final OnMediaDeleteListener pListener) {
        super(pContext);

        // set style
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_photos, null);
        setView(content);

        ButterKnife.bind(this, content);

        // cancel button
        content.findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                dismiss();
            }
        });

        // delete button
        content.findViewById(R.id.photosDialogDeleteImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                dismiss();
                new ConfirmDialog(getContext(), R.string.confirm_delete_media_title, R.string.media_delete_message, R.string.confirm_delete_media_button, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface pDialog, int pWhich) {
                        pDialog.dismiss();
                        if (DatabaseHelper.getInstance(getContext()).deleteMedia(pRecord.getMediaList().get(mSquaredViewPager.getCurrentItem()))) {
                            // success
                            pListener.onMediaDeleted(true);
                        } else {
                            // error
                            pListener.onMediaDeleted(false);
                        }
                    }
                }).show();
            }
        });

        // update position indicator
        mSquaredViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int pPosition, float pPositionOffset, int pPositionOffsetPixels) {
                // empty
            }

            @Override
            public void onPageSelected(int pPosition) {
                mTitleTextView.setText("" + (pPosition + 1) + "/" + pRecord.getMediaList().size());
            }

            @Override
            public void onPageScrollStateChanged(int pState) {
                // empty
            }
        });

        // setup the viewpager
        PhotosAdapter adapter = new PhotosAdapter(pRecord.getMediaList());
        mSquaredViewPager.setAdapter(adapter);
        mSquaredViewPager.setCurrentItem(pPreselectedItem);
        mSquaredViewPager.setPageMargin(getContext().getResources().getDimensionPixelSize(R.dimen.divider_height));

        mTitleTextView.setText("" + (pPreselectedItem + 1) + "/" + pRecord.getMediaList().size());
    }

    /**
     * Used to notify if a mediaitem was deleted.
     */
    public interface OnMediaDeleteListener {

        /**
         * Called when a mediaitem was about to get deleted.
         *
         * @param pDeleted true if deleted, otherwise false
         */
        void onMediaDeleted(boolean pDeleted);
    }

}
