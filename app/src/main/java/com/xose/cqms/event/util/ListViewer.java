package com.xose.cqms.event.util;

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
}
