package com.carstendroesser.audiorecorder.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by carstendrosser on 19.05.17.
 */

public class KeyboardHandler {

    public static void showKeyboard(View pView) {
        pView.requestFocus();
        InputMethodManager imm = (InputMethodManager) pView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(pView, 0);
    }

    public static void hideKeyboard(View pView) {
        InputMethodManager imm = (InputMethodManager) pView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(pView.getWindowToken(), 0);
        pView.clearFocus();
    }

}
