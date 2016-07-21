package edu.cascadia.bookmarked;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import static android.R.color.holo_green_light;

public class MyPostingActivity extends AppCompatActivity implements BookListFragment.OnFragmentInteractionListener {

    private final String TAG = "MyPostingActivity";

    private final int EDIT_REQUEST_CODE = 2;
    private String userID;

    BookListFragment sellItemFragment;
    BookListFragment wantedItemFragment;

    private boolean needsUpdating = false;
    private String lastSelectedPostType = "";

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userID = getIntent().getStringExtra(getString(R.string.user_id_param));

        insertBookListFragments();

        // setup action to return to previous screen
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(null);

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        sendScreenImageName();

    }

    private void insertBookListFragments() {

        // insert book for sale list view
        if (sellItemFragment == null) {
            sellItemFragment = BookListFragment.newInstance("my-sell-list", userID);
        }
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, sellItemFragment);
        fragmentTransaction.commit();

        // insert book wanted list view
        if (wantedItemFragment == null ) {
            wantedItemFragment = BookListFragment.newInstance("my-buy-list", userID);
        }

        FragmentManager fm2 = getFragmentManager();
        FragmentTransaction fragmentTransaction2 = fm.beginTransaction();
        fragmentTransaction2.replace(R.id.fragment_container2, wantedItemFragment);
        fragmentTransaction2.commit();
    }

    @Override
    public void onMyPostingBookClicked(BookItem bookItem, String listType) {
        Intent bookIntent;

        lastSelectedPostType = listType;
        if (listType.contains("sell")) {
            bookIntent = new Intent(this, BookDetailActivity.class);
        } else {
            // wanted book
            bookIntent = new Intent(this, BookWantedActivity.class);
        }
        // pass the book for sale information to detail activity
        bookIntent.putExtra(getString(R.string.book_info_param), bookItem.jsonString);
        bookIntent.putExtra(getString(R.string.book_action_param), "AllowEdit");
        bookIntent.putExtra(getString(R.string.user_id_param), userID);

        startActivityForResult(bookIntent, EDIT_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == EDIT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (lastSelectedPostType.contains("sell")) {
                    sellItemFragment.refreshList();
                } else {
                    wantedItemFragment.refreshList();
                }

                // there were changes, so let the
                // previous activity know
                needsUpdating = true;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void finish() {
        if (needsUpdating) {
            Intent data = new Intent();
            data.putExtra("NewPosting", needsUpdating);

            setResult(RESULT_OK, data);
        }
        super.finish();
    }

    private void sendScreenImageName() {
        Log.i(TAG, "Setting screen name:" + getTitle());
        System.out.println("Setting screen name:" + getTitle());
        mTracker.setScreenName("Screen:" + getTitle());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

}
