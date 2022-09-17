package com.carstendroesser.audiorecorder.adapters;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.models.Category;
import com.carstendroesser.audiorecorder.utils.IconMapper;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by carstendrosser on 05.11.15.
 */
public class CategoriesListAdapter extends RecyclerView.Adapter<CategoriesListAdapter.CategoryViewHolder> {

    private final boolean mCategoriesEditable;
    private ArrayList<Category> mCategories;
    private OnCategorySelectedListener mOnCategorySelectedListener;
    private OnOptionsViewClickListener mOnOptionsViewClickListener;

    /**
     * Creates a new ListAdapter for Categories.
     *
     * @param pCategories         the categories to show in the list
     * @param pCategoriesEditable wether to show the deleteview or not
     */
    public CategoriesListAdapter(List<Category> pCategories, boolean pCategoriesEditable) {
        mCategories = new ArrayList<>(pCategories);
        mCategoriesEditable = pCategoriesEditable;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup pParent, int pViewType) {
        // inflate view
        LayoutInflater layoutInflater = LayoutInflater.from(pParent.getContext());
        View view = layoutInflater.inflate(R.layout.listitem_category, pParent, false);

        // check for delete-view visibility
        if (!mCategoriesEditable) {
            view.findViewById(R.id.categoryListItemEditImageView).setVisibility(GONE);
            view.findViewById(R.id.categoryListItemDeleteImageView).setVisibility(GONE);
            view.findViewById(R.id.categoryListItemIconImageView).setVisibility(GONE);
        } else {
            view.setBackground(new ColorDrawable(Color.TRANSPARENT));
        }

        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder pViewHolder, final int pPosition) {
        // get the category
        final Category currentCategory = mCategories.get(pPosition);

        // get icon for the category
        Drawable icon = IconMapper.getIconById(pViewHolder.itemView.getContext(), currentCategory.getIcon());

        // set content
        pViewHolder.iconView.setImageDrawable(icon);
        pViewHolder.nameView.setText(currentCategory.getName());
        pViewHolder.descriptionView.setText(currentCategory.getDescription());

        if (pPosition == 0) {
            pViewHolder.deleteView.setVisibility(GONE);
        } else if (mCategoriesEditable) {
            pViewHolder.deleteView.setVisibility(VISIBLE);
        }

        // on a listitem-click, notify the listener
        pViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                if (mOnCategorySelectedListener != null) {
                    mOnCategorySelectedListener.onCategorySelected(mCategories.get(pPosition));
                }
            }
        });

        pViewHolder.editView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                if (mOnOptionsViewClickListener != null) {
                    mOnOptionsViewClickListener.onCategoryEditClick(pView, currentCategory);
                }
            }
        });

        pViewHolder.deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                if (mOnOptionsViewClickListener != null) {
                    mOnOptionsViewClickListener.onCategoryDeleteClick(pView, currentCategory);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mCategories.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {

        ImageView iconView;
        TextView nameView;
        TextView descriptionView;
        ImageView editView;
        ImageView deleteView;

        public CategoryViewHolder(View pItemView) {
            super(pItemView);
            iconView = (ImageView) pItemView.findViewById(R.id.categoryListItemIconImageView);
            nameView = (TextView) pItemView.findViewById(R.id.categoryListItemNameTextView);
            descriptionView = (TextView) pItemView.findViewById(R.id.categoryListItemDescriptionTextView);
            editView = (ImageView) pItemView.findViewById(R.id.categoryListItemEditImageView);
            deleteView = (ImageView) pItemView.findViewById(R.id.categoryListItemDeleteImageView);
        }

    }

    /**
     * Sets a listener which is notified whenever a listitem is clicked.
     *
     * @param pListener the listener to notify
     */
    public void setOnCategorySelectedListener(OnCategorySelectedListener pListener) {
        mOnCategorySelectedListener = pListener;
    }

    /**
     * Sets a listener which is notified whenever one of a listitem's optionsview is clicked.
     *
     * @param pListener the listener to notify
     */
    public void setOnOptionsViewClickListener(OnOptionsViewClickListener pListener) {
        mOnOptionsViewClickListener = pListener;
    }

    /**
     * Listener to use for listitem-clicks and getting the clicked category.
     */
    public interface OnCategorySelectedListener {
        /**
         * Called on a listclick.
         *
         * @param pCategory the clicked category
         */
        void onCategorySelected(Category pCategory);
    }

    public interface OnOptionsViewClickListener {
        /**
         * Called when an edit view was clicked.
         *
         * @param pView     the clicked view
         * @param pCategory the category
         */
        void onCategoryEditClick(View pView, Category pCategory);

        /**
         * Called when an delete view was clicked.
         *
         * @param pView     the clicked view
         * @param pCategory the category
         */
        void onCategoryDeleteClick(View pView, Category pCategory);
    }

}
