package com.qdapi.downloaderManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qdapi.downloaderManager.download.DownloadConfig;
import com.qdapi.downloaderManager.download.core.DownloadEntry;
import com.qdapi.downloaderManager.download.core.QuietDownloader;
import com.qdapi.downloaderManager.download.notify.DataUpdatedWatcher;
import com.qdapi.downloaderManager.util.CommonUtil;
import com.qdapi.downloaderManager.util.JsonOptions;
import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import io.dcloud.common.util.ReflectUtils;

public class KarmaDownloader extends WXSDKEngine.DestroyableModule {

    public static Context contextUI;
    public static Context contextAPP;
    private QuietDownloader mQuietDownloader;
    private DownloadEntry entry;
    private DataUpdatedWatcher mDataUpdateReceiver;
    public String TAG;
    private Integer countDownload;
    private JSONArray downloadList;
    private DownloadConfig downloadConfig;

    public KarmaDownloader()
    {
        TAG = "console";
        Log.d("asd", "123");
        countDownload = 0;
        downloadList = null;
        downloadConfig = null;
    }

    public void destroy()
    {
    }

    public void onActivityDestroy()
    {
        super.onActivityDestroy();
    }

    @JSMethod(uiThread = false)
    public void init(JSONObject options, JSCallback jsCallback)
    {
        Log.d(TAG, "init: 123");
        contextUI = mWXSDKInstance.getUIContext();
        contextAPP = ReflectUtils.getApplicationContext();
        downloadConfig = new DownloadConfig(contextAPP);
        if (options.getString(JsonOptions.mMaxDownloadTasks) != null)
            downloadConfig.setMaxDownloadTasks(Integer.parseInt(options.getString(JsonOptions.mMaxDownloadTasks)));
        if (options.getString(JsonOptions.mMaxDownloadThreads) != null)
            downloadConfig.setMaxDownloadThreads(Integer.parseInt(options.getString(JsonOptions.mMaxDownloadThreads)));
        if (options.getString(JsonOptions.mDownloadDir) != null)
        {
            File downloadDir = new File(options.getString(JsonOptions.mDownloadDir));
            downloadConfig.setDownloadDir(downloadDir);
        }
        if (options.getString(JsonOptions.mMinOperateInterval) != null)
            downloadConfig.setMinOperateInterval(Integer.parseInt(options.getString(JsonOptions.mMinOperateInterval)));
        if (options.getString(JsonOptions.mAutoRecovery) != null)
            downloadConfig.setAutoRecovery(Boolean.parseBoolean(options.getString(JsonOptions.mAutoRecovery)));
        if (options.getString(JsonOptions.mOpenRetry) != null)
            downloadConfig.setOpenRetry(Boolean.parseBoolean(options.getString(JsonOptions.mOpenRetry)));
        if (options.getString(JsonOptions.mMaxRetryCount) != null)
            downloadConfig.setMaxRetryCount(Integer.parseInt(options.getString(JsonOptions.mMaxRetryCount)));
        if (options.getString(JsonOptions.mRetryIntervalMillis) != null)
            downloadConfig.setRetryIntervalMillis(Integer.parseInt(options.getString(JsonOptions.mRetryIntervalMillis)));
        if (options.getString(JsonOptions.isAssignNetworl) != null)
            downloadConfig.setAssignNetwork(Boolean.parseBoolean(options.getString(JsonOptions.isAssignNetworl)));

        QuietDownloader.initializeDownloader(downloadConfig);
        Log.d(TAG, "init: 123");
//        mQuietDownloader = QuietDownloader.getImpl();
        jsCallback.invoke(CommonUtil.result("", "初始化成功", 0));
    }

