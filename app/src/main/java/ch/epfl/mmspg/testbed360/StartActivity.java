package ch.epfl.mmspg.testbed360;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

import br.tiagohm.markdownview.MarkdownView;
import br.tiagohm.markdownview.css.InternalStyleSheet;
import br.tiagohm.markdownview.css.styles.Github;
import ch.epfl.mmspg.testbed360.image.ImagesSession;

public class StartActivity extends AppCompatActivity {
    private final static int FILE_PERMISSION_REQUEST_CODE = 1;
    private final static String TAG = "StartActivity";

    private SessionsListFragment sessionsListFragment;
    private HelpFragment helpFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.start_activity_toolbar);
        setSupportActionBar(myToolbar);

        helpFragment = new HelpFragment();
        sessionsListFragment = new SessionsListFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment_holder, sessionsListFragment);
        fragmentTransaction.add(R.id.fragment_holder, helpFragment);
        fragmentTransaction.commit();

        showSessionsListFragment();
    }

    private void showSessionsListFragment(){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.hide(helpFragment);
        fragmentTransaction.show(sessionsListFragment);
        fragmentTransaction.commit();
    }

    private void showHelpFragment(){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.hide(sessionsListFragment);
        fragmentTransaction.show(helpFragment);
        fragmentTransaction.commit();

    }

    @Override
    public void onBackPressed() {
        if(helpFragment.isVisible()){
            showSessionsListFragment();
        }else{
            super.onBackPressed();
        }
    }

    private void startVRActivity() {
        startActivity(new Intent(this, VRViewActivity.class));
    }

    public void openHelp(MenuItem menuItem) {
        if(helpFragment.isVisible()){
            showSessionsListFragment();
        }else{
            showHelpFragment();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_help) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static boolean isStoragePermissionGranted(Activity activity) {
        //credits to MetaSnarf
        // https://stackoverflow.com/questions/33162152/storage-permission-error-in-marshmallow

        if (Build.VERSION.SDK_INT >= 23) {
            if (activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission is granted (API>23)");
                return true;
            } else {

                Log.d(TAG, "Permission is revoked (API>23)");
                ActivityCompat.requestPermissions(activity,
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
        } else if (requestCode == FILE_PERMISSION_REQUEST_CODE
                && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            //TODO display a VRButton to enable file access to continue !
        }
    }

    public static class SessionsListFragment extends Fragment {
        private SwipeRefreshLayout swipeRefreshLayout;
        private TextView noSessionText;
        private ListView sessionsListView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View v = inflater.inflate(R.layout.sessions_list_fragment, container, false);
            swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.session_swipe_refresh);
            noSessionText = (TextView) v.findViewById(R.id.noSessionText);
            sessionsListView = (ListView) v.findViewById(R.id.sessionsListView);

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    seekSessions();
                }
            });

            return v;
        }

        /**
         * Starts the task to load sessions in a background thread and changes the UI accordingly.
         * see {@link ch.epfl.mmspg.testbed360.image.ImagesSession.LoadTask#doInBackground(Activity...)}
         */
        private void seekSessions() {
            swipeRefreshLayout.setRefreshing(true);
            noSessionText.setVisibility(View.GONE);
            sessionsListView.setVisibility(View.GONE);
            sessionsListView.setAdapter(null);

            ImagesSession.LoadTask task = ImagesSession.getLoadingTask(getContext());
            task.execute(getActivity());
            try {
                sessionsListView.setAdapter(new ImagesSession.Adapter(getContext(), R.layout.session_list_item, task.get()));
                if (sessionsListView.getAdapter() != null && sessionsListView.getAdapter().getCount() == 0) {
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

        @Override
        public void onStart() {
            super.onStart();
            //TODO fix crash on my Xiaomi Mi5s on very first start
            if (isStoragePermissionGranted(getActivity())) {
                //startVRActivity();
                seekSessions();
            }
        }
    }

    public static class HelpFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.help_fragment, container, false);
        }

        @Override
        public void onStart() {
            super.onStart();
            MarkdownView mdView = (MarkdownView) getActivity().findViewById(R.id.markdown_view);
            InternalStyleSheet css = new Github();
            css.addRule("body",
                    "font-family: \"Helvetica Neue\", Helvetica, Arial, sans-serif",
                    "font-size: 14px", "line-height: 1.42857143",
                    "color: rgba(255, 255, 255, 0.75)",
                    "background-color: #"+Integer.toHexString(getResources().getColor(R.color.bg_dark)).substring(2),
                    "margin: 0"
            );
            /*css.addRule("code",
                    "color: #"+Integer.toHexString(getResources().getColor(R.color.colorPrimary)).substring(2)
                    );
            css.addRule("code class=\"nohighlight\"",
                    "color: #"+Integer.toHexString(getResources().getColor(R.color.colorPrimary)).substring(2)
                    );*/
            //css.addRule("code", "max-width: 100%");
            mdView.addStyleSheet(css);
            mdView.loadMarkdownFromAsset("How to add pictures.md");
        }
    }
}
