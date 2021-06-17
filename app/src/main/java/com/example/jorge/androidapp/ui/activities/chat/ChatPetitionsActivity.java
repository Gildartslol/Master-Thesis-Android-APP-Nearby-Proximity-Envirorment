package com.example.jorge.androidapp.ui.activities.chat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;

import com.example.jorge.androidapp.R;
import com.example.jorge.androidapp.adapter.MessageListAdapter;
import com.example.jorge.androidapp.entities.User;
import com.example.jorge.androidapp.entities.UserMessage;
import com.example.jorge.androidapp.framework.Utils;
import com.example.jorge.androidapp.framework.nearby.interfaces.MyNearbyPetitionListener;
import com.example.jorge.androidapp.framework.security.ApplicationCipher;
import com.example.jorge.androidapp.network.petition.AbstractPeticionNearby;
import com.example.jorge.androidapp.network.petition.Chatpetition;
import com.example.jorge.androidapp.ui.activities.BaseActivity;
import com.example.jorge.androidapp.users.UsersDataBaseHelper;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public abstract class ChatPetitionsActivity extends BaseActivity implements MyNearbyPetitionListener {


    @BindView(R.id.recyclerView_message_list)
    protected RecyclerView mMessageRecycler;

    @BindView(R.id.edittext_chatbox)
    protected EditText editText;


    protected String TAG = "CHAT_ACTIVITY";
    protected int VIEW_TYPE_MESSAGE_SENT = 1;
    protected int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    protected String endpointConnected;
    protected Chatpetition petition;
    protected ApplicationCipher cipher;
    protected MessageListAdapter mMessageAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*set status color bar*/
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        /*Vista*/
        List<UserMessage> messageList = new ArrayList<>();
        mMessageAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);

        /*Conexion*/
        endpointConnected = getIntent().getStringExtra("endpointConnected");
        logV("endpointConnected : " + endpointConnected);
        petition = (Chatpetition) service.getPetition(endpointConnected, Chatpetition.CHAT_TYPE);


        /*Titulo*/
        String username = Utils.getUserNameFromNetPacket(petition.getDestinationEndpoint().getName());
        setTitle(username);

        /*Base de datos*/
        UsersDataBaseHelper userBbdd = new UsersDataBaseHelper(this);
        User user = userBbdd.getUser(petition.getEndpointUserId());
        byte[] bitesImage = user.getUserImage();
        if (bitesImage != null && bitesImage.length > 0) {
            Bitmap bmp = BitmapFactory.decodeByteArray(user.getUserImage(), 0, user.getUserImage().length);
            mMessageAdapter.setImageUserDestination(bmp);
        }

        /*Cifrador*/
        cipher = new ApplicationCipher(this, false);
    }

    @Override
    public int getLayoutId() {
        return 0;
    }

    @Override
    public void setServicesParameters() {
        if (service != null)
            service.setMyNearbyPetitionListener(this);
    }

    @Override
    protected String getTag() {
        return TAG;
    }


    /***
     ********************************************************************************
     ********************************************************************************
     *****************************  PETITIONS ***************************************
     ********************************************************************************
     ********************************************************************************
     */

    @Override
    public void onUpdatePetition(AbstractPeticionNearby petition, PayloadTransferUpdate update) {

        if (petition.getType() == Chatpetition.CHAT_TYPE) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Chatpetition pet = (Chatpetition) petition;
                    if (pet.getPetitionStep() == Chatpetition.CHAT_MESSAGE) {
                        UserMessage message = pet.getActualUserMessage();
                        //UserMessage message = new UserMessage(infoMsg, pet.getEndpointUsername(), System.currentTimeMillis());
                        message.setType(UserMessage.MESSAGE_RECEIVED);
                        if (message.isEncrypted()) {
                            //logI("ANTES = " + new String(message.getMessage()));
                            String deEncryted = cipher.decipherData(message.getMessage());
                            logI("__DESENCRIPTADO " + deEncryted);
                            message.setMessage(deEncryted.getBytes());
                        }
                        int number = mMessageAdapter.setMessage(message);
                        RecyclerView.ViewHolder holder = mMessageAdapter.onCreateViewHolder(mMessageRecycler, VIEW_TYPE_MESSAGE_RECEIVED);
                        mMessageAdapter.onBindViewHolder(holder, number);
                        //mMessageRecycler.getAdapter().notifyDataSetChanged();
                        mMessageAdapter.notifyItemChanged(number);
                    }
                }
            });
        }

    }

    @Override
    public void onPetitionEnd(AbstractPeticionNearby petition) {
        if (petition.getPetitionStep() == Chatpetition.TIMEOUT) {
            dialogManager.createChatClientLeave(getString(R.string.chat_timeout));
        } else {
            dialogManager.createChatClientLeave(getString(R.string.chat_end));
            service.deletePetition(endpointConnected, Chatpetition.CHAT_TYPE);
        }

    }

    @Override
    public void onPetitionFailure(AbstractPeticionNearby petition) {
        //createEndChat("Connection Error");
        dialogManager.createChatClientLeave(getString(R.string.chat_error));
        service.deletePetition(endpointConnected, Chatpetition.CHAT_TYPE);
    }

}
