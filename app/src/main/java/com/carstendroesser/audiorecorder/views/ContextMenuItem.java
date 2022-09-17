package com.carstendroesser.audiorecorder.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.carstendroesser.audiorecorder.R;


public class ContextMenuItem extends LinearLayout {

    public ContextMenuItem(Context pContext) {
        this(pContext, null);
    }

    public ContextMenuItem(Context pContext, AttributeSet pAttrs) {
        this(pContext, pAttrs, 0);
    }

    public ContextMenuItem(Context pContext, AttributeSet pAttrs, int pDefStyleAttr) {
        super(pContext, pAttrs, pDefStyleAttr);

        inflate(pContext, R.layout.listitem_contextmenu, this);

        TypedArray typedArray = getContext().obtainStyledAttributes(pAttrs, R.styleable.ContextMenuItem, 0, 0);
        ((ImageView) findViewById(R.id.contextMenuItemImageView)).setImageDrawable(typedArray.getDrawable(R.styleable.ContextMenuItem_itemIcon));
        ((TextView) findViewById(R.id.contextMenuItemTextView)).setText(typedArray.getString(R.styleable.ContextMenuItem_itemText));
        ((TextView) findViewById(R.id.contextMenuItemTextView)).setTextColor(typedArray.getInt(R.styleable.ContextMenuItem_itemAlternativeTextColor, R.color.black));

        if (typedArray.getBoolean(R.styleable.ContextMenuItem_itemSpacing, true) == false) {
            findViewById(R.id.contextMenuItemSpace).setVisibility(View.GONE);
        }

        typedArray.recycle();
    }

}
