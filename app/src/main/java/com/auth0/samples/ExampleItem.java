package com.auth0.samples;

import android.app.Activity;


public class ExampleItem extends Activity {
    private String mOrgName;
    private String mPointSummary;
    private Boolean mOrgComplete;


    public ExampleItem(String orgName, String pointSummary, Boolean orgComplete){
        mOrgName = orgName;
        mPointSummary = pointSummary;
        mOrgComplete = orgComplete;
    }

    public String getOrgName(){
        return mOrgName;
    }

    public String getPointSummary(){
        return mPointSummary;
    }

    public Boolean getOrgComplete() {
        return mOrgComplete;
    }
}
