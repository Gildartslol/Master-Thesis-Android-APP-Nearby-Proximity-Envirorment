package com.example.jorge.androidapp.adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jorge.androidapp.R;
import com.example.jorge.androidapp.ui.messages.bottomSheetDialog.BottomSheetDialogFriendList;
import com.example.jorge.androidapp.entities.User;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class FriendListAdapter extends RecyclerView.Adapter {


    private Context mContext;
    private List<User> userList;
    private RecyclerView mRecyclerView;
    private TextView emptyView;
    private int type = 0;

    public static int TYPE_FRIEND = 0;
    public static int TYPE_BLOCK = 1;

    public FriendListAdapter(Context context, List<User> userList) {
        this.mContext = context;
        this.userList = userList;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_list, parent, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        User user = userList.get(position);
        ((UserHolder) holder).bind(user, position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BottomSheetDialogFriendList bottomSheet = new BottomSheetDialogFriendList();
                bottomSheet.setUser(user);
                bottomSheet.setType(type);
                bottomSheet.setPosition(position);
                bottomSheet.show(((AppCompatActivity) mContext).getSupportFragmentManager(), "BottomSheetDialogFriendList");

            }
        });

    }

    public void updateData(ArrayList<User> newData) {
        userList.clear();
        userList.addAll(newData);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {

        return userList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    /*Caso de no usuarios*/
    public void checkForEmptyList() {

        if (type == TYPE_BLOCK)
            emptyView.setText(mContext.getText(R.string.friendList_no_data_block_available));
        if (type == TYPE_FRIEND)
            emptyView.setText(mContext.getText(R.string.friendList_no_data_available));

        if (userList.size() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    public void removeAt(int position) {
        userList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, userList.size());
        checkForEmptyList();
    }

    public void setEmptyView(TextView emptyView) {
        this.emptyView = emptyView;
    }

    public void setType(int type) {
        this.type = type;
    }

    public class UserHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.friend_userName)
        TextView inputFriendUsername;
        @BindView(R.id.friend_device)
        TextView inputUseDevice;
        @BindView(R.id.friend_android_id)
        TextView inputFriendAndroidID;
        @BindView(R.id.friend_image)
        CircleImageView inputImage;


        UserHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(User user, int position) {

            inputFriendUsername.setText(user.getUserName());
            inputUseDevice.setText(user.getUserDeviceName());
            inputFriendAndroidID.setText(user.getUserID());
            byte[] imageBytes = user.getUserImage();
            setImage(imageBytes);
        }

        private void setImage(byte[] bytes){
            if (bytes != null && bytes.length > 0) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                inputImage.setImageBitmap(bmp);
            }
        }

    }

}
