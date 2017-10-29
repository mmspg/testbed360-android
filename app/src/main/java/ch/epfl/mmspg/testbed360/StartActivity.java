package ch.epfl.mmspg.testbed360;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class StartActivity extends AppCompatActivity {
    private final static int FILE_PERMISSION_REQUEST_CODE = 1;
    private final static String TAG = "StartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //TODO fix crash on my Xiaomi Mi5s on very first start
        if(isStoragePermissionGranted()){
            startVRActivity();
        }
    }

    private void startVRActivity() {
        startActivity(new Intent(this, VRViewActivity.class));
    }


    public boolean isStoragePermissionGranted() {
        //credits to MetaSnarf
        // https://stackoverflow.com/questions/33162152/storage-permission-error-in-marshmallow

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission is granted (API>23)");
                return true;
            } else {

                Log.d(TAG, "Permission is revoked (API>23)");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        FILE_PERMISSION_REQUEST_CODE);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.d(TAG, "Permission is granted (API<23)");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FILE_PERMISSION_REQUEST_CODE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission: " + permissions[0] + " was granted.");
            startVRActivity();
        } else if(requestCode == FILE_PERMISSION_REQUEST_CODE
                && grantResults[0] == PackageManager.PERMISSION_DENIED){
            //TODO display a VRButton to enable file access to continue !
        }
    }
}
