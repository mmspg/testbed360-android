package ch.epfl.mmspg.testbed360;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

import ch.epfl.mmspg.testbed360.image.ImagesSession;

public class StartActivity extends AppCompatActivity {
    private final static int FILE_PERMISSION_REQUEST_CODE = 1;
    private final static String TAG = "StartActivity";

    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView noSessionText;
    private ListView sessionsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.start_activity_toolbar);
        setSupportActionBar(myToolbar);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.session_swipe_refresh);
        noSessionText = (TextView) findViewById(R.id.noSessionText);
        sessionsListView = (ListView) findViewById(R.id.sessionsListView);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                seekSessions();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //TODO fix crash on my Xiaomi Mi5s on very first start
        if(isStoragePermissionGranted()){
            //startVRActivity();
            seekSessions();
        }
    }

    private void startVRActivity() {
        startActivity(new Intent(this, VRViewActivity.class));
    }

    /**
     * Starts the task to load sessions in a background thread and changes the UI accordingly.
     * see {@link ch.epfl.mmspg.testbed360.image.ImagesSession.LoadTask#doInBackground(Activity...)}
     */
    private void seekSessions(){
        swipeRefreshLayout.setRefreshing(true);
        noSessionText.setVisibility(View.GONE);
        sessionsListView.setVisibility(View.GONE);
        sessionsListView.setAdapter(null);

        ImagesSession.LoadTask task = ImagesSession.getLoadingTask(this);
        task.execute(this);
        try {
            sessionsListView.setAdapter(new ImagesSession.Adapter(this,R.layout.session_list_item, task.get()));
            if(sessionsListView.getAdapter() != null && sessionsListView.getAdapter().getCount() == 0){
                noSessionText.setText(R.string.noSessionExplanation);
                noSessionText.setVisibility(View.VISIBLE);
            }
        } catch (InterruptedException | ExecutionException e) {
            noSessionText.setText(R.string.error_loading_images);
            e.printStackTrace();
        }
        swipeRefreshLayout.setRefreshing(false);
        sessionsListView.setVisibility(View.VISIBLE);
    }

    public void openHelp(MenuItem menuItem){
        //TODO open README.md on GitHub repo
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if(id==R.id.menu_help){
            return true;
        }
        return super.onOptionsItemSelected(item);
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
