package com.carstendroesser.audiorecorder.adapters;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.models.Media;
import com.carstendroesser.audiorecorder.views.SquaredImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carsten on 15.05.17.
 */

public class PhotosAdapter extends PagerAdapter {

    private ArrayList<Media> mMediaList;

    /**
     * Constructs a new Photosadapter with the given medialist as dataset.
     *
     * @param pMediaList the dataset
     */
    public PhotosAdapter(List<Media> pMediaList) {
        mMediaList = new ArrayList<>();
        mMediaList.addAll(pMediaList);
    }

    @Override
    public Object instantiateItem(ViewGroup pContainer, int pPosition) {
        View view = LayoutInflater.from(pContainer.getContext()).inflate(R.layout.listitem_photos, pContainer, false);
        SquaredImageView imageView = (SquaredImageView) view.findViewById(R.id.imageView);
        Glide.with(pContainer.getContext()).load(mMediaList.get(pPosition).getPath()).fitCenter().into(imageView);
        pContainer.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup pContainer, int pPosition, Object pObject) {
        pContainer.removeView((View) pObject);
    }

    @Override
    public int getCount() {
        return mMediaList.size();
    }

    @Override
    public boolean isViewFromObject(View pView, Object pObject) {
        return pView == pObject;
    }

}
