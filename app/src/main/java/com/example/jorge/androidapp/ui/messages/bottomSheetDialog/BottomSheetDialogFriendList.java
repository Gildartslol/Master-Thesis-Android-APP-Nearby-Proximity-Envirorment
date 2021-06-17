package com.example.jorge.androidapp.ui.messages.bottomSheetDialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.example.jorge.androidapp.R;
import com.example.jorge.androidapp.ui.messages.bottomSheetDialog.dialog_elements.ButtonBottomDialog;
import com.example.jorge.androidapp.ui.messages.bottomSheetDialog.interfaces.BottomSheetFriendListListener;
import com.example.jorge.androidapp.adapter.FriendListAdapter;
import com.example.jorge.androidapp.entities.User;


public class BottomSheetDialogFriendList extends AbstractBottomSheetDialog {

    private BottomSheetFriendListListener mListener;
    private String USER = "USER";
    private int position = 0;
    private int type = 0;


    public BottomSheetDialogFriendList() {
        this.setArguments(new Bundle());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setButtons();
        setThemeDialog(R.style.MaterialDialogSheet);
        setTittleLayout(R.layout.bottom_sheet_layout_friend_list, R.id.bottom_sheet_button_friend_list, R.id.bottom_sheet_icon_friend_list, R.id.bottom_sheet_bar_friend_list);
        super.onCreate(savedInstanceState);
    }

    private void setButtons() {
        if (type == FriendListAdapter.TYPE_FRIEND) {
            ButtonBottomDialog but_delete = new ButtonBottomDialog(getContext());
            but_delete.setText(R.string.bottom_sheet_buttom_delete);
            but_delete.fillButton(View.generateViewId(), R.drawable.ic_delete_gray_24dp);
            but_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onDeleteUser(getArguments().getString(USER), position);
                    dismiss();
                }
            });

            this.addButton(but_delete);

            ButtonBottomDialog but_block = new ButtonBottomDialog(getContext());
            but_block.setText(R.string.bottom_sheet_buttom_block);
            but_block.fillButton(View.generateViewId(), R.drawable.ic_block_gray_24dp);
            but_block.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onBlockUser(getArguments().getString(USER), position);
                    dismiss();
                }
            });

            this.addButton(but_block);

        } else if (type == FriendListAdapter.TYPE_BLOCK) {
            ButtonBottomDialog but_delete = new ButtonBottomDialog(getContext());
            but_delete.setText(R.string.bottom_sheet_buttom_unBlock);
            but_delete.fillButton(View.generateViewId(), R.drawable.ic_undo_black_24dp);
            but_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onUnBlockUser(getArguments().getString(USER), position);
                    dismiss();
                }
            });

            this.addButton(but_delete);
        }

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (BottomSheetFriendListListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement BottomSheetListener");
        }
    }

    public void setUser(User user) {
        getArguments().putString(USER, user.getUserID());
        setTittle(user.getUserName());
        setBytesProfile(user.getUserImage());
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setType(int type) {
        this.type = type;
    }
}