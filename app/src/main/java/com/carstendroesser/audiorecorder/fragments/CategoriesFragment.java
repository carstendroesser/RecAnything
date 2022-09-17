package com.carstendroesser.audiorecorder.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.adapters.CategoriesListAdapter;
import com.carstendroesser.audiorecorder.adapters.CategoriesListAdapter.OnCategorySelectedListener;
import com.carstendroesser.audiorecorder.models.Category;
import com.carstendroesser.audiorecorder.utils.DatabaseHelper;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by carstendrosser on 05.11.15.
 */
public class CategoriesFragment extends Fragment implements CategoriesListAdapter.OnCategorySelectedListener {

    @Bind(R.id.categoriesLoadingIndicator)
    ProgressBar mLoadingIndicator;
    @Bind(R.id.categoriesRecyclerView)
    RecyclerView mRecyclerView;

    //used to notify whenever a listitem was clicked
    private OnCategorySelectedListener mOnCategorySelectedListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_categories, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        //show the loadingindicator, because we are loading the categories
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        final Handler handler = new Handler();

        //load the categories async
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<Category> categories = DatabaseHelper.getInstance(getContext()).getCategories();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //create new adapter
                        CategoriesListAdapter categoriesListAdapter = new CategoriesListAdapter(categories, false);
                        //we want to be notified if a category was clicked
                        categoriesListAdapter.setOnCategorySelectedListener(CategoriesFragment.this);
                        //show recyclerView
                        mLoadingIndicator.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        //set adapter
                        mRecyclerView.setAdapter(categoriesListAdapter);
                    }
                });
            }
        }).start();

    }

    @Override
    public void onCategorySelected(Category pCategory) {
        if (mOnCategorySelectedListener != null) {
            //pass the clicked category to listeners
            mOnCategorySelectedListener.onCategorySelected(pCategory);
        }
    }

    /**
     * Sets a listener which is notified whenever a listitem is clicked.
     *
     * @param pListener the listener to notify
     */
    public void setOnCategorySelectListener(CategoriesListAdapter.OnCategorySelectedListener pListener) {
        mOnCategorySelectedListener = pListener;
    }
}
