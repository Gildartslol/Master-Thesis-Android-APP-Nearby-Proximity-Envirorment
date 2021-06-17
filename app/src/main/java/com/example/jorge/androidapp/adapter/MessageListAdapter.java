package com.example.jorge.androidapp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.jorge.androidapp.R;
import com.example.jorge.androidapp.framework.Utils;
import com.example.jorge.androidapp.entities.UserMessage;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageListAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private Context mContext;
    private List<UserMessage> mMessageList;

    private Bitmap imageUserDestination;

    public MessageListAdapter(Context context, List<UserMessage> messageList) {
        mContext = context;
        mMessageList = messageList;
        imageUserDestination = null;
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    public int getItemViewType(int position) {
        UserMessage message =  mMessageList.get(position);
        return message.getType();
    }


    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    public int setMessage(UserMessage message) {
        this.mMessageList.add(message);
        notifyDataSetChanged();
        return this.mMessageList.size() - 1;
    }


    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        UserMessage message = mMessageList.get(position);

        switch (message.getType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    public void setImageUserDestination(Bitmap imageUserDestination) {
        this.imageUserDestination = imageUserDestination;
    }

    public class SentMessageHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.send_text_message_body)
        TextView messageText;
        @BindView(R.id.send_text_message_time)
        TextView timeText;
        @BindView(R.id.send_text_message_encryption)
        ImageButton encryption;

        SentMessageHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(UserMessage message) {
            messageText.setText(new String(message.getMessage()));
            timeText.setText(Utils.formatTime(message.getTimeStamp()));
            if (message.isEncrypted())
                encryption.setBackground(mContext.getDrawable(R.drawable.ic_enhanced_encryption_black_24dp));
            else
                encryption.setBackground(mContext.getDrawable(R.drawable.ic_no_encryption_black_24dp));
        }
    }


    public class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.received_text_message_body)
        TextView messageText;
        @BindView(R.id.received_text_message_time)
        TextView timeText;
        @BindView(R.id.received_text_message_name)
        TextView nameText;
        @BindView(R.id.image_message_profile)
        CircleImageView image;
        @BindView(R.id.received_text_message_encryption)
        ImageButton encryption;


        ReceivedMessageHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(UserMessage message) {
            messageText.setText(new String(message.getMessage()));
            timeText.setText(Utils.formatTime(message.getTimeStamp()));
            nameText.setText(message.getUser());
            if (imageUserDestination != null)
                image.setImageBitmap(imageUserDestination);
            if (message.isEncrypted())
                encryption.setBackground(mContext.getDrawable(R.drawable.ic_enhanced_encryption_black_24dp));
            else
                encryption.setBackground(mContext.getDrawable(R.drawable.ic_no_encryption_black_24dp));
        }
    }
}

