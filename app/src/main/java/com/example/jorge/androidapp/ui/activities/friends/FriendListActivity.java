package com.example.jorge.androidapp.ui.activities.friends;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.jorge.androidapp.R;
import com.example.jorge.androidapp.adapter.FriendListAdapter;
import com.example.jorge.androidapp.entities.User;
import com.example.jorge.androidapp.framework.Utils;
import com.example.jorge.androidapp.ui.activities.home.HomeActivity;
import com.example.jorge.androidapp.ui.messages.bottomSheetDialog.interfaces.BottomSheetFriendListListener;
import com.example.jorge.androidapp.users.DatabaseResult;
import com.example.jorge.androidapp.users.UsersDataBaseHelper;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;

public class FriendListActivity extends FriendListPetitionsActivity implements BottomSheetFriendListListener {


    @BindView(R.id.recycleViewUsers)
    RecyclerView recycleViewUsers;

    @BindView(R.id.textView_empty_view_users)
    TextView emptyView;

    @BindView(R.id.button_friends_friends_list)
    Button button_friends;

    @BindView(R.id.button_blocked_friends_list)
    Button button_blocked;

    private UsersDataBaseHelper userBbdd;
    private FriendListAdapter friendListAdapter;


    private ArrayList<User> adapterArray;
    private User[] usersFriends;
    private User[] usersBlocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        adapterArray = new ArrayList<>();

        /*Color de la barra de notificaciones*/
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        /*Base de datos*/
        userBbdd = new UsersDataBaseHelper(this);
        usersFriends = userBbdd.getFriendsRegistered(false);
        usersBlocked = userBbdd.getFriendsRegistered(true);


        /*Configuracion de la pantalla de items de amigos*/
        adapterArray = new ArrayList<>(Arrays.asList(usersFriends));

        friendListAdapter = new FriendListAdapter(this, adapterArray);
        this.friendListAdapter.setEmptyView(emptyView);

        recycleViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recycleViewUsers.setAdapter(friendListAdapter);

        /*Caso lista vacia*/
        this.friendListAdapter.checkForEmptyList();
        this.button_friends.setSelected(true);

        /*set Button numer*/
        this.button_friends.setText(getString(R.string.button_friends, usersFriends.length));
        this.button_blocked.setText(getString(R.string.button_blocked, usersBlocked.length));

    }

    public void onBackPressed() {
        this.startActivity(new Intent(this, HomeActivity.class));
        return;
    }


    @Override
    public int getLayoutId() {
        return R.layout.activity_friend_list;
    }

    @Override
    public void setServicesParameters() {

    }


    @Override
    public String getTag() {
        return TAG;
    }


    public void onFriendPressed(View view) {
        if (button_blocked.isSelected()) {
            this.button_friends.setSelected(true);
            this.button_blocked.setSelected(false);

            this.friendListAdapter.setType(FriendListAdapter.TYPE_FRIEND);
            adapterArray = new ArrayList<>(Arrays.asList(usersFriends));

            this.friendListAdapter.updateData(this.adapterArray);

            /*Comprobamos si esta vacio el adapter*/
            this.friendListAdapter.checkForEmptyList();
        }


    }

    public void onBlockedPressed(View view) {
        if (button_friends.isSelected()) {
            this.button_friends.setSelected(false);
            this.button_blocked.setSelected(true);

            this.friendListAdapter.setType(FriendListAdapter.TYPE_BLOCK);
            adapterArray = new ArrayList<>(Arrays.asList(usersBlocked));
            this.friendListAdapter.updateData(adapterArray);

            /*Comprobamos si esta vacio el adapter*/
            this.friendListAdapter.checkForEmptyList();
        }


    }

    private void reloadUsers() {
        this.usersBlocked = userBbdd.getFriendsRegistered(true);
        this.usersFriends = userBbdd.getFriendsRegistered(false);
    }

    @Override
    public void onDeleteUser(String idUser, int position) {
        DatabaseResult result;
        result = this.userBbdd.deleteFriend(idUser);
        if (result.isTransactionOK()) {
            friendListAdapter.removeAt(position);
            reloadUsers();
            Utils.createToastShort(this, result.getMessage());
        } else {
            Utils.createToastShort(this, result.getMessage());
        }
        this.button_friends.setText(getString(R.string.button_friends, usersFriends.length));
    }

    @Override
    public void onBlockUser(String idUser, int position) {
        DatabaseResult result;
        result = userBbdd.blockUser(idUser);
        if (result.isTransactionOK()) {
            friendListAdapter.removeAt(position);
            reloadUsers();
            Utils.createToastLong(this, result.getMessage());
        } else {
            Utils.createToastLong(this, result.getMessage());
        }
        this.button_blocked.setText(getString(R.string.button_blocked, usersBlocked.length));
    }


    @Override
    public void onUnBlockUser(String idUser, int position) {
        DatabaseResult result;
        result = this.userBbdd.unBlockUser(idUser);
        if (result.isTransactionOK()) {
            friendListAdapter.removeAt(position);
            reloadUsers();
            Utils.createToastShort(this, result.getMessage());
        } else {
            Utils.createToastShort(this, result.getMessage());
        }
        this.button_blocked.setText(getString(R.string.button_blocked, usersBlocked.length));
    }


}
