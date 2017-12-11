package com.team.killskills.nukvoy_android;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.team.killskills.nukvoy_android.dto.AirportDto;
import com.team.killskills.nukvoy_android.handlers.DBClient;
import com.team.killskills.nukvoy_android.model.Airport;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AirportAdapter.ClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private RestClient restClient;
    private DBClient dbClient;
    private RecyclerView rvCountry;
    private AirportAdapter adapter;
    private ArrayList<Airport> airportList = new ArrayList<>();

    private SearchView svSearch;
    private ProgressBar pbLoading;
    private LinearLayout llNoData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        init();
        initViews();

        new pullCountryTask().execute();
    }

    @Override
    protected void onStart() {
        super.onStart();
        svSearch.setFocusable(false);
        hideSoftKeyboard();
    }

    private void init() {
        restClient = new RestClient(this);
        dbClient = new DBClient(this);
    }

    private void initViews() {
        svSearch = findViewById(R.id.svSearch);
        pbLoading = findViewById(R.id.pbLoading);
        llNoData = findViewById(R.id.llNoData);
        rvCountry = findViewById(R.id.rvCountry);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvCountry.setLayoutManager(mLayoutManager);
        rvCountry.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.space));
        rvCountry.addItemDecoration(dividerItemDecoration);

        adapter = new AirportAdapter(this, airportList);
        rvCountry.setAdapter(adapter);
    }

    private void configureSearchView() {
        svSearch.setVisibility(View.VISIBLE);
        svSearch.setQueryHint(getString(R.string.search_here));

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            svSearch.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                adapter.getFilter().filter(query);
                return true;
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //showVoiceSearchResult(query);
        }
    }

    private void showVoiceSearchResult(String query) {
        Intent intent = new Intent(this, VoiceSearchResultActivity.class);
        intent.putExtra("list", airportList);
        intent.putExtra("query", query);
        startActivity(intent);
    }

    @Override
    public void onClick(Airport airport) {
        Intent intent = new Intent(this, AirportDetailsActivity.class);
        intent.putExtra("airport", airport);
        startActivity(intent);
    }

    public void onTryAgainClicked(View view) {
        new pullCountryTask().execute();
    }

    class pullCountryTask extends AsyncTask<Void, Void, Void> {

        private void showProgress() {
            pbLoading.setVisibility(View.VISIBLE);
            rvCountry.setVisibility(View.GONE);
            llNoData.setVisibility(View.GONE);
        }

        private void showNoData() {
            pbLoading.setVisibility(View.GONE);
            rvCountry.setVisibility(View.GONE);
            llNoData.setVisibility(View.VISIBLE);
        }

        private void showData() {
            pbLoading.setVisibility(View.GONE);
            llNoData.setVisibility(View.GONE);
            rvCountry.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (restClient.isOnline()) {
                List<AirportDto> countryDtoList = (List<AirportDto>) restClient.get(BuildConfig.url, AirportDto.class, Boolean.TRUE);
                if(countryDtoList!=null) {
                    boolean isInserted = dbClient.insertAirport(countryDtoList);
                    Logger.logInfo(TAG, "isInserted: " + isInserted);
                }
            }
            airportList.clear();
            airportList.addAll(dbClient.getAirportList());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.notifyDataSetChanged();

            if (airportList.isEmpty()) {
                showNoData();
            } else {
                showData();
                configureSearchView();
            }
        }
    }

    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        restClient = null;
        dbClient = null;
    }
}
