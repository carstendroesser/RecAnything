package com.carstendroesser.audiorecorder.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.adapters.IconsListAdapter;
import com.carstendroesser.audiorecorder.models.Category;
import com.carstendroesser.audiorecorder.utils.DatabaseHelper;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by carstendrosser on 20.11.15.
 */
public class CategoryDialog extends AlertDialog implements IconsListAdapter.OnIconClickListener {

    @Bind(R.id.titleTextView)
    protected TextView mTitleTextView;
    @Bind(R.id.recyclerView)
    protected RecyclerView mRecyclerView;
    @Bind(R.id.titleEditText)
    protected EditText mTitleEditText;
    @Bind(R.id.descriptionEditText)
    protected EditText mDescriptionEditText;
    @Bind(R.id.buttonCancel)
    protected Button mButtonCancel;
    @Bind(R.id.buttonCreate)
    protected Button mButtonCreate;

    private IconsListAdapter mIconsListAdapter;
    private Category mCategory;

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence pCharSequence, int pStart, int pCount, int pAfter) {

        }

        @Override
        public void onTextChanged(CharSequence pCharSequence, int pStart, int pFefore, int pCount) {
            checkForValidInputs();
        }

        @Override
        public void afterTextChanged(Editable pEditable) {
        }
    };

    private boolean mIsInEditMode;

    /**
     * Main-constructor to setup everything. No matter if in editmode or not.
     *
     * @param pContext we need that
     */
    private CategoryDialog(Context pContext) {
        super(pContext);

        // set style
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_category, null);
        setView(content);

        ButterKnife.bind(this, content);

        mTitleEditText.addTextChangedListener(mTextWatcher);
        mDescriptionEditText.addTextChangedListener(mTextWatcher);

        mIconsListAdapter = new IconsListAdapter(getContext().getResources().obtainTypedArray(R.array.icons), this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setAdapter(mIconsListAdapter);

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                dismiss();
            }
        });
    }

    /**
     * Creates a categorydialog to create a new category.
     *
     * @param pContext                  we need that
     * @param pOnCategoryCreateListener notified when tried to create a category in the database
     */
    public CategoryDialog(Context pContext, final OnCategoryCreateListener pOnCategoryCreateListener) {
        this(pContext);

        mIsInEditMode = false;

        mButtonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                if (DatabaseHelper.getInstance(getContext()).insertCategory(mTitleEditText.getText().toString(), mDescriptionEditText.getText().toString(), mIconsListAdapter.getSelectedIconPosition())) {
                    pOnCategoryCreateListener.onCategoryCreate(true);
                } else {
                    pOnCategoryCreateListener.onCategoryCreate(false);
                }
                dismiss();
            }
        });

        checkForValidInputs();
    }

    /**
     * Creates a dialog to eidt an existing category.
     *
     * @param pContext  we need that
     * @param pCategory the category to edit
     * @param pListener notified if the category was edited
     */
    public CategoryDialog(Context pContext, final Category pCategory, final OnCategoryEditListener pListener) {
        this(pContext);

        mCategory = pCategory;
        mIsInEditMode = true;

        mTitleTextView.setText(R.string.edit_category);

        mButtonCreate.setText(R.string.apply);

        mIconsListAdapter.setSelectedIcon(pCategory.getIcon());
        mRecyclerView.scrollToPosition(mIconsListAdapter.getSelectedIconPosition());
        mTitleEditText.setText(pCategory.getName());
        mDescriptionEditText.setText(pCategory.getDescription());

        mButtonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                // clone the current category to not make changes if changing didnt work
                Category clonedCategory = pCategory.clone();
                clonedCategory.setName(mTitleEditText.getText().toString());
                clonedCategory.setDescription(mDescriptionEditText.getText().toString());
                clonedCategory.setIcon(mIconsListAdapter.getSelectedIconPosition());

                // try to update the category
                if (DatabaseHelper.getInstance(getContext()).updateCategory(clonedCategory)) {
                    // success
                    pListener.onCategoryEdited(true);
                } else {
                    // failed
                    pListener.onCategoryEdited(false);
                }

                dismiss();
            }
        });

        checkForValidInputs();
    }

    @Override
    public void onIconClick(int pId) {
        checkForValidInputs();
    }

    /**
     * Checks if the applybutton shall get enabled or not.
     */
    private void checkForValidInputs() {
        if (mIsInEditMode) {
            if (mIconsListAdapter.getSelectedIconPosition() == mCategory.getIcon()
                    && mTitleEditText.getText().toString().equals(mCategory.getName())
                    && mDescriptionEditText.getText().toString().equals(mCategory.getDescription())) {
                mButtonCreate.setEnabled(false);
                mButtonCreate.setTextColor(getContext().getResources().getColor(R.color.gray));
            } else {
                mButtonCreate.setEnabled(true);
                mButtonCreate.setTextColor(getContext().getResources().getColor(R.color.black));
            }
        } else {
            if (mTitleEditText.getText().length() == 0
                    || mDescriptionEditText.getText().length() == 0
                    || mIconsListAdapter.getSelectedIconPosition() == -1) {
                mButtonCreate.setEnabled(false);
                mButtonCreate.setTextColor(getContext().getResources().getColor(R.color.gray));
            } else {
                mButtonCreate.setEnabled(true);
                mButtonCreate.setTextColor(getContext().getResources().getColor(R.color.black));
            }
        }
    }

    /**
     * Listener, used to notify when a category was about to be created.
     */
    public interface OnCategoryCreateListener {
        /**
         * Called when a category was about to be created.
         *
         * @param pSuccess true, if created. Otherwise false
         */
        void onCategoryCreate(boolean pSuccess);
    }

    /**
     * Listener, used to notify when a category was edited.
     */
    public interface OnCategoryEditListener {
        /**
         * Called when a category was edited.
         *
         * @param pSuccess true, if edit succeeded. Otherwise false
         */
        void onCategoryEdited(boolean pSuccess);
    }

}
