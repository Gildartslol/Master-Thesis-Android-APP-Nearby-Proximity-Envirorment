package com.example.jorge.androidapp.ui.activities.home;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.jorge.androidapp.R;
import com.example.jorge.androidapp.adapter.DiscoveryDevicesAdapter;
import com.example.jorge.androidapp.constantes.KConstantesShareContent;
import com.example.jorge.androidapp.entities.Device;
import com.example.jorge.androidapp.ui.activities.BaseActivity;
import com.example.jorge.androidapp.ui.activities.friends.FriendListActivity;
import com.example.jorge.androidapp.ui.settings.ActivitySettings;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

public abstract class Home_ActionToggleBar_Activity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.drawer_layout)
    protected DrawerLayout drawer;

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    @BindView(R.id.nav_view)
    protected NavigationView navigationView;


    protected CircleImageView profilePic;

    private TextView profileUsername;
    private View header;
    private Menu mOptionsMenu;

    protected DiscoveryDevicesAdapter devicesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        /*HEADER*/
        header = navigationView.getHeaderView(0);
        /*Nombre de perfil*/
        profileUsername = header.findViewById(R.id.nav_header_userName);
        String username = readFromPreferences(this.getString(R.string.key_username));
        if (!username.equals(""))
            profileUsername.setText(username);

        /*imagen de perfil*/
        profilePic = header.findViewById(R.id.nav_header_imageView);
        String uriString = readFromPreferences(this.getString(R.string.key_profile_uri));
        Bitmap b = null;
        if (!uriString.equals("")) {
            File f = new File(uriString, "profile.jpg");
            try {
                b = BitmapFactory.decodeStream(new FileInputStream(f));
                profilePic.setImageBitmap(b);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        /*Adapter*/
        ArrayList<Device> devices = new ArrayList<>();
        devicesAdapter = new DiscoveryDevicesAdapter(this, devices);

    }


    @Override
    public int getLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    protected void onResume() {
        super.onResume();
        profileUsername = header.findViewById(R.id.nav_header_userName);
        String username = readFromPreferences(this.getString(R.string.key_username));
        if (!username.equals(""))
            profileUsername.setText(username);
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    /**
     * Infla el menu añadiendo los items de la barra de acciones si es necesario.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mOptionsMenu = menu;
        MenuItem liveItem = mOptionsMenu.getItem(1);
        changeColorMenu(liveItem, ContextCompat.getColor(this, R.color.green));

        return true;
    }

    private void changeColorMenu(MenuItem liveItem, int color) {
        SpannableString s = new SpannableString(liveItem.getTitle().toString());
        s.setSpan(new ForegroundColorSpan(color), 0, s.length(), 0);
        liveItem.setTitle(s);
    }


    public void updateProfilePicture(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), KConstantesShareContent.ACTIVITY_RESULT_CODE.PICK_IMAGE_REQUEST_CODE);
    }


    /**
     * Metodo para guardar la imagen de perfil y su uri
     *
     * @return
     */
    protected String saveToInternalStorage(Uri uri) throws IOException {
        AssetFileDescriptor afd = null;
        long fileSize = 0;
        try {
            afd = getContentResolver().openAssetFileDescriptor(uri, "r");
            if (afd != null)
                fileSize = afd.getLength();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (afd != null) {
                try {
                    afd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        long fileSizeInKB = fileSize / 1024;
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

        // Create imageDir
        File mypath = new File(directory, "profile.jpg");

        //Check size
        if (fileSizeInKB > 200) {
            dialogManager.updateErrorDialog(getString(R.string.size_error));
        } else {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mypath);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null)
                        fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return directory.getAbsolutePath();
        }
        return "";
    }


    /**
     * Soporte para el menu de opciones
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_point) {
            if (service.getStrategy() == Strategy.P2P_STAR) {
                service.setStrategy(Strategy.P2P_POINT_TO_POINT);
                service.stopAdvertising();
                service.disconnectFromAllEndpoints();
                service.startAdvertising();
                devicesAdapter.clear();
                changeColorMenu(item, ContextCompat.getColor(this, R.color.green));
                changeColorMenu(mOptionsMenu.getItem(1), ContextCompat.getColor(this, R.color.black));
            }
            return true;
        }
        if (id == R.id.menu_start) {
            if (service.getStrategy() == Strategy.P2P_POINT_TO_POINT) {
                service.setStrategy(Strategy.P2P_STAR);
                service.stopAdvertising();
                service.disconnectFromAllEndpoints();
                service.startAdvertising();
                devicesAdapter.clear();
                changeColorMenu(item, ContextCompat.getColor(this, R.color.green));
                changeColorMenu(mOptionsMenu.getItem(0), ContextCompat.getColor(this, R.color.black));
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Función asociada al boton de actualizar los dispositivos.
     */
    public void resetAll(View view) {
        service.stopAllEndpoints();
        devicesAdapter.clear();
        service.stopDiscovering();
    }


    /**
     * Soporte para la los clicks en los items de navigacion.
     */

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_friendList) {

            startActivity(new Intent(this, FriendListActivity.class));
            return true;

        } else if (id == R.id.nav_manage) {

            startActivity(new Intent(this, ActivitySettings.class));
            return true;

        } else if (id == R.id.nav_share_file) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, KConstantesShareContent.ACTIVITY_RESULT_CODE.MULTIFILE_REQUEST_CODE);
        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_folder) {
            Uri selectedUri = Uri.parse(Environment.getExternalStorageDirectory() + "/Download/Nearby/");
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setDataAndType(selectedUri, "resource/folder");
            if (intent.resolveActivityInfo(getPackageManager(), 0) != null) {
                startActivity(Intent.createChooser(intent, "Open folder"));
            }

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void onSwitchDiscovery(View view) {
        if (service.isDiscovering()) {
            service.stopDiscovering();
        } else {
            service.startDiscovering();
        }
    }

    public void onSwitchAdvertising(View view) {
        if (service.isAdvertising()) {
            service.stopAdvertising();
        } else {
            service.startAdvertising();
        }
    }


}