    @JSMethod(uiThread = false)
    public void createDownloadTask(JSONObject options, JSCallback jsCallback)
    {
        DownloadEntry info = mQuietDownloader.queryById(options.getString(JsonOptions.downUrl));
        if (info == null) {
            mQuietDownloader.download(new DownloadEntry(options.getString(JsonOptions.downUrl), options.getString(JsonOptions.saveName)));
            try {
                Thread.sleep(200);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            jsCallback.invoke(CommonUtil.result("", "任务创建成功", 0));
        } else {
            Log.d(TAG, "任务已创建");
            jsCallback.invoke(CommonUtil.result("", "任务已创建", 3000));
        }
    }

    @JSMethod(uiThread = false)
    public void recoverAll()
    {
        mQuietDownloader.recoverAll();
    }

    @JSMethod(uiThread = false)
    public void pauseAll()
    {
        mQuietDownloader.pauseAll();
    }

    @JSMethod(uiThread = false)
    public void queryAll(JSCallback jsCallback)
    {
        List list = mQuietDownloader.queryAll();
        String _list = CommonUtil.downloadEntryToJosnString(list.toString());
        downloadList = JSONArray.parseArray(_list);
        countDownload = Integer.valueOf(downloadList.size());
        JSONObject objectNew;
        for (Iterator iterator = downloadList.iterator(); iterator.hasNext(); objectNew.put("totalLength", Integer.valueOf(objectNew.getIntValue("totalLength") / 0x100000)))
        {
            Object object = iterator.next();
            objectNew = (JSONObject)object;
            objectNew.put("currentLength", Integer.valueOf(objectNew.getIntValue("currentLength") / 0x100000));
        }

        _list = downloadList.toString();
        try {
            Thread.sleep(300L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jsCallback.invoke(CommonUtil.result(_list, "获取下载列表成功", Integer.valueOf(0)));
    }

    @JSMethod(uiThread = false)
    public void queryById(String id, JSCallback jsCallback)
    {
        DownloadEntry info = mQuietDownloader.queryById(id);
        jsCallback.invoke(CommonUtil.result(info.toString(), "获取成功", Integer.valueOf(0)));
    }

    @JSMethod(uiThread = false)
    public void listener(final JSCallback jsCallback)
    {
        Log.d(TAG, "listener: 监听");
        mDataUpdateReceiver = new DataUpdatedWatcher() {
            public void notifyUpdate(DownloadEntry data)
            {
                if (data.id.equals(data.id))
                {
                    entry = data;
                    JSONObject info = new JSONObject();
                    info.put("id", entry.id);
                    info.put("save_name", entry.name);
                    info.put("status", entry.status);
                    info.put("current_size", Integer.valueOf(entry.currentLength / 100));
                    info.put("total_size", Integer.valueOf(entry.totalLength / 100));
                    float length = ((float)entry.currentLength * 1.0F) / (float)entry.totalLength;
                    int percent = (int)(length * 100F);
                    info.put("percent", Integer.valueOf(percent));
                    jsCallback.invokeAndKeepAlive(CommonUtil.result(info.toJSONString(), "下载监听", 0));
                }
            }
        };
        mQuietDownloader.addObserver(mDataUpdateReceiver);
    }

    @JSMethod(uiThread = false)
    public void stopListener()
    {
        mQuietDownloader.removeObserver(mDataUpdateReceiver);
    }

    @JSMethod(uiThread = false)
    public void deleteAll(Boolean isDeleteFile)
    {
        List list = mQuietDownloader.queryAll();
        String _list = CommonUtil.downloadEntryToJosnString(list.toString());
        JSONArray _downloadList = JSONArray.parseArray(_list);
        if (_downloadList.size() > 0) {
            for (Integer i = 0; i <= _downloadList.size();i++)
            {
                JSONObject json = _downloadList.getJSONObject(i);
                String id = json.getString("id");
                String name = json.getString("name");
                Log.d(TAG, "deleteAll: " + id.toString());
                Log.d(TAG, "deleteAll: " + name);
                mQuietDownloader.deleteById(id);
                if (isDeleteFile.booleanValue())
                    mQuietDownloader.deleteFileByName(name);
            }
        }


    }

    @JSMethod(uiThread = false)
    public void pauseById(String id)
    {
        DownloadEntry info = mQuietDownloader.queryById(id);
        mQuietDownloader.pause(info);
    }

    @JSMethod(uiThread = false)
    public void resumeById(String id)
    {
        DownloadEntry info = mQuietDownloader.queryById(id);
        mQuietDownloader.resume(info);
    }

    @JSMethod(uiThread = false)
    public void cancelById(String id)
    {
        DownloadEntry info = mQuietDownloader.queryById(id);
        mQuietDownloader.cancel(info);
    }

    @JSMethod(uiThread = false)
    public void deleteById(String id, Boolean isDeleteFile, String name)
    {
        mQuietDownloader.deleteById(id);
        if (isDeleteFile.booleanValue() && !name.isEmpty())
            mQuietDownloader.deleteFileByName(name);
    }

    @JSMethod(uiThread = false)
    public void getFilePath(JSCallback jsCallback)
    {
        String path = CommonUtil.getDiskCacheDir(contextAPP);
        jsCallback.invoke(CommonUtil.result((new StringBuilder()).append(path).append("/quietDownloader/").toString(), "获取成功", Integer.valueOf(0)));
    }
}
