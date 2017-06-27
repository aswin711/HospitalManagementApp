package com.synnefx.cqms.event.sync.medicationerror;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class MedicationErrorSyncContentProvider extends ContentProvider {
    public static String AUTHORITY = "com.synnefx.cqms.event.sync.incident.medicationerrorsynccontentprovider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/medicationerror");

    @Override
    public boolean onCreate() {
        //TODO Auto-generated method stub
        return false;
    }

    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {
        return null;
    }


    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

}
