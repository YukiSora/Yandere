package moe.yukisora.yandere.core;

import android.util.SparseLongArray;

public class DownloadRequestManager {
    private static DownloadRequestManager manager;
    private SparseLongArray requestIds;

    private DownloadRequestManager() {
        requestIds = new SparseLongArray();
    }

    public static DownloadRequestManager getInstance() {
        if (manager == null) {
            manager = new DownloadRequestManager();
        }

        return manager;
    }

    public void put(int id, long requestId) {
        requestIds.put(id, requestId);
    }

    public long get(int id) {
        return requestIds.get(id);
    }

    public void delete(long requestId) {
        requestIds.removeAt(requestIds.indexOfValue(requestId));
    }
}
