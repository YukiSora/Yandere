package moe.yukisora.yandere.core;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

import moe.yukisora.yandere.YandereApplication;

public class DownloadRequestManager {
    private static DownloadRequestManager manager;
    private Gson gson;
    private HashMap<Integer, Long> requestIds;

    @SuppressLint("UseSparseArrays")
    private DownloadRequestManager() {
        gson = new Gson();
        requestIds = new HashMap<>();
    }

    public static DownloadRequestManager getInstance() {
        if (manager == null) {
            manager = new DownloadRequestManager();
        }

        return manager;
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("ConstantConditions")
    public void fromJson(final Context context) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                @SuppressLint("UseSparseArrays")
                HashMap<Integer, Long> requestIds = new HashMap<>();
                try (Reader in = new FileReader(new File(YandereApplication.getInternalDirectory(), YandereApplication.DOWNLOAD_REQUESTS_FILENAME))) {
                    requestIds = gson.fromJson(in, new TypeToken<HashMap<Integer, Long>>() {}.getType());
                } catch (IOException ignore) {
                }

                DownloadManager downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
                for (int id : requestIds.keySet()) {
                    try (Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(requestIds.get(id)))) {
                        cursor.moveToFirst();
                        int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));

                        if (status != DownloadManager.STATUS_SUCCESSFUL && status != DownloadManager.STATUS_FAILED) {
                            DownloadRequestManager.this.requestIds.put(id, requestIds.get(id));
                        }
                    }
                }

                return DownloadRequestManager.this.requestIds.size() != requestIds.size();
            }

            @Override
            protected void onPostExecute(Boolean isModified) {
                if (isModified) {
                    toJson();
                }
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void toJson() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try (FileWriter out = new FileWriter(new File(YandereApplication.getInternalDirectory(), YandereApplication.DOWNLOAD_REQUESTS_FILENAME))) {
                    gson.toJson(requestIds, out);
                } catch (IOException ignore) {
                }

                return null;
            }
        }.execute();
    }

    public void put(int id, long requestId) {
        requestIds.put(id, requestId);
        toJson();
    }

    public long get(int id) {
        return requestIds.containsKey(id) ? requestIds.get(id) : 0;
    }

    public void delete(long requestId) {
        for (int key : requestIds.keySet()) {
            if (requestIds.get(key) == requestId) {
                requestIds.remove(key);
                toJson();
                break;
            }
        }
    }
}
