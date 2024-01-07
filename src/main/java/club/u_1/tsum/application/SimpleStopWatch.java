/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application;

import java.util.HashMap;
import java.util.Map;

/**
 * シンプルストップウォッチ.
 */
public final class SimpleStopWatch {
	// ****************************************************************
	// 定数
	// ****************************************************************
	/** 
	 * ナノ秒 to ミリ秒.
	 */
	private static final int NANO_TO_MILLI_SECOND = 1000000;

	// ****************************************************************
	// フィールド
	// ****************************************************************
	/**
	 * startTimeNS.
	 */
	private static Map<String, Long> startTimeNS;

	/**
	 * stopTimeNS.
	 */
	private static Map<String, Long> stopTimeNS;

	// ****************************************************************
	// メソッド
	// ****************************************************************
	/**
	 * コンストラクタ.
	 */
	private SimpleStopWatch() {
		// 無処理
	}

	/**
	 * 初期化.
	 */
	static {
		// 初期化
		startTimeNS = new HashMap<String, Long>();
		stopTimeNS = new HashMap<String, Long>();
	}

	/**
	 * 開始.
	 * 
	 * @param id
	 */
	public static void start(final String id) {
		startTimeNS.put(id, System.nanoTime());
		stopTimeNS.remove(id);
	}

	/**
	 * 停止.
	 * 
	 * @param id
	 */
	public static void stop(final String id) {
		stopTimeNS.put(id, System.nanoTime());
	}

	/**
	 * 経過.
	 * 
	 * @param id
	 * @return 経過時間(ミリ秒)
	 */
	public static long elapsed(final String id) {
		Long startTime = startTimeNS.get(id);
		if (startTime == null) {
			return -1;
		}
		Long endTime = stopTimeNS.get(id);
		if (endTime == null) {
			endTime = System.nanoTime();
			stopTimeNS.put(id, endTime);
		}
		return (endTime - startTime) / NANO_TO_MILLI_SECOND;
	}
}
