/**
 * Copyright Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.punk.com.punkapitestapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    Context mContext;
    LoadTask mLoadTask;
    SaveTask mSaveTask;

    @SuppressLint("StaticFieldLeak")
    private class SaveTask extends AsyncTask<ArrayList<BeerItem>, Void, ArrayList<BeerItem>> {

        @SafeVarargs
        @Override
        protected final ArrayList<BeerItem> doInBackground(ArrayList<BeerItem>... lists) {
            ArrayList<BeerItem> list = lists[0];
            CodelabUtil.addBeerItemsToFile(mContext, list);
            return null;
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class LoadTask extends AsyncTask<Void, Void, List<BeerItem>> {

        @Override
        protected List<BeerItem> doInBackground(Void... voids) {
            return CodelabUtil.getBeerItemsFromFile(mContext);
        }

        @Override
        protected void onPostExecute(List<BeerItem> beerItems) {

            mBeerAdapter = new BeerAdapter(mContext, beerItems, imageRequester);
       //     mBeerAdapter.setBeerItems(beerItems);
      //      mBeerAdapter.notifyDataSetChanged();
            mRecyclerView.setAdapter(mBeerAdapter);

            if (mProgressBar.getVisibility() == View.VISIBLE) {
                showProgress(false);
            }

        }
    }


    private void addTask(String type) {

        if (type.equals(CodelabUtil.ONEOFF_TASK)) {

            OneoffTask oneoffTask = new OneoffTask.Builder()
                    .setService(BestTimeService.class)
                    .setTag(TAG)
                    .setRequiredNetwork(OneoffTask.NETWORK_STATE_CONNECTED)
                    .setExecutionWindow(0, 30)
                    .build();
            mGcmNetworkManager.schedule(oneoffTask);
        } else {

            Intent nowIntent = new Intent(mContext, NowIntentService.class);
            mContext.startService(nowIntent);
        }

    }

    private static final String TAG = "MainActivity";
    private LocalBroadcastManager mLocalBroadcastManager;
    private BroadcastReceiver mBroadcastReceiver;
    private int page;
    private BeerAdapter mBeerAdapter;
    private RecyclerView mRecyclerView;
    ProgressBar mProgressBar;
    CoordinatorLayout mCoordinatorLayout;
    ImageRequester imageRequester;

    private GcmNetworkManager mGcmNetworkManager;

    Snackbar mSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        mProgressBar = findViewById(R.id.progressBar);
        page = CodelabUtil.getIntPreference(CodelabUtil.PAGE, mContext, 1);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        BottomNavigationView bottomNavigation =
                (BottomNavigationView) findViewById(R.id.bottom_navigation);

        bottomNavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        int newValue;
                        if(item.getItemId() == R.id.favorites){
                            newValue = 1;

                        } else {
                            newValue = 0;
                                                    }
                        CodelabUtil.saveIntPreference(CodelabUtil.FAV, newValue, mContext);

                      reloadrecyclerView();

                       return true;
                    }
                });


        imageRequester = ImageRequester.getInstance(this);


        mGcmNetworkManager = GcmNetworkManager.getInstance(this);

        mCoordinatorLayout = findViewById(R.id.mainContent);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(
                new GridLayoutManager(this, getResources().getInteger(R.integer.shr_column_count)));

        initBroadcastReceiver();

    }

    private void reloadrecyclerView() {

        mLoadTask = new LoadTask();
        mLoadTask.execute();

    }

    private void initBroadcastReceiver() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String status = intent.getStringExtra(CodelabUtil.TASK_STATUS);

                if (status.equals(CodelabUtil.FAILED_STATUS) && page != 0) {

                    mSnackbar = Snackbar.make(mRecyclerView, "No internet it seems", Snackbar.LENGTH_INDEFINITE);
                    mSnackbar.setAction("Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            mSnackbar.dismiss();
                            mSnackbar = Snackbar.make(mCoordinatorLayout, "We gonna load data as soon as possible", Snackbar.LENGTH_INDEFINITE);
                            mSnackbar.show();

                            addTask(CodelabUtil.ONEOFF_TASK);
                        }
                    });
                    mSnackbar.show();


                } else if (status.equals(CodelabUtil.EXECUTED_STATUS) && page != 0) {
                    if (mSnackbar != null && mSnackbar.isShown()) {
                        mSnackbar.dismiss();
                    }


                    loadData();

                } else if (status.equals(CodelabUtil.EXECUTED_STATUS) && page == 0) {
                    mBeerAdapter.refresh();
                }

                //         mBeerAdapter.updateTaskItemStatus(taskId, status);
            }
        };
    }

    private void loadData() {

        if (mProgressBar.getVisibility() == View.GONE) {
            showProgress(true);
        }


        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        String url = "https://api.punkapi.com/v2/beers?page=" + page;
        final ArrayList<BeerItem> beerItems = new ArrayList<>();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {


            @Override
            public void onResponse(JSONArray response) {


                try {
                    if (response.length() > 0) {
                        String id = "", name = "", url = "";
                        double ibu = 0, ebc = 0, abv = 0;

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);

                            if (!jsonObject.isNull("name")) {
                                name = jsonObject.getString("name");
                            }
                            if (!jsonObject.isNull("image_url")) {
                                url = jsonObject.getString("image_url");
                            }
                            if (!jsonObject.isNull("id")) {
                                id = jsonObject.getString("id");
                            }
                            if (!jsonObject.isNull("ibu")) {
                                ibu = jsonObject.getDouble("ibu");
                            }
                            if (!jsonObject.isNull("ebc")) {
                                ebc = jsonObject.getDouble("ebc");
                            }
                            if (!jsonObject.isNull("abv")) {
                                abv = jsonObject.getDouble("abv");
                            }
                            beerItems.add(new BeerItem(id, name, url, abv, ibu, ebc, false));
                        }

                        mSaveTask = new SaveTask();
                        mSaveTask.execute(beerItems);


                        savePage(page += 1);
                        loadData();
                    } else {
                        savePage(0);
                        mLoadTask = new LoadTask();
                        mLoadTask.execute();

                    }

                    //       Log.d(TAG, "onResponse: " + response.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        requestQueue.add(jsonArrayRequest);


    }

    private void savePage(int i) {
        page = i;
        CodelabUtil.saveIntPreference(CodelabUtil.PAGE, i, mContext);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mLoadTask != null && mLoadTask.getStatus() == AsyncTask.Status.RUNNING) {
            mLoadTask.cancel(true);
        }

        if (mSaveTask != null && mSaveTask.getStatus() == AsyncTask.Status.RUNNING) {
            mSaveTask.cancel(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLoadTask = new LoadTask();
        mLoadTask.execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, new IntentFilter(CodelabUtil.TASK_UPDATE_FILTER));
        addTask(CodelabUtil.NOW_TASK);
    }

    @Override
    public void onPause() {
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    private void showProgress(final boolean show) {

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        mRecyclerView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressBar.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case R.id.action_sort:
                showSortOrderSetDialog();
                break;
        }

        return false;
    }

    int newFilter;
    int newOrder;
    private AlertDialog alert;

    private void showSortOrderSetDialog() {

        LayoutInflater inflater1 = getLayoutInflater();
        ViewGroup parent = findViewById(R.id.recyclerView);
        final View dialogView1 = inflater1.inflate(R.layout.filter_menu, parent, false);

        newFilter = CodelabUtil.getIntPreference(CodelabUtil.FILTER, mContext);
        newOrder = CodelabUtil.getIntPreference(CodelabUtil.ORDER, mContext);

        String[] mTestArray = getResources().getStringArray(R.array.sortArray);

        Spinner spinner = dialogView1.findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner, mTestArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(newFilter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                newFilter = position;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                newFilter = 0;
            }
        });

        String[] mTestArray1 = getResources().getStringArray(R.array.orderArray);
        Spinner spinner1 = (Spinner) dialogView1.findViewById(R.id.spinner1);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, R.layout.spinner, mTestArray1);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);
        spinner1.setSelection(newOrder);

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                newOrder = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                newOrder = 0;
            }
        });


        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        String dialogTitle = "Set Sort Order";
        String dialog_positive_button = "Set";
        dialog.setTitle(dialogTitle)
                .setPositiveButton(dialog_positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        CodelabUtil.saveIntPreference(CodelabUtil.FILTER, newFilter, mContext);
                        CodelabUtil.saveIntPreference(CodelabUtil.ORDER, newOrder, mContext);
                        reloadrecyclerView();

                    }
                })
                .setNegativeButton("Cancel", null)
                .setView(dialogView1);

        alert = dialog.create();
        alert.show();
    }
}
