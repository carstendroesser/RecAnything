package com.carstendroesser.audiorecorder.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

import com.carstendroesser.audiorecorder.R;

/**
 * Created by carstendrosser on 05.12.15.
 */
public class IconMapper {

    /**
     * Maps a given icon-id to the specific Drawable.
     *
     * @param pContext we need that
     * @param pId      the id of the icon
     * @return a Drawable of the icon
     */
    public static Drawable getIconById(Context pContext, int pId) {
        TypedArray icons = pContext.getResources().obtainTypedArray(R.array.icons);
        return icons.getDrawable(pId);
    }

}
