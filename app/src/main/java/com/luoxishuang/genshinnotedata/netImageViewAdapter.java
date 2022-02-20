package com.luoxishuang.genshinnotedata;

import android.view.View;
import android.widget.SimpleAdapter;

public class netImageViewAdapter implements SimpleAdapter.ViewBinder {
    @Override
    public boolean setViewValue(View view, Object data, String textRepresentation){
        if(view instanceof netImageView){
            ((netImageView) view).setImageURL(data.toString());
            return true;
        }
        return false;
    }
}
