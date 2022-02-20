package com.luoxishuang.genshinnotedata;

import java.util.Observable;

public abstract class requestOBAB extends Observable {
    public void setData(Object data){
        setChanged();
        this.notifyObservers(data);
    }
}
