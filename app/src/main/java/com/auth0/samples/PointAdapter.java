package com.auth0.samples;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class PointAdapter extends RecyclerView.Adapter<PointAdapter.ExampleViewHolder> {
    private ArrayList<ExamplePoint> mPointList;

    public static class ExampleViewHolder extends RecyclerView.ViewHolder {
        public TextView mPointCategory;
        public TextView mCategoryTotalPoints;

        public ExampleViewHolder(View itemView) {
            super(itemView);
            mPointCategory = itemView.findViewById(R.id.pointName);
            mCategoryTotalPoints = itemView.findViewById(R.id.pointTotals);
        }
    }

    public PointAdapter(ArrayList<ExamplePoint> pointList){
        mPointList = pointList;
    }

    @NonNull
    @Override
    public ExampleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View w = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.point_recycler, viewGroup, false);
        ExampleViewHolder evh1 = new ExampleViewHolder(w);
        return evh1;
    }

    @Override
    public void onBindViewHolder(@NonNull ExampleViewHolder exampleViewHolder, int i) {
        ExamplePoint currentCategory = mPointList.get(i);

        exampleViewHolder.mPointCategory.setText(currentCategory.getPointCategory());
        exampleViewHolder.mCategoryTotalPoints.setText(currentCategory.getCategoryTotalPoints());

    }

    @Override
    public int getItemCount() {
        return mPointList.size();
    }
}
