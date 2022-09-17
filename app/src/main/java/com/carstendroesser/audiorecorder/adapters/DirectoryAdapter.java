package com.carstendroesser.audiorecorder.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.carstendroesser.audiorecorder.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by carstendrosser on 05.12.15.
 */
public class DirectoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static int VIEWTYPE_NAVIGATION_UP = 0;
    private static int VIEWTYPE_FOLDER = 1;

    private ArrayList<File> mDirectories;
    private OnDirectoryClickListener mClickListener;

    public DirectoryAdapter(OnDirectoryClickListener pListener) {
        mDirectories = new ArrayList<>();
        mClickListener = pListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup pParent, int pViewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(pParent.getContext());
        View view;

        if (pViewType == VIEWTYPE_NAVIGATION_UP) {
            view = layoutInflater.inflate(R.layout.listitem_navigationup, pParent, false);
            return new NavigationViewHolder(view);
        } else {
            view = layoutInflater.inflate(R.layout.listitem_directory, pParent, false);
            return new DirectoryViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder pHolder, final int pPosition) {
        if (getItemViewType(pPosition) == VIEWTYPE_NAVIGATION_UP) {
            pHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View pView) {
                    mClickListener.onNavigationUpClicked();
                }
            });

        } else if (getItemViewType(pPosition) == VIEWTYPE_FOLDER) {
            ((DirectoryViewHolder) pHolder).setFile(mDirectories.get(pPosition));
            pHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View pView) {
                    mClickListener.onDirectoryClicked(mDirectories.get(pPosition));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDirectories.size();
    }

    @Override
    public int getItemViewType(int pPosition) {
        if (pPosition == 0 && mDirectories.get(pPosition) == null) {
            return VIEWTYPE_NAVIGATION_UP;
        } else {
            return VIEWTYPE_FOLDER;
        }
    }

    public void updateList(ArrayList<File> pDirectories) {
        mDirectories.clear();
        mDirectories.addAll(pDirectories);
        notifyDataSetChanged();
    }

    public interface OnDirectoryClickListener {
        void onNavigationUpClicked();

        void onDirectoryClicked(File pDirectory);
    }

    public static class NavigationViewHolder extends RecyclerView.ViewHolder {

        public NavigationViewHolder(View pView) {
            super(pView);
        }
    }

    public static class DirectoryViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;

        public DirectoryViewHolder(View pView) {
            super(pView);
            nameTextView = (TextView) pView.findViewById(R.id.nameTextView);
        }

        public void setFile(File pFile) {
            nameTextView.setText(pFile.getName());
        }
    }

}
