package com.example.jorge.androidapp.ui.messages.bottomSheetDialog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.jorge.androidapp.ui.messages.bottomSheetDialog.dialog_elements.ButtonBottomDialog;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AbstractBottomSheetDialog extends BottomSheetDialogFragment {


    private ArrayList<ButtonBottomDialog> buttons;

    private String THEME_DIALOG = "THEME_DIALOG";

    private String LAYOUT_DIALOG = "LAYOUT_DIALOG";
    private String TITLE_BUTTON_DIALOG = "TITLE_BUTTON_DIALOG";
    private String TITLE = "TITLE";
    private String TITLE_IMAGE_DIALOG = "TITLE_IMAGE_DIALOG";
    private String TITLE_PROFILE_IMAGE = "TITLE_PROFILE_IMAGE";
    private String VIEW_TO_CONSTRAINT_DIALOG = "VIEW_TO_CONSTRAINT_DIALOG";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int themeDialog = getCustomTheme();
        if (themeDialog != 0)
            setStyle(STYLE_NORMAL, themeDialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ConstraintLayout layout = (ConstraintLayout) inflater.inflate(getCustomLayout(), container, false);

        /*Establecemos el titulo del bootom sheet*/
        String tittleString = getArguments().getString(TITLE);
        Button tittleButton = layout.findViewById(getCustomTitleButtom());
        tittleButton.setText(tittleString);

        /*Establecemos la imagen del bottom Sheet*/
        CircleImageView tittleImage = layout.findViewById(getCustomTitleImage());
        byte[] profile_bytes = getBytesProfile();
        if (profile_bytes != null && profile_bytes.length > 0) {
            Bitmap bmp = BitmapFactory.decodeByteArray(profile_bytes, 0, profile_bytes.length);
            tittleImage.setImageBitmap(bmp);
        }


        /*Creamos el resto de elementos*/
        int lastViewToConstraint = getCustomViewToConstraint();

        /*Se clona*/
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(getContext(), getCustomLayout());

        for (ButtonBottomDialog but : buttons) {

            constraintSet = but.fillConstraints(constraintSet, lastViewToConstraint);
            lastViewToConstraint = but.getId();
            layout.addView(but);

        }

        constraintSet.applyTo(layout);
        return layout;
    }


    public void setThemeDialog(int themeDialog) {
        this.getArguments().putInt(THEME_DIALOG, themeDialog);
    }

    public void setTittleLayout(int layoutDialog, int tittleButton, int titleImage, int viewToCOntraint) {
        this.getArguments().putInt(LAYOUT_DIALOG, layoutDialog);
        this.getArguments().putInt(TITLE_BUTTON_DIALOG, tittleButton);
        this.getArguments().putInt(TITLE_IMAGE_DIALOG, titleImage);
        this.getArguments().putInt(VIEW_TO_CONSTRAINT_DIALOG, viewToCOntraint);
    }

    private int getCustomTheme() {
        return this.getArguments().getInt(THEME_DIALOG);
    }

    private int getCustomLayout() {
        return this.getArguments().getInt(LAYOUT_DIALOG);
    }

    private int getCustomTitleButtom() {
        return this.getArguments().getInt(TITLE_BUTTON_DIALOG);
    }

    private int getCustomTitleImage() {
        return this.getArguments().getInt(TITLE_IMAGE_DIALOG);
    }

    private int getCustomViewToConstraint() {
        return this.getArguments().getInt(VIEW_TO_CONSTRAINT_DIALOG);
    }

    protected void setBytesProfile(byte[] bytes) {
        this.getArguments().putByteArray(TITLE_PROFILE_IMAGE, bytes);
    }

    protected byte[] getBytesProfile() {
        return this.getArguments().getByteArray(TITLE_PROFILE_IMAGE);
    }

    protected void setTittle(String title) {
        this.getArguments().putString(TITLE, title);
    }

    protected void addButton(ButtonBottomDialog but) {
        if (buttons == null)
            buttons = new ArrayList<>();
        this.buttons.add(but);
    }


}
