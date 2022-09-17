package com.carstendroesser.audiorecorder.Dialogs;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.views.ContextMenuItem;
import com.carstendroesser.audiorecorder.views.RevealFrameLayout;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.view.View.GONE;

public class ContextMenuDialog extends AlertDialog implements View.OnClickListener {

    @Bind(R.id.contextMenuItemRename)
    protected ContextMenuItem mItemRename;
    @Bind(R.id.contextMenuItemRecategory)
    protected ContextMenuItem mItemRecategory;
    @Bind(R.id.contextMenuItemShare)
    protected ContextMenuItem mItemShare;
    @Bind(R.id.contextMenuItemMore)
    protected ContextMenuItem mItemMore;
    @Bind(R.id.contextMenuItemExternalPlayer)
    protected ContextMenuItem mItemExternalPlayer;
    @Bind(R.id.contextMenuItemTrim)
    protected ContextMenuItem mItemTrim;
    @Bind(R.id.contextMenuItemDelete)
    protected ContextMenuItem mItemDelete;
    @Bind(R.id.contextMenuItemNote)
    protected ContextMenuItem mItemNote;
    @Bind(R.id.contextMenuItemTakePhoto)
    protected ContextMenuItem mItemTakePhoto;
    @Bind(R.id.contextMenuItemPickPhoto)
    protected ContextMenuItem mItemPickPhoto;
    @Bind(R.id.contextMenuItemPainting)
    protected ContextMenuItem mItemPainting;
    @Bind(R.id.contextMenuItemAbout)
    protected ContextMenuItem mItemAbout;
    @Bind(R.id.contextMenuListContainer)
    protected LinearLayout mListContainer;
    @Bind(R.id.contextMenuListContainerExpansion)
    protected View mListContainerExpansion;
    @Bind(R.id.contextMenuNoteHint)
    protected View mNoteHintView;
    @Bind(R.id.contextMenuItemSetRingtone)
    protected ContextMenuItem mItemSetRingtone;

    private View.OnClickListener mOnMenuItemClickListener;

    public ContextMenuDialog(Context pContext, boolean pIsWavRecording, boolean pShowNote, View.OnClickListener pOnClickListener) {
        super(pContext);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_contextmenu, null);
        setView(content);

        ButterKnife.bind(this, content);

        mOnMenuItemClickListener = pOnClickListener;

        mItemRename.setOnClickListener(this);
        mItemRecategory.setOnClickListener(this);
        mItemShare.setOnClickListener(this);
        mItemMore.setOnClickListener(this);
        mItemExternalPlayer.setOnClickListener(this);
        mItemTrim.setOnClickListener(this);
        mItemNote.setOnClickListener(this);
        mItemTakePhoto.setOnClickListener(this);
        mItemPickPhoto.setOnClickListener(this);
        mItemPainting.setOnClickListener(this);
        mItemDelete.setOnClickListener(this);
        mItemAbout.setOnClickListener(this);
        mItemSetRingtone.setOnClickListener(this);

        // (not) show note item
        if (!pShowNote) {
            mItemNote.setVisibility(GONE);
            mNoteHintView.setVisibility(View.VISIBLE);
        } else {
            mItemNote.setVisibility(View.VISIBLE);
            mNoteHintView.setVisibility(GONE);
        }

        if (!pIsWavRecording) {
            mItemTrim.setVisibility(GONE);
        }
    }

    @Override
    public void onClick(View pView) {
        // pass trough click if not "more" item was clicked
        if (pView.getId() == R.id.contextMenuItemMore) {
            mItemMore.setVisibility(GONE);
            mListContainer.setLayoutTransition(new LayoutTransition());
            mListContainerExpansion.setVisibility(View.VISIBLE);
        } else {
            mOnMenuItemClickListener.onClick(pView);
        }
    }

}
