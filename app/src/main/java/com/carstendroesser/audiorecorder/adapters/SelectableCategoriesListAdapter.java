package com.carstendroesser.audiorecorder.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.models.Category;

import java.util.ArrayList;

/**
 * Created by carstendrosser on 05.11.15.
 */
public class SelectableCategoriesListAdapter extends RecyclerView.Adapter<SelectableCategoriesListAdapter.SelectableCategoryViewHolder> {

    private ArrayList<Category> mCategories;
    private Toast mErrorToast;

    public SelectableCategoriesListAdapter(Context pContext) {
        mCategories = new ArrayList<>();
        mErrorToast = Toast.makeText(pContext, R.string.at_least_one_selection, Toast.LENGTH_SHORT);
    }

    @Override
    public SelectableCategoryViewHolder onCreateViewHolder(ViewGroup pParent, int pViewType) {
        // inflate view
        LayoutInflater layoutInflater = LayoutInflater.from(pParent.getContext());
        View view = layoutInflater.inflate(R.layout.listitem_selectable_category, pParent, false);

        return new SelectableCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SelectableCategoryViewHolder pViewHolder, final int pPosition) {
        pViewHolder.nameView.setText(mCategories.get(pPosition).getName());
        pViewHolder.checkBox.setOnCheckedChangeListener(null);
        pViewHolder.checkBox.setChecked(mCategories.get(pPosition).getIsSelected());
        pViewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton pButtonView, boolean pIsChecked) {
                // check if it is the last checked category
                // and prevent from unchecking it
                if (!pIsChecked) {
                    if (getUnselectedCategories().size() == mCategories.size() - 1) {
                        pButtonView.setChecked(!pIsChecked);
                        mErrorToast.setText(R.string.at_least_one_selection);
                        mErrorToast.show();
                        return;
                    }
                }

                mCategories.get(pPosition).setSelected(pIsChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCategories.size();
    }

    /**
     * Sets a new list of categories.
     *
     * @param pList the categories
     */
    public void updateList(ArrayList<Category> pList) {
        mCategories.clear();
        mCategories.addAll(pList);
        notifyDataSetChanged();
    }

    /**
     * Return a sublist with categories that are unselected.
     *
     * @return a list of all categories that are unselected
     */
    public ArrayList<Category> getUnselectedCategories() {
        ArrayList<Category> unselectedCategories = new ArrayList<>();
        for (Category category : mCategories) {
            if (!category.getIsSelected()) {
                unselectedCategories.add(category);
            }
        }
        return unselectedCategories;
    }

    public class SelectableCategoryViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkBox;
        TextView nameView;

        public SelectableCategoryViewHolder(View pItemView) {
            super(pItemView);
            checkBox = (CheckBox) pItemView.findViewById(R.id.selectableCategoryListItemCheckBox);
            nameView = (TextView) pItemView.findViewById(R.id.selectableCategoryListItemTextView);
        }

    }
}
