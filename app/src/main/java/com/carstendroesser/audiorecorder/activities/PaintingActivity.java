package com.carstendroesser.audiorecorder.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.carstendroesser.audiorecorder.Dialogs.ColorsDialog;
import com.carstendroesser.audiorecorder.Dialogs.ConfirmDialog;
import com.carstendroesser.audiorecorder.Dialogs.ThicknessDialog;
import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.utils.FileFactory;
import com.carstendroesser.audiorecorder.views.ContextMenuItem;
import com.carstendroesser.audiorecorder.views.PaintView;

import java.io.FileNotFoundException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by carstendrosser on 24.11.15.
 */
public class PaintingActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    protected Toolbar mToolbar;
    @Bind(R.id.paintView)
    protected PaintView mPaintView;
    @Bind(R.id.paintingUndoView)
    protected ContextMenuItem mUndoView;

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);
        setContentView(R.layout.activity_painting);
        ButterKnife.bind(this);

        mUndoView.setVisibility(GONE);

        mPaintView.setOnDrawnListener(new PaintView.OnDrawingListener() {
            @Override
            public void onNewDrawn() {
                if (mPaintView.canUndo()) {
                    mUndoView.setVisibility(VISIBLE);
                } else {
                    mUndoView.setVisibility(GONE);
                }
            }
        });

        // setup toolbar
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        mToolbar.setTitle(R.string.new_painting);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                new ConfirmDialog(PaintingActivity.this, R.string.discard, R.string.painting_back_confirm, R.string.confirm_discard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface pDialog, int pWhich) {
                        PaintingActivity.super.onBackPressed();
                    }
                }).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu pMenu) {
        getMenuInflater().inflate(R.menu.menu_painting, pMenu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem pItem) {
        int id = pItem.getItemId();

        // we want to save the painting
        if (id == R.id.action_save) {
            new ConfirmDialog(this, R.string.confirm_painting_save_title, R.string.cofirm_painting_save_message, R.string.confirm_painting_save_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface pDialog, int pWhich) {
                    String filepath = FileFactory.createPaintingFilePath(getApplicationContext());
                    try {
                        mPaintView.save(filepath);
                        // "finish activity with result"
                        finishWith(filepath);
                    } catch (FileNotFoundException pException) {
                        // something went wrong
                        new AlertDialog.Builder(PaintingActivity.this)
                                .setTitle(R.string.save_painting_error_title)
                                .setMessage(R.string.save_painting_error_message + "\n\n" + pException.getMessage())
                                .setPositiveButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface pDialog, int pWhich) {
                                        pDialog.dismiss();
                                        finish();
                                    }
                                })
                                .create()
                                .show();
                    }
                }
            }).show();
        }

        return super.onOptionsItemSelected(pItem);
    }

    /**
     * Finishes the PaintingActivity and returns the filepath as <i>paintingFilepath</i>
     * as result.
     *
     * @param pFilepath the path the painting is stored
     */
    private void finishWith(String pFilepath) {
        Intent data = new Intent();
        data.putExtra("paintingFilepath", pFilepath);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * Opens a dialog to select the color for the PaintView.
     */
    @OnClick(R.id.paintingColorView)
    protected void onSelectColorViewClick() {
        new ColorsDialog(this, mPaintView.getColor(), new ColorsDialog.OnColorSelectListener() {
            @Override
            public void onColorSelect(int pColor) {
                mPaintView.setColor(pColor);
            }
        }).show();
    }

    /**
     * Opens a dialog to select the thickness for the paintingview.
     */
    @OnClick(R.id.paintingThicknessView)
    protected void onSelectThicknessViewClick() {
        new ThicknessDialog(this, mPaintView.getStrokeWidth(), new ThicknessDialog.OnThicknessSelectListener() {
            @Override
            public void onThicknessSelect(int pThickness) {
                mPaintView.setStrokeWidth(pThickness);
            }
        }).show();
    }

    /**
     * Undos the last painting-action.
     */
    @OnClick(R.id.paintingUndoView)
    protected void onUndoViewClick() {
        if (mPaintView.canUndo()) {
            new ConfirmDialog(this, R.string.confirm_painting_undo_title, R.string.undo_confirm, R.string.undo_confirm_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface pDialog, int pWhich) {
                    pDialog.dismiss();
                    mPaintView.undo();
                    if (mPaintView.canUndo()) {
                        mUndoView.setVisibility(VISIBLE);
                    } else {
                        mUndoView.setVisibility(GONE);
                    }
                }
            }).show();
        } else {
            Snackbar.make(mPaintView, R.string.undo_error, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Clears everything inside the PaintView.
     */
    @OnClick(R.id.paintingClearView)
    protected void onClearViewClick() {
        new ConfirmDialog(this, R.string.confirm_painting_clear_title, R.string.clear_confirm, R.string.clear_confirm_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface pDialog, int pWhich) {
                pDialog.dismiss();
                mPaintView.clear();
                if (mPaintView.canUndo()) {
                    mUndoView.setVisibility(VISIBLE);
                } else {
                    mUndoView.setVisibility(GONE);
                }
            }
        }).show();
    }

    @Override
    public void onBackPressed() {
        new ConfirmDialog(PaintingActivity.this, R.string.discard, R.string.painting_back_confirm, R.string.confirm_discard, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface pDialog, int pWhich) {
                PaintingActivity.super.onBackPressed();
            }
        }).show();
    }
}
