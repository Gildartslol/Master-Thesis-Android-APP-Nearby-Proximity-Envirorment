package com.example.jorge.androidapp.ui.dialogs;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.example.jorge.androidapp.R;
import com.example.jorge.androidapp.entities.Endpoint;
import com.example.jorge.androidapp.framework.Utils;
import com.example.jorge.androidapp.framework.nearby.MyNearbyConnectionService;
import com.example.jorge.androidapp.network.petition.AbstractPeticionNearby;
import com.example.jorge.androidapp.network.petition.Chatpetition;
import com.example.jorge.androidapp.network.petition.FileExchangePetition;
import com.example.jorge.androidapp.network.petition.RequestFriendPetition;
import com.example.jorge.androidapp.ui.activities.chat.ChatActivity;
import com.example.jorge.androidapp.ui.activities.home.HomeActivity;

public class DialogManager {

    private CustomDialog activeDialog = null;
    private Context mContext;

    private int primaryColor;
    private int secondaryColor;
    private int warningColor;

    public DialogManager(Context context) {
        this.mContext = context;
        this.primaryColor = R.color.dark_olive_green;
        this.secondaryColor = R.color.indian_red;
        this.warningColor = R.color.dark_orange;
    }


    public CustomDialog getActiveDialog() {
        return activeDialog;
    }

    public void updateTextActive(String text) {
        if (activeDialog != null) {
            activeDialog.setProgressText(text);
        }
    }

    public void updateErrorDialog(String text) {
        if (activeDialog != null)
            activeDialog.dismiss();

        CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
        builder.setCancelable(false)
                .setBackgroundColor(secondaryColor)
                .setIcon(R.drawable.ic_error_white_24dp)
                .isVisibleBtnPositive(true)
                .setText(text);

        activeDialog = builder.show();
    }


    /********
     *
     *
     * CONNECTION
     *
     */

    public void createConnectionDialog() {
        if (activeDialog != null)
            activeDialog.dismiss();

        CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
        builder.setCancelable(false)
                .setProgressBarActive()
                .setBackgroundColor(primaryColor)
                .setIcon(R.drawable.ic_connect_white_24dp)
                .setText(mContext.getString(R.string.connection_start));

        activeDialog = builder.show();
    }

    public void createIncomingConnectionDialog(MyNearbyConnectionService service, Endpoint endpoint) {
        if (activeDialog != null)
            activeDialog.dismiss();

        CustomDialog.CustomOnClickListener listenerNegative = new CustomDialog.CustomOnClickListener() {
            @Override
            public void onClick(View v) {
                service.rejectConnection(endpoint);
                mDialog.dismiss();
            }
        };

        CustomDialog.CustomOnClickListener listenerPositive = new CustomDialog.CustomOnClickListener() {
            @Override
            public void onClick(View v) {
                service.acceptConnection(endpoint);
                mDialog.dismiss();
            }
        };

        CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
        builder.setCancelable(false)
                .setBackgroundColor(primaryColor)
                .setIcon(R.drawable.ic_connect_white_24dp)
                .setText(mContext.getString(R.string.connection_incoming, Utils.getUserNameFromEndpoint(endpoint)))
                .setCustomNegativeListener(listenerNegative)
                .isVisibleBtnNegative(true)
                .setCustomPositiveListener(listenerPositive)
                .isVisibleBtnPositive(true);

        activeDialog = builder.show();
    }


    public void updateErrorDialogConnecting(String username) {
        if (activeDialog != null) {
            activeDialog.setIcon(R.drawable.ic_error_white_24dp);
            activeDialog.setVisibleBtnPositive(true);
            activeDialog.setVisibleBtnNegative(false);
            activeDialog.setNoProgressBars();
            activeDialog.setImageColor(secondaryColor);
            activeDialog.setProgressText(mContext.getString(R.string.connection_out_of_range, username));
        }
    }

    public void updateFailedConnectingDialog() {
        if (activeDialog != null) {
            activeDialog.setIcon(R.drawable.ic_error_white_24dp);
            activeDialog.setVisibleBtnPositive(true);
            activeDialog.setVisibleBtnNegative(false);
            activeDialog.setNoProgressBars();
            activeDialog.setImageColor(secondaryColor);
            activeDialog.setProgressText(mContext.getString(R.string.dialog_error_connecting));
        }
    }

    public void updateOKConnectingDialog() {
        if (activeDialog != null) {

            CustomDialog.CustomOnClickListener defaultListener = new CustomDialog.CustomOnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                }
            };

