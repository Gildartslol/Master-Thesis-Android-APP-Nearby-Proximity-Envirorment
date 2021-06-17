package com.example.jorge.androidapp.ui.activities.chat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.jorge.androidapp.R;
import com.example.jorge.androidapp.entities.UserMessage;
import com.example.jorge.androidapp.network.petition.Chatpetition;
import com.example.jorge.androidapp.ui.activities.KConstantesActivities;

public class ChatActivity extends ChatPetitionsActivity{



    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_message_list;
    }

    @Override
    public void setServicesParameters() {
        /*Listeners*/
        if (service != null) {
            service.setMyNearbyPetitionListener(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        service.stopDiscovering();
    }

    @Override
    public String getTag() {
        return TAG;
    }


    @Override
    public void onBackPressed() {
        petition.sendEnd();
        service.deletePetition(endpointConnected, Chatpetition.CHAT_TYPE);
        super.onBackPressed();
    }


    public void sendButton(View view) {

        UserMessage message;
        final String user = service.getName();
        final long timestamp = System.currentTimeMillis();

        String text = editText.getText().toString();
        if (editText != null && !editText.getText().toString().equals("")) {

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    boolean isEncrypted = true;
                    byte[] encryptedMessage = null;
                    UserMessage message = new UserMessage(user, timestamp);
                    try {
                        encryptedMessage = cipher.cipherData(text);
                        cipher.closeAll();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.w(TAG, "Error al encriptar");
                        isEncrypted = false;
                    }
                    message.setType(UserMessage.MESSAGE_SENT);
                    message.setEncrypted(isEncrypted);
                    if (!isEncrypted)
                        message.setMessage(text.getBytes());
                    else
                        message.setMessage(encryptedMessage);

                    petition.sendMessage(message);
                }
            });

            message = new UserMessage(text.getBytes(), user, timestamp);
            message.setType(VIEW_TYPE_MESSAGE_SENT);
            int number = mMessageAdapter.setMessage(message);
            RecyclerView.ViewHolder holder = mMessageAdapter.onCreateViewHolder(mMessageRecycler, VIEW_TYPE_MESSAGE_SENT);
            mMessageAdapter.onBindViewHolder(holder, number);
            mMessageRecycler.getAdapter().notifyItemChanged(number);
        }

        if (editText != null)
            editText.setText(KConstantesActivities.MESSAGELIST.EMPTY_CHAT);

    }



}
