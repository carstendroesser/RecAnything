package com.carstendroesser.audiorecorder.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.adapters.SelectableCategoriesListAdapter;
import com.carstendroesser.audiorecorder.models.Category;
import com.carstendroesser.audiorecorder.utils.DatabaseHelper;
import com.carstendroesser.audiorecorder.views.RevealFrameLayout;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FilterDialog extends AlertDialog {

    @Bind(R.id.selectableCategoriesRecyclerView)
    protected RecyclerView mSelectableCategoriesRecyclerView;
    @Bind(R.id.buttonCancel)
    protected Button mButtonCancel;
    @Bind(R.id.buttonApply)
    protected Button mButtonApply;
    @Bind(R.id.selectableCategoriesLoadingIndicator)
    protected ProgressBar mLoadingIndicator;

    private SelectableCategoriesListAdapter mAdapter;

    /**
     * Creates a dialog with a selectable list of categories to filter the
     * Databasequeries.
     *
     * @param pContext  we need that
     * @param pListener when the apply button is clicked
     */
    public FilterDialog(Context pContext, final OnApplyClickListener pListener) {
        super(pContext);

        // set style
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_filter, null);
        setView(content);

        ButterKnife.bind(this, content);

        mAdapter = new SelectableCategoriesListAdapter(getContext());

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                dismiss();
            }
        });

        mButtonApply.setEnabled(false);
        mButtonApply.setTextColor(getContext().getResources().getColor(R.color.gray));
        mButtonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                // update the list of unselected categories in the databasehelper
                DatabaseHelper.getInstance(getContext()).setUnselectedCategories(mAdapter.getUnselectedCategories());
                dismiss();
                if (pListener != null) {
                    // notify the listener to update the recordslist
                    pListener.onApplyClick();
                }
            }
        });

        mSelectableCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        loadCategories();
    }

    private void loadCategories() {
        final Handler handler = new Handler();

        mSelectableCategoriesRecyclerView.setVisibility(View.GONE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mLoadingIndicator.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));

        // dont block ui
        new Thread(new Runnable() {
            @Override
            public void run() {

                // get all categories
                final ArrayList<Category> categories = DatabaseHelper.getInstance(getContext()).getCategories();
                // get the unselected categories
                ArrayList<Category> unselectedCategories = DatabaseHelper.getInstance(getContext()).getUnselectedCatgoriesList();

                // check which category is also in the unselected categories
                for (Category category : categories) {
                    for (Category unselectedCategory : unselectedCategories) {
                        if (category.getId() == unselectedCategory.getId()) {
                            category.setSelected(false);
                        }
                    }
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // show recyclerview
                        mLoadingIndicator.setVisibility(View.GONE);
                        mSelectableCategoriesRecyclerView.setVisibility(View.VISIBLE);
                        mSelectableCategoriesRecyclerView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));

                        // create adapter
                        mAdapter.updateList(categories);

                        // set adapter
                        mSelectableCategoriesRecyclerView.setAdapter(mAdapter);

                        mButtonApply.setEnabled(true);
                        mButtonApply.setTextColor(getContext().getResources().getColor(R.color.black));
                    }
                });
            }
        }).start();
    }

    /**
     * Listener used to listen to the apply button. The list of records should be
     * updated.
     */
    public interface OnApplyClickListener {
        /**
         * The applybutton was clicked.
         */
        void onApplyClick();
    }

}
