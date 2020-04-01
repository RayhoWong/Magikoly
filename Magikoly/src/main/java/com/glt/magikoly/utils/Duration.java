package com.glt.magikoly.utils;

import com.glt.magikoly.FaceEnv;

import java.util.HashMap;

/**
 *
 * @author yangguanxiang
 *
 */
public class Duration {
	private static final boolean DEBUG = FaceEnv.sSIT;
	private static final String TAG = "Duration";
	public static HashMap<String, Duration> sMap = new HashMap<>();
	public long start;
	public long end;

	private Duration() {
	}

	public static void reset(String tag) {
		if (!DEBUG) {
			return;
		}
		if (sMap.containsKey(tag)) {
			Duration duration = sMap.get(tag);
			duration.start = 0;
			duration.end = 0;
		}
	}

	public static void clear(String tag) {
		if (!DEBUG) {
			return;
		}
        sMap.remove(tag);
	}

	public static void setStart(String tag) {
		if (!DEBUG) {
			return;
		}
		Duration duration = getDurationInstance(tag);
		duration.start = System.nanoTime();
	}

	private static Duration setEnd(String tag) {
		if (!DEBUG) {
			return null;
		}
		Duration duration = getDurationInstance(tag);
		duration.end = System.nanoTime();
		return duration;
	}

	public static long getDuration(String tag) {
		if (!DEBUG) {
			return 0;
		}
		Duration duration = setEnd(tag);
		return (duration.end - duration.start) / 1000000;
	}

	private static Duration getDurationInstance(String tag) {
		if (!DEBUG) {
			return null;
		}
		Duration duration;
		if (sMap.containsKey(tag)) {
			duration = sMap.get(tag);
		} else {
			duration = new Duration();
			sMap.put(tag, duration);
		}
		return duration;
	}

	public static void logDuration(String tag) {
		Logcat.i(TAG, tag + " -- " + getDuration(tag));
	}
}
