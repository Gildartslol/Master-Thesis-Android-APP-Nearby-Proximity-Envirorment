package com.example.jorge.androidapp.ui.messages.bottomSheetDialog.dialog_elements;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;

import com.example.jorge.androidapp.R;
import com.example.jorge.androidapp.framework.Utils;

public class ButtonBottomDialog extends android.support.v7.widget.AppCompatButton {


    private int HEIGHT = 60;
    private int MARGIN_TOP = 0;
    private int TEXT_SIZE = 5;
    private int PADDING = 20;



    public ButtonBottomDialog(Context context) {
        super(context);
    }

    public void fillButton(int id, int idDrawable){
        this.setId(id);

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                Utils.getSP(getContext(),HEIGHT)
        );
        params.setMargins(0,Utils.getSP(getContext(),MARGIN_TOP),0,0);
        Drawable leftDrawable = getResources().getDrawable(idDrawable);
        this.setCompoundDrawablesWithIntrinsicBounds(leftDrawable,null,null,null);
        this.setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
        this.setLayoutParams(params);
        this.setPadding(Utils.getSP(getContext(),PADDING),0,0,0);
        this.setTextSize(Utils.getSP(getContext(),TEXT_SIZE));
        this.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_selection_friendlist));
        this.setCompoundDrawablePadding(Utils.getSP(getContext(),PADDING));
    }

    public ConstraintSet fillConstraints(ConstraintSet constraintSet, int topItem) {


        constraintSet.connect(this.getId(), ConstraintSet.TOP, topItem, ConstraintSet.BOTTOM, 0);
        constraintSet.constrainHeight(this.getId(), Utils.getSP(getContext(),HEIGHT));
        constraintSet.constrainWidth(this.getId(),ConstraintLayout.LayoutParams.MATCH_PARENT );
        constraintSet.setMargin(this.getId(),ConstraintSet.TOP,Utils.getSP(getContext(),MARGIN_TOP));
        return constraintSet;

    }


/*
    <Button
        android:id="@+id/button3"
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:text="button 1"
        android:drawableStart = "@drawable/ic_block_gray_24dp"
        android:drawablePadding="10dp"
        android:padding="10dp"
        android:layout_marginTop="10dp"
        android:textAlignment="viewStart"
        android:background="@drawable/button_selection_friendlist"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/bottom_sheet_bar_friend_list"
        />

*/

}
