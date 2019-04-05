package com.auth0.samples;

public class ExampleEvent {

    private String mEventName;
    private String mEventDescription;

    public ExampleEvent(String eventName, String eventDescription) {
        mEventName = eventName;
        mEventDescription = eventDescription;
    }

    public String getEventName(){
        return mEventName + "\n";
    }

    public String getEventDescription(){
        return mEventDescription;
    }
}

