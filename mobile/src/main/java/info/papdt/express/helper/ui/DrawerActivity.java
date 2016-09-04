package info.papdt.express.helper.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v13.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import info.papdt.express.helper.R;
import info.papdt.express.helper.dao.PackageDatabase;
import info.papdt.express.helper.model.Package;
import info.papdt.express.helper.ui.fragment.home.BaseFragmentNew;
import info.papdt.express.helper.ui.fragment.home.FragmentAllNew;
import info.papdt.express.helper.ui.launcher.AppWidgetProvider;


public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    public static final int REQUEST_ADD = 10001, RESULT_NEW_PACKAGE = 2000, REQUEST_DETAILS = 10002, RESULT_DELETED = 2001, RESULT_RENAMED = 2002;

    public static final int MSG_NOTIFY_DATA_CHANGED = 1, MSG_NOTIFY_ITEM_REMOVE = 2;
    private PackageDatabase mDatabase;
    private BaseFragmentNew[] fragments;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        verifyStoragePermissions(DrawerActivity.this);

        mDatabase = PackageDatabase.getInstance(getApplicationContext());
        fragments = new BaseFragmentNew[]{
                FragmentAllNew.newInstance(mDatabase, FragmentAllNew.TYPE_ALL),
                FragmentAllNew.newInstance(mDatabase, FragmentAllNew.TYPE_DELIVERED),
                FragmentAllNew.newInstance(mDatabase, FragmentAllNew.TYPE_DELIVERING)
        };

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DrawerActivity.this, AddActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(intent, REQUEST_ADD);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDatabase.save();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return /*PlaceholderFragment.newInstance(position);*/
                    fragments[position];
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.navigation_item_all);
                case 1:
                    return getString(R.string.navigation_item_delivered);
                case 2:
                    return getString(R.string.navigation_item_on_the_way);
            }
            return null;
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        menu.findItem(R.id.action_search).getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            SettingsActivity.launch(this, SettingsActivity.FLAG_MAIN);
            return true;
        } else if (id == R.id.action_read_all) {
            new ReadAllTask().execute();
            return true;
        } else if (id == R.id.action_search) {
            View menuButton = findViewById(id);
            int[] location = new int[2];
            menuButton.getLocationOnScreen(location);
            SearchActivity.launch(this, location[0] + menuButton.getHeight() / 2, location[1] + menuButton.getWidth() / 2);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("Main", "result received, requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (requestCode == REQUEST_ADD) {
            if (resultCode == RESULT_NEW_PACKAGE) {
                String jsonData = data.getStringExtra(AddActivity.RESULT_EXTRA_PACKAGE_JSON);
                Package p = Package.buildFromJson(jsonData);
                if (p != null) {
                    Log.i("Main", p.toJsonString());
                    mDatabase.add(p);
                    this.notifyDataChanged(-1);
                }
            }
        }
        if (requestCode == REQUEST_DETAILS) {
            switch (resultCode) {
                case RESULT_RENAMED:
                    notifyDataChanged(-1);
                    break;
                case RESULT_DELETED:
                    notifyDataChanged(-1);
//                    final int fragId = mBottomBar.getCurrentTabPosition();
//                    Snackbar.make(
//                            $(R.id.coordinator_layout),
//                            String.format(getString(R.string.toast_item_removed), data.getStringExtra("title")),
//                            Snackbar.LENGTH_LONG
//                    )
//                            .setAction(R.string.toast_item_removed_action, new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    fragments[fragId].onUndoActionClicked();
//                                }
//                            })
//                            .show();
                    break;
            }
        }
    }

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, DrawerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NOTIFY_DATA_CHANGED:
                    AppWidgetProvider.updateManually(getApplication());
                    for (int i = 0; i < fragments.length; i++) {
                        if (i == msg.arg1) continue; // Skip the fragment which sent message.
                        fragments[i].notifyDataSetChanged();
                    }
                    break;
                case MSG_NOTIFY_ITEM_REMOVE:
//                    Snackbar.make(
//                            $(R.id.coordinator_layout),
//                            String.format(getString(R.string.toast_item_removed), msg.getData().getString("title")),
//                            Snackbar.LENGTH_LONG
//                    )
//                            .setAction(R.string.toast_item_removed_action, new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    fragments[mBottomBar.getCurrentTabPosition()].onUndoActionClicked();
//                                }
//                            })
//                            .show();
                    break;
            }
        }
    };

    public void notifyDataChanged(int fromFragId) {
        Message msg = new Message();
        msg.what = MSG_NOTIFY_DATA_CHANGED;
        msg.arg1 = fromFragId;
        mHandler.sendMessage(msg);
    }

    private class ReadAllTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            int count = 0;
            for (int i = 0; i < mDatabase.size(); i++) {
                if (mDatabase.get(i).unreadNew) {
                    count++;
                    mDatabase.get(i).unreadNew = false;
                }
            }
            mDatabase.save();
            return count;
        }

        @Override
        protected void onPostExecute(Integer count) {
            notifyDataChanged(-1);
//            Snackbar.make(
//                    $(R.id.coordinator_layout),
//                    getString(R.string.toast_all_read, count),
//                    Snackbar.LENGTH_LONG
//            ).show();
        }

    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }
}
