package com.example.shuang.indoor;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private List<Class> mClasses = new ArrayList<>();
    private Class mClass;
    boolean locationPermission;
    boolean storagePermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //用ArrayList 存储两个Activity 0是Image 1是Map
        mClasses.add(ImageViewActivity.class);
        mClasses.add(MapViewActivity.class);
        mClass = mClasses.get(0);//现在使用的是ImageView
//        mClass = mClasses.get(1);//现在使用的是MapView
        locationPermission = PermissionsManager.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        storagePermission = PermissionsManager.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        findViewById(R.id.text1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    startActivity(new Intent(MainActivity.this, mClass)
                            .putExtra("floorPlanId", "68c4d9a7-b06b-46cc-be69-ffd3455044a3"));
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean checkPermission() {
        Logger.i("Has " + Manifest.permission.ACCESS_FINE_LOCATION + " & "
                + Manifest.permission.WRITE_EXTERNAL_STORAGE + " permission: "
                + locationPermission + " & " + storagePermission);
        if (!locationPermission || !storagePermission) {
            PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(this,
                    new String[]{
                            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    new PermissionsResultAction() {
                        @Override
                        public void onGranted() {
                            Logger.e("all permissions granted");
                        }

                        @Override
                        public void onDenied(String permission) {
                            String message = String.format(Locale.getDefault(), "Permission \\\"%1$s\\\" has been denied.", permission);
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
            return false;
        } else {
            return true;
        }
    }

}
