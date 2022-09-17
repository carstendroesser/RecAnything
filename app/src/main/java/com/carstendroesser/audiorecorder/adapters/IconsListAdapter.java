package com.carstendroesser.audiorecorder.adapters;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.carstendroesser.audiorecorder.R;

/**
 * Created by carstendrosser on 05.12.15.
 */
public class IconsListAdapter extends RecyclerView.Adapter<IconsListAdapter.ViewHolder> {

    private TypedArray mIcons;
    private OnIconClickListener mOnIconClickListener;

    private int mSelectedPosition = -1;

    /**
     * Creates a new adapter to use for showing icons-
     *
     * @param pIcons the icons to show in a list
     */
    public IconsListAdapter(TypedArray pIcons, OnIconClickListener pListener) {
        mIcons = pIcons;
        mOnIconClickListener = pListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup pParent, int pViewType) {
        // create view
        LayoutInflater layoutInflater = LayoutInflater.from(pParent.getContext());
        View view = layoutInflater.inflate(R.layout.listitem_icon, pParent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder pHolder, final int pPosition) {

        if (pPosition == mSelectedPosition) {
            pHolder.backgroundView.setVisibility(View.VISIBLE);
        } else {
            pHolder.backgroundView.setVisibility(View.GONE);
        }

        // get the current icon-drawable
        Drawable icon = mIcons.getDrawable(pPosition);

        // set drawable into imageview
        pHolder.iconImageView.setImageDrawable(icon);

        // setup clicklistener
        pHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                int oldSelectedPosition = mSelectedPosition;
                mSelectedPosition = pPosition;
                pHolder.backgroundView.setVisibility(View.VISIBLE);
                pHolder.backgroundView.startAnimation(AnimationUtils.loadAnimation(pHolder.itemView.getContext(), R.anim.fade_in));
                notifyItemChanged(oldSelectedPosition);
                if (mOnIconClickListener != null) {
                    mOnIconClickListener.onIconClick(pPosition);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mIcons.length();
    }

    public int getSelectedIconPosition() {
        return mSelectedPosition;
    }

    public void setSelectedIcon(int pSelectedIcon) {
        mSelectedPosition = pSelectedIcon;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView iconImageView;
        View backgroundView;

        public ViewHolder(View pView) {
            super(pView);
            iconImageView = (ImageView) pView.findViewById(R.id.iconImageView);
            backgroundView = pView.findViewById(R.id.iconBackgroundView);
        }

    }

    /**
     * Listener to receive callback whenever an icon is clicked.
     */
    public interface OnIconClickListener {
        /**
         * Fired whenever an icon is clicked.
         *
         * @param pId the id of the clicked icon
         */
        void onIconClick(int pId);
    }

}
