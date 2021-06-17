package com.example.jorge.androidapp.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.example.jorge.androidapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomDialog extends Dialog {


    @BindView(R.id.cus_dialog_top_side)
    protected ImageView topImage;

    @BindView(R.id.cus_dialog_text_dialog)
    protected TextView progressText;

    @BindView(R.id.cus_dialog_progress_bar)
    protected ProgressBar progressBar;

    @BindView(R.id.cus_dialog_number_progress)
    protected NumberProgressBar numberedProgresBar;

    @BindView(R.id.custom_dialog_btn_positive)
    protected Button btn_positive;

    @BindView(R.id.custom_dialog_btn_negative)
    protected Button btn_negative;

    private boolean isDefaultListenerPositive = false;
    private boolean isDefaultListenerNegative = false;


    public CustomDialog(Context context) {
        super(context);
        this.setContentView(R.layout.dialog_progress_custom);
        ButterKnife.bind(this);
    }

    protected void setButtonListenerPositive(CustomOnClickListener listener) {
        isDefaultListenerPositive = false;
        listener.setDialog(this);
        this.btn_positive.setOnClickListener(listener);
    }

    protected void setButtonListenerNegative(CustomOnClickListener listener) {
        isDefaultListenerNegative = false;
        listener.setDialog(this);
        this.btn_negative.setOnClickListener(listener);
    }

    protected void setIcon(int resID) {
        topImage.setImageResource(resID);
    }

    protected void setImageColor(int colorId) {
        topImage.setBackgroundResource(colorId);
        btn_negative.setBackgroundResource(colorId);
        btn_positive.setBackgroundResource(colorId);
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(), colorId), PorterDuff.Mode.SRC_IN);
    }

    protected void setVisibleBtnPositive(boolean isVisible) {
        btn_positive.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    protected void setVisibleBtnNegative(boolean isVisible) {
        btn_negative.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    protected void setTextPositiveBtn(String text) {
        btn_positive.setText(text);
    }

    protected void setTextNegativeBtn(String text) {
        btn_negative.setText(text);
    }

    protected void setProgressBarActive() {
        this.numberedProgresBar.setVisibility(View.INVISIBLE);
        this.progressBar.setIndeterminate(true);
        this.progressBar.setVisibility(View.VISIBLE);
    }

    protected void setNumberedProgresBarActive(int progress) {
        this.progressBar.setVisibility(View.INVISIBLE);
        this.numberedProgresBar.setVisibility(View.VISIBLE);
        this.numberedProgresBar.setProgress(progress);
    }

    protected void setNoProgressBars() {
        this.progressBar.setVisibility(View.INVISIBLE);
        this.numberedProgresBar.setVisibility(View.INVISIBLE);
    }


    protected void setProgressText(String text) {
        if (progressText != null)
            progressText.setText(text);
    }

    @Override
    public void show() {

        CustomOnClickListener defaultListener = new CustomOnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        };

        if (isDefaultListenerNegative)
            setButtonListenerNegative(defaultListener);
        if (isDefaultListenerPositive)
            setButtonListenerPositive(defaultListener);
        super.show();
    }

    public static class Builder {
        private final CustomParams P;

        public Builder(@NonNull Context context) {
            this.P = new CustomDialog.CustomParams(context);
        }

        @NonNull
        public Context getContext() {
            return this.P.mContext;
        }

        public CustomDialog.Builder setCustomPositiveListener(CustomOnClickListener listener) {
            this.P.mListenerPositive = listener;
            return this;
        }

        public CustomDialog.Builder setCustomNegativeListener(CustomOnClickListener listener) {
            this.P.mListenerNegative = listener;
            return this;
        }

        public CustomDialog.Builder setCancelable(boolean isCancelable) {
            this.P.mCancelable = isCancelable;
            return this;
        }

        public CustomDialog.Builder setIcon(int resId) {
            this.P.iconID = resId;
            return this;
        }

        public CustomDialog.Builder isVisibleBtnPositive(boolean disable) {
            this.P.isVisibleBtnPositive = disable;
            return this;
        }

        public CustomDialog.Builder isVisibleBtnNegative(boolean disable) {
            this.P.isVisibleBtnNegative = disable;
            return this;
        }

        public CustomDialog.Builder setTextPositiveBtn(String text) {
            this.P.btnPositiveText = text;
            return this;
        }

        public CustomDialog.Builder setTextNegativeBtn(String text) {
            this.P.btnNegativeText = text;
            return this;
        }


        public CustomDialog.Builder setBackgroundColor(int resId) {
            this.P.colorID = resId;
            return this;
        }

        public CustomDialog.Builder setText(String text) {
            this.P.mText = text;
            return this;
        }

        public CustomDialog.Builder setProgressBarActive() {
            this.P.isNumberedProgressBarActive = false;
            this.P.isProgressBarActive = true;
            return this;
        }


        public CustomDialog.Builder setNumberedProgressBarActive(int progress) {
            this.P.isProgressBarActive = false;
            this.P.isNumberedProgressBarActive = true;
            this.P.progress = progress;
            return this;
        }


        public CustomDialog create() {
            CustomDialog dialog = new CustomDialog(this.P.mContext);
            this.P.apply(dialog);
            dialog.setCancelable(this.P.mCancelable);
            if (this.P.mCancelable) {
                dialog.setCanceledOnTouchOutside(true);
            }

            return dialog;
        }

        public CustomDialog show() {
            CustomDialog dialog = this.create();
            dialog.show();
            return dialog;
        }
    }


    public static class CustomOnClickListener implements View.OnClickListener {

        public CustomDialog mDialog;

        protected void setDialog(CustomDialog dialog) {
            this.mDialog = dialog;
        }

        @Override
        public void onClick(View v) {
            mDialog.dismiss();
        }
    }

    public static class CustomParams {

        public Context mContext;
        public boolean mCancelable;
        public int iconID;
        public int colorID;
        public String mText;
        public boolean isProgressBarActive = false;
        public boolean isNumberedProgressBarActive = false;
        public int progress = 0;
        public boolean isVisibleBtnPositive = false;
        public boolean isVisibleBtnNegative = false;
        public String btnPositiveText = "";
        public String btnNegativeText = "";
        public CustomOnClickListener mListenerPositive = null;
        public CustomOnClickListener mListenerNegative = null;

        public CustomParams(Context context) {
            this.mContext = context;
            this.mCancelable = true;
        }

        public void apply(CustomDialog dialog) {

            dialog.setProgressText(mText);
            dialog.setVisibleBtnNegative(isVisibleBtnNegative);
            dialog.setVisibleBtnPositive(isVisibleBtnPositive);
            if (isProgressBarActive)
                dialog.setProgressBarActive();
            if (isNumberedProgressBarActive)
                dialog.setNumberedProgresBarActive(progress);
            if (iconID != 0)
                dialog.setIcon(iconID);
            if (colorID != 0)
                dialog.setImageColor(colorID);

            if (mListenerNegative != null)
                dialog.setButtonListenerNegative(mListenerNegative);
            else
                dialog.isDefaultListenerNegative = true;

            if (mListenerPositive != null)
                dialog.setButtonListenerPositive(mListenerPositive);
            else
                dialog.isDefaultListenerPositive = true;

            if (!btnPositiveText.equals(""))
                dialog.setTextPositiveBtn(btnPositiveText);

            if (!btnNegativeText.equals(""))
                dialog.setTextNegativeBtn(btnNegativeText);
        }
    }

}
