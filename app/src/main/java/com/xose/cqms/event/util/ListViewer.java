package com.xose.cqms.event.util;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by cedex on 6/12/2017.
 */

public class ListViewer {

    public static <T> String view(List<T> list){
        String listString = "";
        for (T t:list){
            listString += t.toString()+"\n";
        }
        return listString;
    }

    public static <T> String view(T t){
        Gson gson = new Gson();
        return gson.toJson(t);
    }
}
