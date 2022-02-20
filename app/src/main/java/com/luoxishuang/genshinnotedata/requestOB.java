package com.luoxishuang.genshinnotedata;

import org.json.JSONException;

import java.util.Observer;
import java.util.Observable;

public abstract class requestOB implements Observer {
    @Override
    public void update(Observable o, Object data){
        try {
            onDataChanged(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public abstract void onDataChanged(Object data) throws JSONException;
}
