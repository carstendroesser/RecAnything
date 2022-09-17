package com.carstendroesser.audiorecorder.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.models.Record;
import com.carstendroesser.audiorecorder.utils.DatabaseHelper;
import com.carstendroesser.audiorecorder.utils.FormatUtils;
import com.carstendroesser.audiorecorder.utils.IconMapper;
import com.carstendroesser.audiorecorder.views.SquaredImageView;

import java.io.IOException;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by carstendrosser on 21.10.15.
 */
public class RecordsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Record> mRecords;
    private OnListItemClickListener mOnListItemClickListener;
    private OnFilterResetButtonClickListener mOnFilterResetButtonClickListener;
    private Context mContext;

    private static final int VIEWTYPE_RECORDING = 0;
    private static final int VIEWTYPE_INFO_FOOTER = 1;
    private static final int VIEWTYPE_INFO_HEADER = 2;

    /**
     * Constructor.
     *
     * @param pContext we need that
     * @param pRecords a list with records
     */
    public RecordsAdapter(Context pContext, List<Record> pRecords) {
        mContext = pContext;
        mRecords = pRecords;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup pParent, int pViewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(pParent.getContext());

        if (pViewType == VIEWTYPE_RECORDING) {
            View view = layoutInflater.inflate(R.layout.listitem_record, pParent, false);
            return new RecordViewHolder(view);
        } else if (pViewType == VIEWTYPE_INFO_FOOTER) {
            View view = layoutInflater.inflate(R.layout.listitem_footer_info, pParent, false);
            return new InfoViewHolder(view);
        } else {
            View view = layoutInflater.inflate(R.layout.listitem_header_info, pParent, false);
            return new HeaderViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder pHolder, int pPosition) {
        if (getItemViewType(pPosition) == VIEWTYPE_RECORDING) {
            if (hasFilterHeader()) {
                pPosition--;
            }
            final Record record = mRecords.get(pPosition);

            // update the ViewHolder with content
            ((RecordViewHolder) pHolder).setRecord(record, mOnListItemClickListener);
        } else if (getItemViewType(pPosition) == VIEWTYPE_INFO_FOOTER) {
            long sumFileSize = 0;
            for (Record record : mRecords) {
                sumFileSize += record.getFileSize();
            }

            String info = "" + mRecords.size()
                    + " " + pHolder.itemView.getContext().getResources().getString(R.string.infoview_recordings)
                    + " (" + FormatUtils.toReadableSize(sumFileSize) + ")";
            ((InfoViewHolder) pHolder).textview.setText(info);
        } else {

            // setup the infotext
            int filteredCategoriesCount = DatabaseHelper.getInstance(mContext).getUnselectedCatgoriesList().size();
            String filterText = "" + filteredCategoriesCount + " ";
            if (filteredCategoriesCount == 1) {
                filterText += mContext.getResources().getString(R.string.category_filtered) + " ";
            } else {
                filterText += mContext.getResources().getString(R.string.categories_filtered) + " ";
            }

            // add the filtered categorynames to the infotext
            for (int i = 0; i < filteredCategoriesCount; i++) {
                String categoryName = DatabaseHelper.getInstance(mContext).getUnselectedCatgoriesList().get(i).getName();
                filterText += categoryName;
                if (i < filteredCategoriesCount - 1) {
                    filterText += ", ";
                }
            }

            // passtrough the clickevent of the resetbutton
            ((HeaderViewHolder) pHolder).resetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View pView) {
                    if (mOnFilterResetButtonClickListener != null) {
                        mOnFilterResetButtonClickListener.onFilterResetButtonClick();
                    }
                }
            });

            ((HeaderViewHolder) pHolder).textview.setText(filterText);
        }
    }

    @Override
    public int getItemCount() {
        if (mRecords.isEmpty()) {
            return hasFilterHeader() ? 1 : 0;
        } else {
            int size = mRecords.size() + 1;
            if (hasFilterHeader()) {
                size++;
            }
            return size;
        }
    }

    /**
     * Checks if the infoheader should be applied.
     *
     * @return true if the infoheader shall be shown, false otherwise
     */
    private boolean hasFilterHeader() {
        return DatabaseHelper.getInstance(mContext).getUnselectedCatgoriesList().size() != 0;
    }

    @Override
    public int getItemViewType(int pPosition) {
        if (pPosition == 0 && hasFilterHeader()) {
            return VIEWTYPE_INFO_HEADER;
        } else if (pPosition == getItemCount() - 1) {
            return VIEWTYPE_INFO_FOOTER;
        } else {
            return VIEWTYPE_RECORDING;
        }
    }

    /**
     * Will set new data to show.
     *
     * @param pRecords the list of records you want to show
     */
    public void updateList(List<Record> pRecords) {
        mRecords = pRecords;
        notifyDataSetChanged();
    }

    /**
     * Sets a listener to use to notify when a listitem is clicked.
     *
     * @param pListener
     */
    public void setOnItemClickListener(OnListItemClickListener pListener) {
        mOnListItemClickListener = pListener;
    }

    /**
     * Sets a listener to use to notify when the filterresetbutton was clicked.
     *
     * @param pListener the listener to notify
     */
    public void setOnFilterResetButtonClickListener(OnFilterResetButtonClickListener pListener) {
        mOnFilterResetButtonClickListener = pListener;
    }

    /**
     * Listener used to receive Filterresetbutton clicks.
     */
    public interface OnFilterResetButtonClickListener {
        /**
         * The filter-reset button in the headeritem was clicked.
         */
        void onFilterResetButtonClick();
    }

    /**
     * Interface to receive callbacks when a listitem is clicked.
     */
    public interface OnListItemClickListener {
        /**
         * Called when a listitem is clicked
         *
         * @param pView   the clicked view
         * @param pRecord the record which is shown in this view
         */
        void onListItemClick(View pView, Record pRecord);

        /**
         * Called when a listitem is longclicked
         *
         * @param pView   the clicked view
         * @param pRecord the record which is shown in this view
         */
        void onListItemLongClick(View pView, Record pRecord);

        /**
         * Called when a listitem's menuview was clicked
         *
         * @param pView   the menuview with the dots
         * @param pRecord the clicked record
         */
        void onListItemMenuClick(View pView, Record pRecord);

        /**
         * Called when a listitem's mediaitem was clicked
         *
         * @param pView      the clicked view
         * @param pRecord    the record of this listitem
         * @param pMediaItem the clicked mediaitem
         */
        void onListItemMediaItemClick(View pView, Record pRecord, int pMediaItem);

        /**
         * Called when a listitem's note was clicked
         *
         * @param pView   the clicked view
         * @param pRecord the record of this listitem
         */
        void onListItemNoteClick(View pView, Record pRecord);
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView textview;
        Button resetButton;

        public HeaderViewHolder(View pView) {
            super(pView);
            textview = (TextView) pView.findViewById(R.id.textview);
            resetButton = (Button) pView.findViewById(R.id.resetButton);
        }

    }

    public static class InfoViewHolder extends RecyclerView.ViewHolder {

        TextView textview;

        public InfoViewHolder(View pView) {
            super(pView);
            textview = (TextView) pView.findViewById(R.id.textview);
        }

    }

    public static class RecordViewHolder extends RecyclerView.ViewHolder {

        LinearLayout root;
        LinearLayout mediaContainerLayout;
        TextView nameTextView;
        TextView lengthTextView;
        TextView categoryTextView;
        TextView dateTextView;
        TextView noteTextView;
        ImageView iconImageView;
        ImageView menuImageView;

        public RecordViewHolder(View pView) {
            super(pView);

            root = (LinearLayout) pView.findViewById(R.id.listitemRoot);
            mediaContainerLayout = (LinearLayout) pView.findViewById(R.id.listitemMediaContainer);
            noteTextView = (TextView) pView.findViewById(R.id.listitemNoteTextView);
            nameTextView = (TextView) pView.findViewById(R.id.listitemNameTextView);
            lengthTextView = (TextView) pView.findViewById(R.id.listitemLengthTextView);
            categoryTextView = (TextView) pView.findViewById(R.id.listitemCategoryTextView);
            dateTextView = (TextView) pView.findViewById(R.id.listitemDateTextView);
            iconImageView = (ImageView) pView.findViewById(R.id.listitemIconImageView);
            menuImageView = (ImageView) pView.findViewById(R.id.listitemMenuImageView);
        }

        /**
         * Sets the content to the specific views using the data given by the record.
         *
         * @param pRecord                  the record to use the data from
         * @param pOnListItemClickListener listener to receive clickevents
         */
        public void setRecord(final Record pRecord, final OnListItemClickListener pOnListItemClickListener) {

            final Handler handler = new Handler();

            // get the duration for this recording
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final MediaPlayer player = new MediaPlayer();
                        player.setDataSource(pRecord.getPath());
                        player.prepare();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                lengthTextView.setText(FormatUtils.toReadableDuration(player.getDuration()));
                                player.release();
                            }
                        });
                    } catch (final IOException pException) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                lengthTextView.setText("-");
                            }
                        });
                    }
                }
            }).start();

            // set content
            nameTextView.setText(pRecord.getName());
            categoryTextView.setText(pRecord.getCategory().getName());
            dateTextView.setText(FormatUtils.toReadableDate(pRecord.getLastModified()));

            // get the icon of the category
            Drawable icon = IconMapper.getIconById(itemView.getContext(), pRecord.getCategory().getIcon());
            iconImageView.setImageDrawable(icon);

            //set onclick-listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View pView) {
                    if (pOnListItemClickListener != null) {
                        pOnListItemClickListener.onListItemClick(pView, pRecord);
                    }
                }
            });

            //set on longclick listener
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View pView) {
                    if (pOnListItemClickListener != null) {
                        pOnListItemClickListener.onListItemLongClick(pView, pRecord);
                    }
                    return true;
                }
            });

            menuImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View pView) {
                    if (pOnListItemClickListener != null) {
                        pOnListItemClickListener.onListItemMenuClick(pView, pRecord);
                    }
                }
            });

            // show/hide the note subview
            if (pRecord.getNote() == null) {
                noteTextView.setVisibility(View.GONE);
            } else {
                // setup the noteview
                noteTextView.setVisibility(View.VISIBLE);
                noteTextView.setText(pRecord.getNote().getText());
                noteTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View pView) {
                        if (pOnListItemClickListener != null) {
                            pOnListItemClickListener.onListItemNoteClick(pView, pRecord);
                        }
                    }
                });
            }

            // setup the mediaitems
            if (pRecord.getMediaList().size() == 0) {
                mediaContainerLayout.setVisibility(GONE);
            } else {
                mediaContainerLayout.setVisibility(VISIBLE);
                mediaContainerLayout.removeAllViews();

                // add several mediaitem views
                for (int i = 0; i < pRecord.getMediaList().size(); i++) {
                    final int finali = i;
                    if (i < 3) {
                        final FrameLayout mediaView = (FrameLayout) LayoutInflater.from(itemView.getContext()).inflate(R.layout.listitem_media, null);
                        final SquaredImageView imageView = (SquaredImageView) mediaView.findViewById(R.id.listitemMediaImageView);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View pView) {
                                if (pOnListItemClickListener != null) {
                                    pOnListItemClickListener.onListItemMediaItemClick(imageView, pRecord, finali);
                                }
                            }
                        });
                        mediaContainerLayout.addView(mediaView);
                        Glide.with(itemView.getContext()).load(pRecord.getMediaList().get(i).getPath()).fitCenter().into(imageView);
                    } else {
                        // add the "show all" button
                        LinearLayout mediaMoreView = (LinearLayout) LayoutInflater.from(itemView.getContext()).inflate(R.layout.listitem_media_more, null);
                        mediaMoreView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View pView) {
                                if (pOnListItemClickListener != null) {
                                    pOnListItemClickListener.onListItemMediaItemClick(pView, pRecord, finali);
                                }
                            }
                        });
                        TextView countTextView = (TextView) mediaMoreView.findViewById(R.id.listitemMediaMoreTextView);
                        countTextView.setText("+" + (pRecord.getMediaList().size() - 3));
                        mediaContainerLayout.addView(mediaMoreView);
                        break;
                    }
                }

            }
        }

    }

}
