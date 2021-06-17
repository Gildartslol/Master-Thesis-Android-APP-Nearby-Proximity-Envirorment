package com.example.jorge.androidapp.ui.messages.bottomSheetDialog.dialog_elements;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;

import com.example.jorge.androidapp.framework.Utils;

public class ImageBottomDialog extends android.support.v7.widget.AppCompatImageView {

    private int HEIGHT = 40;
    private int WIDTH = 40;
    private int MARGIN_TOP = 20;
    private float BIAS = 0.1F;

    public ImageBottomDialog(Context context) {
        super(context);
    }


    public void fillImage(int id) {
        this.setId(id);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(Utils.getSP(getContext(), HEIGHT), Utils.getSP(getContext(), WIDTH));
        this.setLayoutParams(params);
    }

    public void setDrawable (int resource){
        Drawable d = getResources().getDrawable(resource);
        this.setBackground(d);
    }


    public ConstraintSet fillConstraints(ConstraintSet constraintSet, int topIcon) {
        constraintSet.connect(this.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
        constraintSet.connect(this.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
        constraintSet.connect(this.getId(), ConstraintSet.TOP, topIcon, ConstraintSet.BOTTOM, 0);
        constraintSet.constrainHeight(this.getId(), Utils.getSP(getContext(), HEIGHT));
        constraintSet.constrainWidth(this.getId(), Utils.getSP(getContext(), WIDTH));
        constraintSet.setMargin(this.getId(), ConstraintSet.TOP, Utils.getSP(getContext(), MARGIN_TOP));
        constraintSet.setHorizontalBias(this.getId(), BIAS);
        return constraintSet;
    }

    /**
     <ImageView
     android:id="@+id/image2"
     android:layout_width="40dp"
     android:layout_height="40dp"
     android:background="@drawable/common_google_signin_btn_icon_dark"
     android:layout_marginTop="10dp"
     app:layout_constraintHorizontal_bias="0.05"
     app:layout_constraintLeft_toLeftOf="parent"
     app:layout_constraintRight_toRightOf="parent"
     app:layout_constraintTop_toBottomOf="@id/bottom_shee_bar" />
     **/

}
