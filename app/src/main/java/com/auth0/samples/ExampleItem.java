package com.auth0.samples;

import android.app.Activity;
import android.view.View;

public class ExampleItem extends Activity {
    private String mOrgName;
    private String mPointSummary;
    private String events;
    private String points;
    private View view1;
    private View view2;

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
