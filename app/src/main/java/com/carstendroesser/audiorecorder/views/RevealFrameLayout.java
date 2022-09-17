package com.carstendroesser.audiorecorder.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.FrameLayout;

import static com.carstendroesser.audiorecorder.views.RevealFrameLayout.Position.BEGIN;
import static com.carstendroesser.audiorecorder.views.RevealFrameLayout.Position.END;
import static com.carstendroesser.audiorecorder.views.RevealFrameLayout.Position.MID;

/**
 * Created by carstendrosser on 06.02.16.
 */
public class RevealFrameLayout extends FrameLayout {

    public enum Position {
        BEGIN,
        MID,
        END
    }

    public RevealFrameLayout(Context context) {
        super(context);
    }

    public RevealFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RevealFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RevealFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void animateIn(Position pPositionX, Position pPositionY) {

        int cx = 0;
        int cy = 0;
        int finalRadius = 0;

        switch (pPositionX) {
            case BEGIN:
                cx = 0;
                break;
            case MID:
                cx = getWidth() / 2;
                break;
            case END:
                cx = getWidth();
                break;
        }

        switch (pPositionY) {
            case BEGIN:
                cy = 0;
                break;
            case MID:
                cy = getHeight() / 2;
                break;
            case END:
                cy = getHeight();
                break;
        }

        if (pPositionX == BEGIN && pPositionY == BEGIN
                || pPositionX == BEGIN && pPositionY == END
                || pPositionX == END && pPositionY == BEGIN
                || pPositionX == END && pPositionY == END) {
            finalRadius = (int) Math.sqrt(Math.pow(getWidth(), 2) + Math.pow(getHeight(), 2));
        } else if (pPositionX == MID && pPositionY == BEGIN
                || pPositionX == MID && pPositionY == END
                || pPositionX == BEGIN && pPositionY == MID
                || pPositionX == END && pPositionY == MID) {
            finalRadius = (int) Math.sqrt(Math.pow(getHeight(), 2) + Math.pow(getWidth() / 2, 2));
        } else if (pPositionX == MID && pPositionY == MID) {
            finalRadius = (int) Math.sqrt(Math.pow(getWidth() / 2, 2) + Math.pow(getHeight() / 2, 2));
        }

        if (isAttachedToWindow()) {
            Animator anim = ViewAnimationUtils.createCircularReveal(this, cx, cy, 0, finalRadius);
            setVisibility(View.VISIBLE);
            anim.start();
        }
    }

    public void animateOut(Position pPositionX, Position pPositionY) {

        int cx = 0;
        int cy = 0;
        int finalRadius = 0;

        switch (pPositionX) {
            case BEGIN:
                cx = 0;
                break;
            case MID:
                cx = getWidth() / 2;
                break;
            case END:
                cx = getWidth();
                break;
        }

        switch (pPositionY) {
            case BEGIN:
                cy = 0;
                break;
            case MID:
                cy = getHeight() / 2;
                break;
            case END:
                cy = getHeight();
                break;
        }

        if (pPositionX == BEGIN && pPositionY == BEGIN
                || pPositionX == BEGIN && pPositionY == END
                || pPositionX == END && pPositionY == BEGIN
                || pPositionX == END && pPositionY == END) {
            finalRadius = (int) Math.sqrt(Math.pow(getWidth(), 2) + Math.pow(getHeight(), 2));
        } else if (pPositionX == MID && pPositionY == BEGIN
                || pPositionX == MID && pPositionY == END
                || pPositionX == BEGIN && pPositionY == MID
                || pPositionX == END && pPositionY == MID) {
            finalRadius = (int) Math.sqrt(Math.pow(getHeight(), 2) + Math.pow(getWidth() / 2, 2));
        } else if (pPositionX == MID && pPositionY == MID) {
            finalRadius = (int) Math.sqrt(Math.pow(getWidth() / 2, 2) + Math.pow(getHeight() / 2, 2));
        }

        if (isAttachedToWindow()) {
            Animator anim = ViewAnimationUtils.createCircularReveal(this, cx, cy, finalRadius, 0);

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    setVisibility(View.GONE);
                }
            });
            anim.start();
        }
    }

}
