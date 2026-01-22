// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   CommonUtil.java

package com.qdapi.downloaderManager.util;

import android.content.Context;
import android.os.Environment;
import com.alibaba.fastjson.JSONObject;
import java.io.File;
import java.util.List;
import org.json.JSONArray;

public class CommonUtil
{

	public CommonUtil()
	{
	}

	public static String getDiskCacheDir(Context context)
	{
		String cachePath = null;
		if ("mounted".equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable())
			cachePath = context.getExternalCacheDir().getPath();
		else
			cachePath = context.getCacheDir().getPath();
		return cachePath;
	}

	public static int length(String value)
	{
		int valueLength = 0;
		String chinese = "[u4e00-u9fa5]";
		for (int i = 0; i < value.length(); i++)
		{
			String temp = value.substring(i, i + 1);
			if (temp.matches(chinese))
				valueLength += 2;
			else
				valueLength++;
		}

		return valueLength;
	}

	public static JSONObject result(String string, String msg, Integer code)
	{
		JSONObject result = new JSONObject();
		result.put("code", code);
		result.put("msg", msg);
		result.put("data", string);
		return result;
	}

	public static String listToString(List list)
	{
		JSONArray jsonArray = new JSONArray(list);
		return jsonArray.toString();
	}

	public static String downloadEntryToJosnString(String downloadEntry)
	{
		return downloadEntry.replace("DownloadEntry", "").replace("'", "\"").replace("=", "\":\"").replace("{", "{\"").replace("}", "\"}").replace(", ", "\", \"").replace("\"\"", "\"").replace("}\", \"{", "}, {");
	}
}
