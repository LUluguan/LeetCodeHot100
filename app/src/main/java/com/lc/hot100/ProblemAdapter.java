package com.lc.hot100;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 列表适配器：分类标题行（String）+ 题目行（Problem），两种视图类型。
 */
public class ProblemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnProblemClickListener {
        void onProblemClick(ProblemRepository.Problem problem);
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_PROBLEM = 1;

    private final OnProblemClickListener mListener;
    private final List<Object> mRows = new ArrayList<>();

    public ProblemAdapter(Context context, List<ProblemRepository.Category> categories,
                          OnProblemClickListener listener) {
        mListener = listener;
        for (ProblemRepository.Category c : categories) {
            mRows.add(c.name); // String 表示分类头
            mRows.addAll(c.problems);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mRows.get(position) instanceof String ? TYPE_HEADER : TYPE_PROBLEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderHolder(inflater.inflate(R.layout.item_header, parent, false));
        }
        return new ProblemHolder(inflater.inflate(R.layout.item_problem, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object row = mRows.get(position);
        if (holder instanceof HeaderHolder) {
            String name = (String) row;
            int count = 0;
            // 向后统计属于该分类的题目数量
            for (int i = position + 1; i < mRows.size()
                    && !(mRows.get(i) instanceof String); i++) {
                count++;
            }
            ((HeaderHolder) holder).bind(name, count);
        } else {
            ((ProblemHolder) holder).bind((ProblemRepository.Problem) row, mListener);
        }
    }

    @Override
    public int getItemCount() {
        return mRows.size();
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        final TextView tvCategory;
        final TextView tvCount;

        HeaderHolder(View v) {
            super(v);
            tvCategory = v.findViewById(R.id.tv_category);
            tvCount = v.findViewById(R.id.tv_count);
        }

        void bind(String name, int count) {
            tvCategory.setText(name);
            tvCount.setText("共 " + count + " 题");
        }
    }

    static class ProblemHolder extends RecyclerView.ViewHolder {
        final TextView tvNum;
        final TextView tvTitle;
        final TextView tvDifficulty;

        ProblemHolder(View v) {
            super(v);
            tvNum = v.findViewById(R.id.tv_num);
            tvTitle = v.findViewById(R.id.tv_title);
            tvDifficulty = v.findViewById(R.id.tv_difficulty);
        }

        void bind(final ProblemRepository.Problem p, final OnProblemClickListener listener) {
            tvNum.setText(String.valueOf(p.num));
            tvTitle.setText(p.title);
            tvDifficulty.setText(p.difficulty);
            tvDifficulty.setTextColor(ProblemRepository.difficultyColor(p.difficulty));
            tvDifficulty.setBackgroundColor(ProblemRepository.difficultyBg(p.difficulty));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onProblemClick(p);
                    }
                }
            });
        }
    }
}
