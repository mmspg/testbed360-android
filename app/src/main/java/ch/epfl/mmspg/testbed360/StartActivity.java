// Copyright (C) 2017 ECOLE POLYTECHNIQUE FEDERALE DE LAUSANNE, Switzerland
// Multimedia Signal Processing Group
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.
//

package ch.epfl.mmspg.testbed360;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

import br.tiagohm.markdownview.MarkdownView;
import br.tiagohm.markdownview.css.InternalStyleSheet;
import br.tiagohm.markdownview.css.styles.Github;
import ch.epfl.mmspg.testbed360.image.ImagesSession;

/**
 * This is the very first activity displayed, which prompts choices of {@link ImagesSession} to launch
 * within the {@link SessionsListFragment}, and gives you to see some explanations with the
 * {@link HelpFragment}. Will display a {@link PermissionRequestFragment} if the user has denied the
 * app to access the filesystem !
 */
public class StartActivity extends AppCompatActivity {
    private final static int FILE_PERMISSION_REQUEST_CODE = 3;
    private final static String TAG = "StartActivity";

    private SessionsListFragment sessionsListFragment;
    private HelpFragment helpFragment;
    private PermissionRequestFragment permissionRequestFragment;

    private boolean resumeToHelp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.start_activity_toolbar);
        setSupportActionBar(myToolbar);

        helpFragment = new HelpFragment();
        sessionsListFragment = new SessionsListFragment();
        permissionRequestFragment = new PermissionRequestFragment();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment_holder, sessionsListFragment);
        fragmentTransaction.add(R.id.fragment_holder, helpFragment);
        fragmentTransaction.add(R.id.fragment_holder, permissionRequestFragment);
        fragmentTransaction.commitNow();

        showSessionsListFragment();
    }

    /**
     * Checks if the app has the correct permissions to read an write to Android's filesystem, and
     * sets the {@link #sessionsListFragment} to the foreground if it's the case, or the
     * {@link #permissionRequestFragment}
     * <p>
     * see {@link #isStoragePermissionGranted(Activity)}
     */
    private void showSessionsListFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (isStoragePermissionGranted(this)) {
            fragmentTransaction.hide(permissionRequestFragment);
            fragmentTransaction.hide(helpFragment);
            fragmentTransaction.show(sessionsListFragment);
        } else {
            fragmentTransaction.hide(helpFragment);
            fragmentTransaction.hide(sessionsListFragment);
            fragmentTransaction.show(permissionRequestFragment);
        }
        fragmentTransaction.commitNowAllowingStateLoss();
    }

    /**
     * Sets the {@link #sessionsListFragment} to the foreground, hiding all other fragments
     */
    private void showHelpFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.hide(sessionsListFragment);
        fragmentTransaction.hide(permissionRequestFragment);
        fragmentTransaction.show(helpFragment);
        fragmentTransaction.commitNowAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        if (helpFragment.isVisible()) {
            //hide the help, and shows the sessions
            showSessionsListFragment();
        } else {
            //quit the app as for any app
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (resumeToHelp) {
            showHelpFragment();
        } else {
            showSessionsListFragment();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        resumeToHelp = helpFragment.isVisible();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.hide(permissionRequestFragment);
        fragmentTransaction.hide(helpFragment);
        fragmentTransaction.hide(sessionsListFragment);
        fragmentTransaction.commitNowAllowingStateLoss();
    }

    /**
     * see {@link #toggleHelp()}
     */
    public void toggleHelp(MenuItem menuItem) {
        toggleHelp();
    }

    /**
     * see {@link #toggleHelp()}
     */
    public void toggleHelp(View v) {
        toggleHelp();
    }

    /**
     * Toggles the {@link #helpFragment}. Called by the help button in the action bar or the
     * "Need help?" button in {@link SessionsListFragment}.
     */
    public void toggleHelp() {
        if (helpFragment.isVisible()) {
            showSessionsListFragment();
        } else {
            showHelpFragment();
        }
    }

    @SuppressLint("NewApi")
    public void requestPermissions(View v) {
        Log.d(TAG, "Permission is revoked (API>23)");

        if (isStoragePermissionGranted(this)) {
            showSessionsListFragment();
        } else {
            //here we are sure we are in API more than 23, no need to check again
            requestPermissions(new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    FILE_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_help) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FILE_PERMISSION_REQUEST_CODE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission: " + permissions[0] + " was granted.");
            showSessionsListFragment();
            sessionsListFragment.seekSessions();
        } else if (requestCode == FILE_PERMISSION_REQUEST_CODE
                && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Log.d(TAG, "Permission: " + permissions[0] + " was not granted. Need to ask again");
            //do nothing here, the RequestPermissionFragment is still shown
        }
    }

    /**
     * Checks if the app has the {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE} and
     * {@link android.Manifest.permission#READ_EXTERNAL_STORAGE} set to true. If the Android device
     * is of API < 23, this automatically granted, so when this return false we know that the device
     * is of API >=23.
     *
     * @return true if the user has given the app read and write to external storage permissions (or
     * device API < 23), false otherwise.
     */
    public static boolean isStoragePermissionGranted(Activity activity) {
        //credits to MetaSnarf
        // https://stackoverflow.com/questions/33162152/storage-permission-error-in-marshmallow

        if (Build.VERSION.SDK_INT >= 23) {
            if (activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
                    && activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission is granted (API>23)");
                return true;
            } else {
                Log.d(TAG, "Permission is not granted ! Should request it(API>23)");
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.d(TAG, "Permission is granted (API<23)");
            return true;
        }
    }

    /**
     * Fragment used to display the different detected {@link ImagesSession} on the Android device,
     * or a small message if there is none.
     */
    public static class SessionsListFragment extends Fragment {
        private SwipeRefreshLayout swipeRefreshLayout;
        private TextView noSessionText;
        private LinearLayout noSessionLayout;
        private ListView sessionsListView;
        private LinearLayout sessionsListLayout;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View v = inflater.inflate(R.layout.sessions_list_fragment, container, false);
            swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.session_swipe_refresh);
            noSessionText = (TextView) v.findViewById(R.id.noSessionText);
            noSessionLayout = (LinearLayout) v.findViewById(R.id.noSessionLayout);
            sessionsListView = (ListView) v.findViewById(R.id.sessionsListView);
            sessionsListLayout = (LinearLayout) v.findViewById(R.id.sessionListLayout);

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    seekSessions();
                }
            });

            return v;
        }

        @Override
        public void onResume() {
            super.onResume();
            if (isStoragePermissionGranted(getActivity())) {
                seekSessions();
            }
        }

        /**
         * Starts the task to load sessions in a background thread and changes the UI accordingly.
         * see {@link ch.epfl.mmspg.testbed360.image.ImagesSession.LoadTask#doInBackground(Activity...)}
         */
        private void seekSessions() {
            swipeRefreshLayout.setRefreshing(true);
            noSessionLayout.setVisibility(View.GONE);
            sessionsListLayout.setVisibility(View.GONE);
            sessionsListView.setAdapter(null);

            ImagesSession.LoadTask task = ImagesSession.getLoadingTask(getContext());
            task.execute(getActivity());
            try {
                sessionsListView.setAdapter(new ImagesSession.Adapter(getContext(), R.layout.session_list_item, task.get()));
                if (sessionsListView.getAdapter() != null && sessionsListView.getAdapter().getCount() == 0) {
                    noSessionText.setText(R.string.noSessionExplanation);
                    noSessionLayout.setVisibility(View.VISIBLE);
                } else {
                    sessionsListLayout.setVisibility(View.VISIBLE);
                }
            } catch (InterruptedException | ExecutionException e) {
                noSessionLayout.setVisibility(View.VISIBLE);
                noSessionText.setText(R.string.error_loading_images);
                e.printStackTrace();
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * {@link Fragment} displaying some help written in MarkDown format. Could be loaded directly from
     * the GitHub repo when public.
     */
    public static class HelpFragment extends Fragment {
        MarkdownView mdView;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View v = inflater.inflate(R.layout.help_fragment, container, false);
            mdView = (MarkdownView) v.findViewById(R.id.markdown_view);
            InternalStyleSheet css = new Github();
            css.addRule("body",
                    "font-family: \"Helvetica Neue\", Helvetica, Arial, sans-serif",
                    "font-size: 14px", "line-height: 1.42857143",
                    "color: rgba(255, 255, 255, 0.75)",
                    "background-color: #" + Integer.toHexString(getResources().getColor(R.color.bg_dark)).substring(2),
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
            return v;
        }
    }

    /**
     * {@link Fragment} displayed if we should display the {@link #sessionsListFragment} but the wanted
     * permissions were not set.
     * See {@link #isStoragePermissionGranted(Activity)}
     */
    public static class PermissionRequestFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.request_permission_fragment, container, false);
        }
    }
}
