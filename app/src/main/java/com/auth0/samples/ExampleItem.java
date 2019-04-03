package com.auth0.samples;

import android.app.Activity;

public class ExampleItem extends Activity {
    private String mOrgName;
    private String mPointSummary;

    public ExampleItem(String orgName, String pointSummary){
        mOrgName = orgName;
        mPointSummary = pointSummary;
    }

    public String getOrgName(){
        return mOrgName;
    }

    public String getPointSummary(){
        return mPointSummary;
    }

}
