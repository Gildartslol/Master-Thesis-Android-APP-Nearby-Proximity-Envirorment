package com.example.jorge.androidapp.ui.messages.bottomSheetDialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.example.jorge.androidapp.R;
import com.example.jorge.androidapp.ui.messages.bottomSheetDialog.dialog_elements.ButtonBottomDialog;
import com.example.jorge.androidapp.ui.messages.bottomSheetDialog.interfaces.BottomSheetHomeListener;
import com.example.jorge.androidapp.entities.Device;
import com.example.jorge.androidapp.entities.User;


public class BottomSheetDialogHomeActivity extends AbstractBottomSheetDialog {

    private BottomSheetHomeListener mListener;
    private Device device = null;
    private int position = 0;
    private int type = 0;


    public BottomSheetDialogHomeActivity() {
        this.setArguments(new Bundle());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setButtons();
        setThemeDialog(R.style.MaterialDialogSheet);
        setTittleLayout(R.layout.bottom_sheet_layout_home_activity, R.id.bottom_sheet_button_home_activity, R.id.bottom_sheet_icon_home_activity, R.id.bottom_sheet_bar_home_activity);
        super.onCreate(savedInstanceState);
    }

    private void setButtons() {
        if (device != null) {
            if (!device.isFriend()) {
                ButtonBottomDialog buttonAddFriend = createButtonAddFriend();
                this.addButton(buttonAddFriend);
            }
            if (device.isFriend() && !device.isConnected()) {
                ButtonBottomDialog buttonConnect = createButtonConnectTo();
                this.addButton(buttonConnect);
            }
            if (device.isFriend() && device.isConnected()) {
                ButtonBottomDialog buttonConnect = createButtonStartChat();
                this.addButton(buttonConnect);

                ButtonBottomDialog buttonSendFile = createButtonSendFile();
                this.addButton(buttonSendFile);

                ButtonBottomDialog buttonDisconnect = createButtonDisconnect();
                this.addButton(buttonDisconnect);
            }
        }
    }

    private ButtonBottomDialog createButtonAddFriend() {
        ButtonBottomDialog but_addFriend = new ButtonBottomDialog(getContext());
        but_addFriend.setText(R.string.bottom_sheet_buttom_addFriend);
        but_addFriend.fillButton(View.generateViewId(), R.drawable.ic_person_add_black_24dp);
        but_addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDialogAddFriend(device, position);
                dismiss();
            }
        });

        return but_addFriend;
    }

    private ButtonBottomDialog createButtonConnectTo() {
        ButtonBottomDialog but_connect = new ButtonBottomDialog(getContext());
        but_connect.setText(R.string.bottom_sheet_buttom_connect_to);
        but_connect.fillButton(View.generateViewId(), R.drawable.ic_connect_black_24dp);
        but_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDialogConnect(device, position);
                dismiss();
            }
        });

        return but_connect;
    }

    private ButtonBottomDialog createButtonStartChat() {
        ButtonBottomDialog but_connect = new ButtonBottomDialog(getContext());
        but_connect.setText(R.string.bottom_sheet_buttom_chat);
        but_connect.fillButton(View.generateViewId(), R.drawable.ic_chat_black_24dp);
        but_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDialogOpenChat(device, position);
                dismiss();
            }
        });

        return but_connect;
    }

    private ButtonBottomDialog createButtonSendFile() {
        ButtonBottomDialog but_connect = new ButtonBottomDialog(getContext());
        but_connect.setText(R.string.bottom_sheet_buttom_send_file);
        but_connect.fillButton(View.generateViewId(), R.drawable.ic_file_upload_black_24dp);
        but_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDialogSendFile(device, position);
                dismiss();
            }
        });

        return but_connect;
    }

    private ButtonBottomDialog createButtonDisconnect() {
        ButtonBottomDialog but_connect = new ButtonBottomDialog(getContext());
        but_connect.setText(R.string.bottom_sheet_buttom_disconnect);
        but_connect.fillButton(View.generateViewId(), R.drawable.ic_disconnect_black_24dp);
        but_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDialogDisconect(device);
                dismiss();
            }
        });

        return but_connect;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (BottomSheetHomeListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement BottomSheetHomeListener");
        }
    }

    public void setDevice(Device device) {
        this.device = device;
        setTittle(device.getDeviceUsername());
        if (device.getUser() != null)
            setBytesProfile(device.getUser().getUserImage());
    }

    public void setUser(User user) {

    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setType(int type) {
        this.type = type;
    }
}