package com.auth0.samples;

public class ExamplePoint {
    private String mPointCategory;
    private String mCategoryTotalPoints;

    public ExamplePoint(String pointCategory, String categoryTotalPoints) {
        mPointCategory = pointCategory;
        mCategoryTotalPoints = categoryTotalPoints;
    }

    public String getPointCategory(){
        return mPointCategory;
    }

    public String getCategoryTotalPoints(){
        return mCategoryTotalPoints;
    }
}
