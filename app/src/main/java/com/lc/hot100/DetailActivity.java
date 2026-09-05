package com.lc.hot100;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 题目详情页：题目描述 + 隐藏的答案代码（点击显示，支持 C / Python3 / Java 切换）+ 思路解析。
 */
public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_NUM = "extra_num";

    private Button mBtnShowAnswer;
    private View mTabRow;
    private HorizontalScrollView mScrollCode;
    private TextView mTvCode;
    private final TextView[] mTabs = new TextView[3];
    private final String[] mCodes = new String[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        int num = getIntent().getIntExtra(EXTRA_NUM, -1);
        ProblemRepository repo = ProblemRepository.get();
        ProblemRepository.Problem p = repo == null ? null : repo.findByNum(num);
        if (p == null) {
            finish();
            return;
        }

        setTitle(p.num + ". " + p.title);

        TextView tvTitle = findViewById(R.id.tv_title);
        TextView tvDifficulty = findViewById(R.id.tv_difficulty);
        TextView tvCategory = findViewById(R.id.tv_category);
        TextView tvDesc = findViewById(R.id.tv_desc);
        TextView tvExplain = findViewById(R.id.tv_explain);
        mBtnShowAnswer = findViewById(R.id.btn_show_answer);
        mTabRow = findViewById(R.id.tab_row);
        mScrollCode = findViewById(R.id.scroll_code);
        mTvCode = findViewById(R.id.tv_code);
        mTabs[0] = findViewById(R.id.tv_tab_c);
        mTabs[1] = findViewById(R.id.tv_tab_py);
        mTabs[2] = findViewById(R.id.tv_tab_java);

        // 三种语言的答案代码
        mCodes[0] = p.codeC;
        mCodes[1] = p.codePy;
        mCodes[2] = p.codeJava;

        tvTitle.setText(p.num + ". " + p.title);
        tvDifficulty.setText(p.difficulty);
        tvDifficulty.setTextColor(ProblemRepository.difficultyColor(p.difficulty));
        tvDifficulty.setBackgroundColor(ProblemRepository.difficultyBg(p.difficulty));
        tvCategory.setText(p.category);
        tvDesc.setText(p.desc);
        tvExplain.setText(p.explain);

        // 答案默认隐藏，点击按钮后显示（默认语言为 Java）
        mBtnShowAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnShowAnswer.setVisibility(View.GONE);
                mTabRow.setVisibility(View.VISIBLE);
                mScrollCode.setVisibility(View.VISIBLE);
                selectLanguage(2);
            }
        });
        for (int i = 0; i < mTabs.length; i++) {
            final int lang = i;
            mTabs[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectLanguage(lang);
                }
            });
        }
    }

    /** 切换语言：0=C，1=Python3，2=Java */
    private void selectLanguage(int lang) {
        mTvCode.setText(mCodes[lang]);
        for (int i = 0; i < mTabs.length; i++) {
            if (i == lang) {
                mTabs[i].setBackgroundResource(R.color.lc_orange);
                mTabs[i].setTextColor(getColor(R.color.white));
            } else {
                mTabs[i].setBackgroundResource(R.color.chip_bg);
                mTabs[i].setTextColor(getColor(R.color.text_sub));
            }
        }
    }
}
