package com.carstendroesser.audiorecorder.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import com.carstendroesser.audiorecorder.Dialogs.CategoryDialog;
import com.carstendroesser.audiorecorder.Dialogs.ConfirmDialog;
import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.adapters.CategoriesListAdapter;
import com.carstendroesser.audiorecorder.models.Category;
import com.carstendroesser.audiorecorder.models.Record;
import com.carstendroesser.audiorecorder.utils.DatabaseHelper;
import com.carstendroesser.audiorecorder.utils.Preferences;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by carstendrosser on 30.11.15.
 */
public class CategoriesActivity extends AppCompatActivity implements CategoriesListAdapter.OnOptionsViewClickListener, CategoryDialog.OnCategoryCreateListener, CategoryDialog.OnCategoryEditListener {

    @Bind(R.id.toolbar)
    protected Toolbar mToolbar;
    @Bind(R.id.categoriesLoadingIndicator)
    protected ProgressBar mLoadingIndicator;
    @Bind(R.id.categoriesRecyclerView)
    protected RecyclerView mRecyclerView;

    private boolean mCategoriesChanged = false;
    private boolean mUpdateFilters = false;

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);
        setContentView(R.layout.activity_categories);
        ButterKnife.bind(this);

        //setup the toolbar and up-navigation
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        mToolbar.setTitle(getResources().getString(R.string.categories_manage));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                onBackPressed();
            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        loadCategories();
    }

    /**
     * Loads the category into the RecyclerView
     */
    private void loadCategories() {

        // show loadingindicator
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mLoadingIndicator.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        mRecyclerView.setVisibility(View.GONE);

        final Handler handler = new Handler();

        // dont block ui
        new Thread(new Runnable() {
            @Override
            public void run() {
                // get categories
                final ArrayList<Category> categories = DatabaseHelper.getInstance(getApplicationContext()).getCategories();
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
                        mRecyclerView.setVisibility(View.VISIBLE);
                        mRecyclerView.startAnimation(AnimationUtils.loadAnimation(CategoriesActivity.this, R.anim.fade_in));

                        // create adapter
                        CategoriesListAdapter categoriesListAdapter = new CategoriesListAdapter(categories, true);

                        // receive clickevents
                        categoriesListAdapter.setOnOptionsViewClickListener(CategoriesActivity.this);

                        // set adapter
                        mRecyclerView.setAdapter(categoriesListAdapter);
                    }
                });
            }
        }).start();

    }

    @Override
    public void onCategoryEditClick(View pView, Category pCategory) {
        editCategoryIntent(pCategory);
    }

    @Override
    public void onCategoryDeleteClick(View pView, Category pCategory) {
        deleteCategoryIntent(pCategory);
    }

    @OnClick(R.id.addCategoryImageView)
    protected void onAddCategoryClick() {
        new CategoryDialog(this, this).show();
    }

    private void editCategoryIntent(final Category pCategory) {
        new CategoryDialog(this, pCategory, this).show();
    }

    private void deleteCategoryIntent(final Category pCategory) {
        // show deletedialog
        ConfirmDialog confirmDialog = new ConfirmDialog(this, R.string.confirm_delete_category, R.string.delete_category_dialog_message, R.string.confirm_delete_category_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface pDialog, int pWhich) {
                int snackbarText;

                // check if it is the default category
                if (pCategory.getId() == 1) {
                    snackbarText = R.string.delete_default_category;
                } else {
                    // get records within this category
                    DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
                    List<Record> recordsInCategory = databaseHelper.getRecordsWithCategoryId(pCategory.getId(), Preferences.getRecordingsSorting(getApplicationContext()));
                    int size = recordsInCategory.size();

                    // if no records, try to delete
                    if (size == 0) {
                        if (databaseHelper.deleteCategory(pCategory)) {
                            loadCategories();
                            snackbarText = R.string.category_deleted;
                            mCategoriesChanged = true;
                            if (databaseHelper.getUnselectedCatgoriesList().size() > 0) {
                                mUpdateFilters = true;
                                databaseHelper.clearUnselectedCategories();
                            }
                        } else {
                            snackbarText = R.string.delete_category_default_error;
                        }
                    } else {
                        snackbarText = R.string.delete_category_error_exists;
                    }
                }

                Snackbar.make(mRecyclerView, snackbarText, Snackbar.LENGTH_SHORT).show();
                pDialog.dismiss();
            }
        });
        confirmDialog.show();
    }

    @Override
    public void onCategoryCreate(boolean pSuccess) {
        if (pSuccess) {
            loadCategories();
            Snackbar.make(mRecyclerView, R.string.create_category_success, Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(mRecyclerView, R.string.create_category_error, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCategoryEdited(boolean pSuccess) {
        if (pSuccess) {
            loadCategories();
            Snackbar.make(mRecyclerView, R.string.edit_category_success, Snackbar.LENGTH_SHORT).show();
            mCategoriesChanged = true;
            if (DatabaseHelper.getInstance(this).getUnselectedCatgoriesList().size() > 0) {
                // onyl delte the filters if there were filters applied
                mUpdateFilters = true;
                DatabaseHelper.getInstance(this).clearUnselectedCategories();
            }
        } else {
            Snackbar.make(mRecyclerView, R.string.edit_category_error, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        Bundle extras = new Bundle();
        extras.putBoolean("categoriesChanged", mCategoriesChanged);
        extras.putBoolean("updateFilters", mUpdateFilters);
        data.putExtras(extras);
        setResult(RESULT_OK, data);
        finish();
    }
}