            activeDialog.setNoProgressBars();
            activeDialog.setVisibleBtnPositive(true);
            activeDialog.setVisibleBtnNegative(false);
            activeDialog.setProgressText(mContext.getString(R.string.connection_connected));
            activeDialog.setButtonListenerPositive(defaultListener);
        }
    }

    /*****
     *
     *
     * FRIEND REQUEST
     *
     */

    public void createFriendRequest(MyNearbyConnectionService service, Endpoint endpoint) {
        if (activeDialog != null)
            activeDialog.dismiss();

        CustomDialog.CustomOnClickListener listenerNegative = new CustomDialog.CustomOnClickListener() {
            @Override
            public void onClick(View v) {
                service.rejectConnection(endpoint);
                service.deletePetition(endpoint.getId(), RequestFriendPetition.REQUEST_FRIEND_TYPE);
                mDialog.dismiss();
            }
        };

        CustomDialog.CustomOnClickListener listenerPositive = new CustomDialog.CustomOnClickListener() {
            @Override
            public void onClick(View v) {
                service.acceptConnection(endpoint);
                mDialog.setVisibleBtnNegative(false);
                mDialog.setVisibleBtnPositive(false);
                //mDialog.setProgressText("Waiting ...");
                mDialog.dismiss();
            }
        };

        CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
        builder.setCancelable(false)
                .setBackgroundColor(primaryColor)
                .isVisibleBtnPositive(true)
                .isVisibleBtnNegative(true)
                .setCustomPositiveListener(listenerPositive)
                .setCustomNegativeListener(listenerNegative)
                .setIcon(R.drawable.ic_person_white_24dp)
                .setText(mContext.getString(R.string.friend_request_income, Utils.getUserNameFromEndpoint(endpoint)));

        activeDialog = builder.show();
    }


    public void updateDialogFriendRequest(String text, boolean progressBar, boolean progressBarNumbered, int percent, boolean btnPositive, boolean btnNegative) {
        if (activeDialog != null) {
            activeDialog.setNoProgressBars();
            activeDialog.setIcon(R.drawable.ic_person_white_24dp);
            activeDialog.setImageColor(primaryColor);
            if (progressBar)
                activeDialog.setProgressBarActive();
            if (progressBarNumbered)
                activeDialog.setNumberedProgresBarActive(percent);

            activeDialog.setVisibleBtnPositive(btnPositive);
            activeDialog.setVisibleBtnNegative(btnNegative);
            activeDialog.setProgressText(text);
            activeDialog.show();
        } else {
            CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
            builder.setCancelable(false)
                    .setBackgroundColor(primaryColor)
                    .setIcon(R.drawable.ic_person_white_24dp)
                    .setText(text);
            if (progressBar)
                builder.setProgressBarActive();
            if (progressBarNumbered)
                builder.setNumberedProgressBarActive(percent);

            activeDialog = builder.show();
        }
    }


    public void createChatClientLeave(String text) {

        CustomDialog.CustomOnClickListener listenerPositive = new CustomDialog.CustomOnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, HomeActivity.class);
                mContext.startActivity(intent);
                mDialog.dismiss();
            }
        };


        CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
        builder.setCancelable(false)
                .setBackgroundColor(primaryColor)
                .setIcon(R.drawable.ic_exit_white_24dp)
                .setText(text)
                .isVisibleBtnPositive(true)
                .setCustomPositiveListener(listenerPositive);

        activeDialog = builder.show();
    }



    /*
     *
     * FILE EXCHANGE
     *
     * */

    public void createFileExchangeDialog() {
        if (activeDialog != null)
            activeDialog.dismiss();

        CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
        builder.setCancelable(false)
                .setBackgroundColor(primaryColor)
                .setIcon(R.drawable.ic_connect_white_24dp)
                .setText(mContext.getString(R.string.file_exchange_start));

        activeDialog = builder.show();
    }

    public void createWarningDialog(MyNearbyConnectionService service, Endpoint endpoint, Uri fileUri) {
        if (activeDialog != null)
            activeDialog.dismiss();

        CustomDialog.CustomOnClickListener listenerPositve = new CustomDialog.CustomOnClickListener() {
            @Override
            public void onClick(View v) {
                service.startPetition(endpoint, FileExchangePetition.FILE_EXCHANGE_TYPE, fileUri);
                mDialog.dismiss();
            }
        };

        CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
        builder.setCancelable(false)
                .setBackgroundColor(warningColor)
                .isVisibleBtnPositive(true)
                .isVisibleBtnNegative(true)
                .setTextPositiveBtn("Continue")
                .setTextNegativeBtn("Cancel")
                .setCustomPositiveListener(listenerPositve)
                .setIcon(R.drawable.ic_warning_white_24dp)
                .setText(mContext.getString(R.string.file_exchange_warning_size));

        activeDialog = builder.show();
    }

    public void updateFileExchangeDialog(String text, boolean progressBar, boolean progressBarNumbered, int percent, boolean btnPositive, boolean btnNegative, CustomDialog.CustomOnClickListener negListener) {
        if (activeDialog != null) {
            activeDialog.setNoProgressBars();
            activeDialog.setIcon(R.drawable.ic_connect_white_24dp);
            activeDialog.setImageColor(primaryColor);

            if (progressBar)
                activeDialog.setProgressBarActive();
            if (progressBarNumbered)
                activeDialog.setNumberedProgresBarActive(percent);
            if (negListener != null)
                activeDialog.setButtonListenerNegative(negListener);

            activeDialog.setVisibleBtnPositive(btnPositive);
            activeDialog.setVisibleBtnNegative(btnNegative);
            activeDialog.setProgressText(text);
            activeDialog.show();
        } else {
            CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
            builder.setCancelable(false)
                    .setBackgroundColor(primaryColor)
                    .setIcon(R.drawable.ic_connect_white_24dp)
                    .setText(text)
                    .isVisibleBtnNegative(btnNegative)
                    .isVisibleBtnPositive(btnPositive);

            if (negListener != null)
                builder.setCustomNegativeListener(negListener);
            if (progressBar)
                builder.setProgressBarActive();
            if (progressBarNumbered)
                builder.setNumberedProgressBarActive(percent);

            activeDialog = builder.show();
        }
    }

    /*
     *
     * CHAT
     *
     * */

    public void createChatDialog() {
        if (activeDialog != null)
            activeDialog.dismiss();

        CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
        builder.setCancelable(false)
                .setBackgroundColor(primaryColor)
                .setIcon(R.drawable.ic_chat_white_24dp)
                .setProgressBarActive()
                .setText(mContext.getString(R.string.chat_start));

        activeDialog = builder.show();
    }

    public void createChatIncomeDialog(String text, AbstractPeticionNearby petition) {
        if (activeDialog != null)
            activeDialog.dismiss();

        Chatpetition pet = (Chatpetition) petition;

        CustomDialog.CustomOnClickListener listenerNegative = new CustomDialog.CustomOnClickListener() {
            @Override
            public void onClick(View v) {
                pet.sendReject();
                mDialog.dismiss();
            }
        };

        CustomDialog.CustomOnClickListener listenerPositive = new CustomDialog.CustomOnClickListener() {
            @Override
            public void onClick(View v) {
                pet.sendAccept();
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("endpointConnected", pet.getEndpointID());
                mContext.startActivity(intent);
                mDialog.dismiss();
            }
        };

        CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
        builder.setCancelable(false)
                .setBackgroundColor(primaryColor)
                .setIcon(R.drawable.ic_chat_white_24dp)
                .setText(mContext.getString(R.string.chat_start))
                .isVisibleBtnNegative(true)
                .setCustomNegativeListener(listenerNegative)
                .isVisibleBtnPositive(true)
                .setCustomPositiveListener(listenerPositive)
                .setText(text);

        activeDialog = builder.show();
    }


    /*
     *
     * Multifile
     *
     * */
    public void createMultiFileExchangeDialog(String username) {
        if (activeDialog != null)
            activeDialog.dismiss();

        CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
        builder.setCancelable(false)
                .setBackgroundColor(primaryColor)
                .setIcon(R.drawable.ic_connect_white_24dp)
                .setText(mContext.getString(R.string.multi_exchange_onConnect, username));

        activeDialog = builder.show();
    }

    public void updateMultiFileExchangeDialog(String text, boolean progressBar, boolean progressBarNumbered, int percent, boolean btnPositive, boolean btnNegative) {
        if (activeDialog != null) {
            activeDialog.setNoProgressBars();
            activeDialog.setIcon(R.drawable.ic_connect_white_24dp);
            activeDialog.setImageColor(primaryColor);
            if (progressBar)
                activeDialog.setProgressBarActive();
            if (progressBarNumbered)
                activeDialog.setNumberedProgresBarActive(percent);

            activeDialog.setVisibleBtnPositive(btnPositive);
            activeDialog.setVisibleBtnNegative(btnNegative);
            activeDialog.setProgressText(text);
            activeDialog.show();
        } else {
            CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
            builder.setCancelable(false)
                    .setBackgroundColor(primaryColor)
                    .setIcon(R.drawable.ic_connect_white_24dp)
                    .setText(text);
            if (progressBar)
                builder.setProgressBarActive();
            if (progressBarNumbered)
                builder.setNumberedProgressBarActive(percent);

            activeDialog = builder.show();
        }
    }


}
