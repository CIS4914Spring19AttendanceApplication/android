package com.auth0.samples;

public class ExamplePoint {
    private String mPointCategory;
    private String mCategoryTotalPoints;
    private Boolean mIsCompelte;

    public ExamplePoint(String pointCategory, String categoryTotalPoints, boolean isComplete) {
        mPointCategory = pointCategory;
        mCategoryTotalPoints = categoryTotalPoints;
        mIsCompelte = isComplete;
    }

    public String getPointCategory(){
        return mPointCategory;
    }

    public String getCategoryTotalPoints(){
        return mCategoryTotalPoints;
    }

    public Boolean getIsComplete(){
        return mIsCompelte;
    }
}
