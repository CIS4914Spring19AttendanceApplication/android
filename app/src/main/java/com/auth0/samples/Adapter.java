package com.auth0.samples;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.ExampleViewHolder> {
    private ArrayList<ExampleItem> mExampleList;
    private int mExpandedPosition = -1;

    public static class ExampleViewHolder extends RecyclerView.ViewHolder{
        public TextView mOrgName;
        public TextView mPointSummary;
        public TextView mEvents;
        public TextView mPoints;
        public View mView;
        public View mView2;
        public RecyclerView mRecycler;
        public RecyclerView mRecylcer2;

        public ExampleViewHolder(@NonNull View itemView) {
            super(itemView);
            mOrgName = itemView.findViewById(R.id.orgName);
            mPointSummary = itemView.findViewById(R.id.orgPoints);
            mEvents = itemView.findViewById(R.id.events);
            mPoints = itemView.findViewById(R.id.points);
            mView = itemView.findViewById(R.id.view1);
            mView2 = itemView.findViewById(R.id.view3);
            mRecycler = itemView.findViewById(R.id.rv_events);
            mRecylcer2 = itemView.findViewById(R.id.rv_points);


        }
    }

    public Adapter(ArrayList<ExampleItem> exampleList){
        mExampleList = exampleList;
    }

    @NonNull
    @Override
    public ExampleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_row, parent, false);
        ExampleViewHolder evh = new ExampleViewHolder(v);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull ExampleViewHolder viewHolder, int i) {
        ExampleItem currentItem = mExampleList.get(i);

        viewHolder.mOrgName.setText(currentItem.getOrgName());
        viewHolder.mPointSummary.setText(currentItem.getPointSummary());


        final int position = i;

        final boolean isExpanded = position==mExpandedPosition;


        viewHolder.mEvents.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        viewHolder.mPoints.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        viewHolder.mView.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        viewHolder.mView2.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        viewHolder.mRecycler.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        viewHolder.mRecylcer2.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        viewHolder.itemView.setActivated(isExpanded);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = isExpanded ? -1:position;
                notifyItemChanged(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mExampleList.size();
    }
}
