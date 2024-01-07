/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * メンバー情報.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberModel implements Serializable {
	/**
	 * ランキングNo.
	 */
	public int rankNumber = -1;

	/**
	 * get ランキングNo.
	 * @return rankNumber
	 */
	public int getRankNumber() {
		return rankNumber;
	}

	/**
	 * 前回送信日時.
	 */
	public LocalDateTime lastSendDatetime = null;

	/**
	 * get 前回送信日時.
	 * @return lastSendDatetime
	 */
	public LocalDateTime getLastSendDatetime() {
		return lastSendDatetime;
	}

	/**
	 * 前回送信日時候補.
	 */
	@JsonIgnore
	public LocalDateTime lastSendDatetimeCandidate = null;

	/**
	 * get 前回送信日時候補.
	 * @return lastSendDatetimeCandidate
	 */
	@JsonProperty("lastSendDatetimeCandidate")
	public LocalDateTime getLastSendDatetimeCandidate() {
		return lastSendDatetimeCandidate;
	}

	//	/**
	//	 *  名前(未使用).
	//	 *  <P>
	//	 *  名前をキャプチャした画像ファイルのパス
	//	 */
	//	public String name = "";
	//
	//	/**
	//	 *  アイコン(未使用).
	//	 *  <P>
	//	 *  アイコンをキャプチャした画像ファイルのパス
	//	 */
	//	public String icon = "";
	//
	//	/**
	//	 *  スコア(未使用).
	//	 *  <P>
	//	 *  スコアをキャプチャした画像ファイルのパス<BR>
	//	 */
	//	public String score = "";
	//
	//	/**
	//	 *  ハイスコア(未使用).
	//	 *  <P>
	//	 *  ハイスコアをキャプチャした画像ファイルのパス
	//	 */
	//	public String highScore = "";

	/**
	 * ハイスコアポップアップ.
	 *  <P>
	 *  ハイスコアポップアップをキャプチャした画像ファイルのパス
	 */
	public String highScorePopup = "";

	/**
	 * 同分回数.
	 */
	public int timesOfSameMin = 0;

	/**
	 * get 同分回数.
	 * @return timesOfSameMin
	 */
	public int getTimesOfSameMin() {
		return timesOfSameMin;
	}

	/**
	 * 同秒回数.
	 */
	public int timesOfSameSec = 0;

	/**
	 * get 同秒回数.
	 * @return timesOfSameSec
	 */
	public int getTimesOfSameSec() {
		return timesOfSameSec;
	}

	/**
	 * オフセット(ns).
	 */
	public long offsetOfNanos = 0;

	/**
	 * get オフセット(ns).
	 * @return offsetOfNanos
	 */
	public long getOffsetOfNanos() {
		return offsetOfNanos;
	}

	/**
	 * インスタンス生成.
	 * @param rankNumber
	 * @param lsd
	 * @param highScorePopup
	 * @param times_of_same_min
	 * @param times_of_same_sec
	 * @return 生成したインスタンス
	 */
	public static MemberModel of(
		int rank_no,
		LocalDateTime lsd,
		String high_score_popup,
		int times_of_same_min,
		int times_of_same_sec,
		long offset_of_nanos) {
		MemberModel instance = new MemberModel();
		instance.rankNumber = rank_no;
		instance.lastSendDatetime = lsd;
		instance.highScorePopup = high_score_popup;
		instance.timesOfSameMin = times_of_same_min;
		instance.timesOfSameSec = times_of_same_sec;
		instance.offsetOfNanos = offset_of_nanos;
		return instance;
	}

	/**
	 * インスタンス生成.
	 * @param rankNumber
	 * @param lsd
	 * @param highScorePopup
	 * @return 生成したインスタンス
	 */
	public static MemberModel of(
		int rank_no,
		LocalDateTime lsd,
		String high_score_popup) {
		MemberModel instance = new MemberModel();
		instance.rankNumber = rank_no;
		instance.lastSendDatetime = lsd;
		instance.highScorePopup = high_score_popup;
		return instance;
	}
}
