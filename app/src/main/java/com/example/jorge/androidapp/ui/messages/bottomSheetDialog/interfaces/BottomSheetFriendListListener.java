package com.example.jorge.androidapp.ui.messages.bottomSheetDialog.interfaces;

public interface BottomSheetFriendListListener {

    void onDeleteUser(String idUser, int position);
    void onBlockUser(String idUser, int position);
    void onUnBlockUser(String idUser, int position);
}
