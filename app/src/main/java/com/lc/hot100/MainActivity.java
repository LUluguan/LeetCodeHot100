package com.lc.hot100;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * 主页：按分类分组的 100 题列表。
 */
public class MainActivity extends AppCompatActivity
        implements ProblemAdapter.OnProblemClickListener {

    private ProgressBar mProgress;
    private TextView mLoadingText;
    private RecyclerView mRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mProgress = findViewById(R.id.progress);
        mLoadingText = findViewById(R.id.tv_loading);
        mRecycler = findViewById(R.id.recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setVisibility(View.GONE);

        ProblemRepository.load(this, new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onLoaded();
                    }
                });
            }
        });
    }

    private void onLoaded() {
        mProgress.setVisibility(View.GONE);
        mLoadingText.setVisibility(View.GONE);
        mRecycler.setVisibility(View.VISIBLE);

        ProblemAdapter adapter = new ProblemAdapter(this,
                ProblemRepository.get().getCategories(), this);
        mRecycler.setAdapter(adapter);
    }

    @Override
    public void onProblemClick(ProblemRepository.Problem problem) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_NUM, problem.num);
        startActivity(intent);
    }
}
