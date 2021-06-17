package com.example.jorge.androidapp.adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.jorge.androidapp.R;
import com.example.jorge.androidapp.ui.messages.bottomSheetDialog.BottomSheetDialogHomeActivity;
import com.example.jorge.androidapp.entities.Device;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class DiscoveryDevicesAdapter extends RecyclerView.Adapter {


    private Context mContext;
    private List<Device> deviceList;

    public DiscoveryDevicesAdapter(Context context, List<Device> deviceList) {
        this.mContext = context;
        this.deviceList = deviceList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list, parent, false);
        return new DeviceHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Device device = deviceList.get(position);
        ((DeviceHolder) holder).bind(device);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BottomSheetDialogHomeActivity bottomSheetHome = new BottomSheetDialogHomeActivity();
                bottomSheetHome.setDevice(device);
                bottomSheetHome.setPosition(position);
                bottomSheetHome.show(((AppCompatActivity) mContext).getSupportFragmentManager(), "BottomSheetDialogHomeActivity");
            }
        });
    }


    private void updateDevice(int position, Device newDeviceUpdated) {
        deviceList.set(position, newDeviceUpdated);
        this.notifyItemChanged(position);

    }

    private int getDevicePosition(Device device) {
        int pos = 0;
        for (Device dev : deviceList) {
            if (dev.getDeviceID().equals(device.getDeviceID())) {
                return pos;
            } else {
                pos++;
            }

        }
        return -1;
    }

    public void clear() {
        int size = deviceList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                deviceList.remove(0);
            }

            notifyItemRangeRemoved(0, size);
        }
    }


    public void updateFriendStatus(String idDevice) {
        Device dev = getDeviceFromID(idDevice);
        if (dev != null) {
            int pos = getDevicePosition(dev);
            dev.setIsFriend(true);
            this.notifyItemChanged(pos);
        }
    }

    public void updateFriendConnected(String idDevice) {
        Device dev = getDeviceFromID(idDevice);
        if (dev != null) {
            int pos = getDevicePosition(dev);
            dev.setIsConnected(true);
            this.notifyItemChanged(pos);
        }
    }

    private Device getDeviceFromID(String deviceID) {
        for (Device device : deviceList) {
            if (device.getDeviceID().equals(deviceID))
                return device;
        }
        return null;
    }

    public int setDevice(Device device) {
        int pos = getDevicePosition(device);
        if (pos == -1) {
            this.deviceList.add(device);
            this.notifyItemInserted(deviceList.size() - 1);
            /*Mejor usar el notify inserted para no romper animaciones si hay*/
            //mAdapter.notifyDataSetChanged();
            return this.deviceList.size() - 1;
        } else {
            updateDevice(pos, device);
            return -1;
        }
    }


    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public class DeviceHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.input_deviceName)
        TextView inputDeviceName;
        @BindView(R.id.input_userName)
        TextView inputUserName;
        @BindView(R.id.userImage)
        CircleImageView profileImage;
        @BindView(R.id.item_button_friend)
        Button buttonFriend;
        @BindView(R.id.item_button_status)
        Button buttonConnected;


        DeviceHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(Device device) {
            inputDeviceName.setText(device.getDeviceName());
            inputUserName.setText(device.getDeviceUsername());
            if (device.isFriend()) {
                buttonFriend.setText(mContext.getString(R.string.item_friendOK));
                buttonFriend.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_green));
            }
            if (device.isConnected()) {
                buttonConnected.setText(mContext.getString(R.string.item_connected));
                buttonConnected.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_green));
            }

            byte[] imageBytes = device.getUserProfile();
            setImage(imageBytes);
        }

        private void setImage(byte[] bytes) {
            if (bytes != null && bytes.length > 0) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                profileImage.setImageBitmap(bmp);
            }
        }


    }

    public int getPositionById(String deviceID) {
        int result = -1;
        for (int i = 0; i <= deviceList.size() - 1; i++) {
            if (deviceList.get(i).getDeviceID().equals(deviceID)) {
                result = i;
                break;
            }
        }
        return result;
    }

    public void removeAt(int position) {
        deviceList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, deviceList.size());
    }

}
