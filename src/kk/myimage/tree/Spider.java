package kk.myimage.tree;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import kk.myimage.activity.BaseActivity;
import kk.myimage.util.AppUtil;
import kk.myimage.util.Logger;

public class Spider {
	static final String FILE_NAME = "spider.tree";
	static final String CHAR_SET = "UTF-8";

	static final String KEY_PATH = "path";
	static final String KEY_THUM = "thum";
	static final String KEY_HIDDEN = "hidden";
	static final String KEY_SORT_INDEX = "sort_index";

	public static class SpiderNode {
		public String thum;
		public boolean hidden = false;
		public int sortIndex = 0;

		public SpiderNode() {
		}

		public SpiderNode(SpiderNode sn) {
			if (sn == null) {
				return;
			}
			
			thum = sn.thum;
			hidden = sn.hidden;
			sortIndex = sn.sortIndex;
		}
	}

	private static Map<String, SpiderNode> sMap = new HashMap<String, SpiderNode>();

	private static boolean sSaveDelay = false;

	static {
		load();
	}

	public static void load() {
		try {
			InputStream is = BaseActivity.ca().openFileInput(FILE_NAME);
			String string = read(is);
			Map<String, SpiderNode> map = decode(string);

			synchronized (sMap) {
				sMap = map;
			}
		} catch (Exception e) {
			Logger.print(null, e);
		}
	}

	public static void save() {
		try {
			String string = encode(Ant.getData(true));
			OutputStream os = BaseActivity.ca().openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
			write(os, string);
		} catch (Exception e) {
			Logger.print(null, e);
		}
	}

	public static void saveDelay() {
		synchronized (Spider.class) {
			if (sSaveDelay) {
				return;
			}
			sSaveDelay = true;
		}

		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				synchronized (Spider.class) {
					sSaveDelay = false;
				}

				save();
			}
		}, 5000);
	}

	private static String read(InputStream is) throws Exception {
		byte[] data = new byte[1024 * 1024 * 8];
		int len, off = 0;

		while ((len = is.read(data, off, 1024 * 1024)) != -1) {
			off += len;
		}

		is.close();

		return new String(data, 0, off, CHAR_SET);
	}

	private static void write(OutputStream os, String string) throws Exception {
		byte[] data = string.getBytes(CHAR_SET);
		os.write(data);
		os.close();
	}

	private static Map<String, SpiderNode> decode(String string) throws Exception {
		Map<String, SpiderNode> map = new HashMap<String, SpiderNode>();

		JSONArray ja = new JSONArray(string);
		int len = ja.length();

		for (int i = 0; i < len; i++) {
			JSONObject jo = ja.getJSONObject(i);

			String path = jo.getString(KEY_PATH);
			File direct = new File(path);
			if (!direct.exists() || !direct.canRead() || !direct.isDirectory()) {
				continue;
			}

			SpiderNode sn = new SpiderNode();
			try {
				sn.thum = jo.getString(KEY_THUM);
			} catch (Exception e) {
			}
			try {
				sn.hidden = jo.getBoolean(KEY_HIDDEN);
			} catch (Exception e) {
			}
			try {
				sn.sortIndex = jo.getInt(KEY_SORT_INDEX);
			} catch (Exception e) {
			}

			map.put(path, sn);
		}

		return map;
	}

	private static String encode(List<BranchData> list) throws Exception {
		JSONArray ja = new JSONArray();
		int len = list.size();
		
		synchronized (sMap) {
			for (int i = 1; i < len; i++) {
				BranchData bd = list.get(i);
				String path = bd.getPath();
				SpiderNode sn = sMap.get(path);
				
				JSONObject jo = new JSONObject();
				jo.put(KEY_PATH, path);
				
				if (sn == null) {
					sMap.put(path, new SpiderNode());
				} else {
					if (sn.thum != null) {
						jo.put(KEY_THUM, sn.thum);
					}

					if (sn.hidden) {
						jo.put(KEY_HIDDEN, sn.hidden);
					}

					if (sn.sortIndex != 0) {
						jo.put(KEY_SORT_INDEX, sn.sortIndex);
					}
				}
				
				ja.put(jo);
			}
		}
			
		return ja.toString();
	}

	public static Map<String, SpiderNode> getAllNode() {
		Map<String, SpiderNode> map = new HashMap<String, SpiderNode>();

		synchronized (sMap) {
			for (Entry<String, SpiderNode> entry : sMap.entrySet()) {
				map.put(entry.getKey(), new SpiderNode(entry.getValue()));
			}
		}

		return map;
	}

	public static SpiderNode getNode(String path) {
		synchronized (sMap) {
			if (sMap.containsKey(path)) {
				return new SpiderNode(sMap.get(path));
			}
		}

		return null;
	}

	public static void setAllNode(Map<String, SpiderNode> map) {
		synchronized (sMap) {
			sMap = map;
		}

		saveDelay();
	}

	public static void setNode(String path, SpiderNode sn) {
		synchronized (sMap) {
			sMap.put(path, new SpiderNode(sn));
		}

		saveDelay();
	}

	public static void removeNode(String path) {
		synchronized (sMap) {
			sMap.remove(path);
		}

		saveDelay();
	}
}
