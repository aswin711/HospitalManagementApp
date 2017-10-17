package com.synnefx.cqms.event.sync;

import android.util.Log;

import com.synnefx.cqms.event.util.ListViewer;

import java.util.List;

/***
 * Manages 2-way synchronization of local and remote datastores.
 *
 * @param <L> - Local Syncable data type
 * @param <R> - Remote Syncable data type
 * @author SAS
 */
public class SyncManager<L extends Syncable, R extends Syncable> {
    private static final String TAG = "SyncManager";

    private Datastore<L> mLocalStore;
    private Datastore<R> mRemoteStore;


    private L findRemoteItemInLocalData(R remoteItem, List<L> localData) {
        L result = null;
        if (null != localData && 0 < localData.size()) {
            for (L localItem : localData) {
                Long localItemRemoteId = localItem.getRemoteId();
                if (localItemRemoteId != null) {
                    if (localItemRemoteId.equals(remoteItem.getRemoteId())) {
                        result = localItem;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private R findLocalItemInRemoteData(L localItem, List<R> remoteData) {
        R result = null;
        if (null != remoteData && 0 < remoteData.size()) {
            for (R remoteItem : remoteData) {
                //Log.d(TAG, serverItem.toString()+" =? "+localItem.toString());
                Long remoteItemRemoteId = remoteItem.getRemoteId();
                if (remoteItemRemoteId != null) {
                    if (remoteItemRemoteId.equals(localItem.getRemoteId())) {
                        result = remoteItem;
                        break;
                    }
                }
            }
        }
        return result;
    }


    private void syncItem(L localItem, R remoteItem) {
        Log.d(TAG, "syncItem...");
        Log.d(TAG, "local seq:" + localItem.getLastUpdatedSequence() + " remote seq:" + remoteItem.getLastUpdatedSequence());
        if (remoteItem.getLastUpdatedSequence() > localItem.getLastUpdatedSequence()) {
            // update local data
            localItem.mapFromRemote(remoteItem);
            localItem.setLastUpdatedSequence(remoteItem.getLastUpdatedSequence());
            localItem = mLocalStore.update(localItem);
        } else if (remoteItem.getLastUpdatedSequence() < localItem.getLastUpdatedSequence()) {
            // update remote data
            remoteItem.mapFromLocal(localItem);
            remoteItem.setLastUpdatedSequence(localItem.getLastUpdatedSequence());
            remoteItem = mRemoteStore.update(remoteItem);
        } else {
            Log.d(TAG, "syncItem...no update to apply...synced already.");
        }

    }

    private void pull() {
        Log.d(TAG, "pull...");
        List<R> remoteData = mRemoteStore.get();
        if (null != remoteData && 0 < remoteData.size()) {
            List<L> localData = mLocalStore.get();
            for (R remoteItem : remoteData) {
                Log.d(TAG, "check remote item: " + remoteItem.toString());
                L localItem = findRemoteItemInLocalData(remoteItem, localData);
                if (localItem == null) {
                    // new item from server
                    localItem = mLocalStore.create();
                    localItem.mapFromRemote(remoteItem);
                    Log.d(TAG, "no local found. adding: " + localItem.toString());
                    localItem = mLocalStore.add(localItem);
                } else {
                    syncItem(localItem, remoteItem);
                }
            }
        }else{
            Log.d(TAG, "remoteData is not avail ");
        }
    }

    private void push() {
        Log.d(TAG, "push...");
        List<L> localData = mLocalStore.get();
        List<R> remoteData = mRemoteStore.get();
        Log.d(TAG, ListViewer.view(localData));
        if (null != localData && 0 < localData.size()) {
            for (L localItem : localData) {
                Log.e(TAG, "check local item: " + localItem.toString());
                R remoteItem = findLocalItemInRemoteData(localItem, remoteData);
                if (remoteItem == null) {
                    remoteItem = mRemoteStore.create();
                    remoteItem.mapFromLocal(localItem);
                    remoteItem = mRemoteStore.add(remoteItem);
                    Log.e(TAG, "check remote item: " + remoteItem);
                    if (null != remoteItem) {
                        localItem.setRemoteId(remoteItem.getRemoteId());
                        localItem.setLastUpdatedSequence(remoteItem.getLastUpdatedSequence());
                        mLocalStore.update(localItem);
                    }
                } else {
                    syncItem(localItem, remoteItem);
                }
            }
        }else{
            Log.d(TAG, "localData is not avail.");
        }
    }

    /***
     * Initiate 2-way sync.
     */
    public void sync() {
        pull();
        push();
    }


    /***
     * @param localStore  - Interface to local datastore
     * @param remoteStore - Interface to remote datastore
     */
    public SyncManager(Datastore<L> localStore, Datastore<R> remoteStore) {
        mLocalStore = localStore;
        mRemoteStore = remoteStore;
    }

}
