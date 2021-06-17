package com.example.jorge.androidapp.ui.messages.bottomSheetDialog.interfaces;

import com.example.jorge.androidapp.entities.Device;

public interface BottomSheetHomeListener {

    void onDialogAddFriend(Device user, int position);
    void onDialogConnect(Device device, int position);
    void onDialogDisconect(Device device);
    void onDialogSendFile(Device device, int position);
    void onDialogBlock(Device device, int position);
    void onDialogOpenChat(Device device, int position);

}
