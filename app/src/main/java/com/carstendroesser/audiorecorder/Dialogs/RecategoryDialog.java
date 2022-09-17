package com.carstendroesser.audiorecorder.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.adapters.CategoriesListAdapter;
import com.carstendroesser.audiorecorder.models.Category;
import com.carstendroesser.audiorecorder.utils.DatabaseHelper;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RecategoryDialog extends AlertDialog {

    @Bind(R.id.categoriesRecyclerView)
    protected RecyclerView mRecyclerView;
    @Bind(R.id.buttonCancel)
    protected Button mButtonCancel;

    /**
     * Creates a new dialog to select a category.
     *
     * @param pContext  we need that
     * @param pListener listen to category clicks
     */
    public RecategoryDialog(Context pContext, final CategoriesListAdapter.OnCategorySelectedListener pListener) {
        super(pContext);

        // set style
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_recategory, null);
        setView(content);

        ButterKnife.bind(this, content);

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                dismiss();
            }
        });

        // load existing categories
        List categories = DatabaseHelper.getInstance(getContext()).getCategories();
        CategoriesListAdapter categoriesListAdapter = new CategoriesListAdapter(categories, false);

        // pass-trough click events
        categoriesListAdapter.setOnCategorySelectedListener(new CategoriesListAdapter.OnCategorySelectedListener() {
            @Override
            public void onCategorySelected(Category pCategory) {
                dismiss();
                pListener.onCategorySelected(pCategory);
            }
        });

        mRecyclerView.setAdapter(categoriesListAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

}
