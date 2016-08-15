package org.pandawarrior.popularmovie.ui.main;

import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.pandawarrior.popularmovie.ApiService;
import org.pandawarrior.popularmovie.R;
import org.pandawarrior.popularmovie.data.Movie;
import org.pandawarrior.popularmovie.data.MovieResponse;
import org.pandawarrior.popularmovie.databinding.ActivityMainBinding;
import org.pandawarrior.popularmovie.utils.ApiUtils;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    public static final String ACTIVITY_MOVIE_LIST = "mainActivityMovieList";
    private static final String ACTIVITY_FILTER_TYPE = "mainActivityFilterType";

    ApiService mApiService;
    MainActivityRecyclerAdapter mainActivityRecyclerAdapter;

    ActivityMainBinding mainBinding;
    ArrayList<Movie> movieArrayList;

    @ApiUtils.ApiFilter
    int mApiFilterType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mApiService = ApiUtils.initApiService();
        initActionBar();
        initRecyclerview();
        initData(savedInstanceState);
    }

    private void initRecyclerview() {
        mainActivityRecyclerAdapter = new MainActivityRecyclerAdapter(null);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mainBinding.activityMainRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        } else {
            mainBinding.activityMainRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        }
        mainActivityRecyclerAdapter.setClickHandler(new MovieClickHandler<Movie>() {
            @Override
            public void onItemClick(Movie movie) {
                Intent intent = new Intent(MainActivity.this, MovieDetailActivity.class);
                intent.putExtra(MovieDetailActivity.MOVIE_INTENT, movie);
                startActivity(intent);
            }
        });
        mainBinding.activityMainRecyclerView.setAdapter(mainActivityRecyclerAdapter);
    }

    @SuppressWarnings("ResourceType")
    private void initData(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            movieArrayList = savedInstanceState.getParcelableArrayList(ACTIVITY_MOVIE_LIST);
            mApiFilterType = savedInstanceState.getInt(ACTIVITY_FILTER_TYPE);
            mainActivityRecyclerAdapter.setMovieList(movieArrayList);
        } else {
            mApiFilterType = ApiUtils.API_POPULAR;
            loadData(1, true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_sort_popular:
                mApiFilterType = ApiUtils.API_POPULAR;
                break;
            case R.id.menu_sort_top:
                mApiFilterType = ApiUtils.API_TOP_RATED;
                break;
        }
        loadData(1, true);
        return super.onOptionsItemSelected(item);
    }

    private void loadData(int page, final boolean isStart) {
        Call<MovieResponse> call;
        if (mApiFilterType == ApiUtils.API_POPULAR) {
            call = mApiService.getPopularMovie();
        } else if (mApiFilterType == ApiUtils.API_TOP_RATED) {
            call = mApiService.getTopRatedMovie();
        } else {
            return;
        }
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful()) {
                    movieArrayList = (ArrayList<Movie>) response.body().getData();
                    if (isStart) {
                        mainActivityRecyclerAdapter.setMovieList(movieArrayList);
                    } else {
                        mainActivityRecyclerAdapter.addMovieList(movieArrayList);
                    }
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();


            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(ACTIVITY_MOVIE_LIST, movieArrayList);
        outState.putInt(ACTIVITY_FILTER_TYPE, mApiFilterType);
        super.onSaveInstanceState(outState);
    }

    private void initActionBar() {
        setSupportActionBar(mainBinding.activityMainToolbar.toolbar);
    }
}