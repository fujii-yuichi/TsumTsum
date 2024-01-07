/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application.bs5;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

import club.u_1.tsum.application.HeartSendState;
import club.u_1.tsum.application.MemberModel;
import club.u_1.tsum.application.StatusModel;

public class BS5Status {

	// ----- 静的フィールド -----
	private static StatusModel instance = new StatusModel();

	/**
	 * get 自身のランキングNo.
	 * @return 自身のランキングNo.
	 */
	public static int get_number_of_my_rank() {
		return instance.get_number_of_my_rank();
	}

	/**
	 * set 自身のランキングNo.
	 * @param number_of_my_rank 自身のランキングNo.
	 */
	public static void set_number_of_my_rank(int number_of_my_rank) {
		instance.set_number_of_my_rank(number_of_my_rank);
	}

	/**
	 * get ランキングメンバー数.
	 * @return ランキングメンバー数
	 */
	public static int get_number_of_members() {
		return instance.get_number_of_members();
	}

	/**
	 * set ランキングメンバー数.
	 * @param number_of_members ランキングメンバー数
	 */
	public static void set_number_of_members(int number_of_members) {
		instance.set_number_of_members(number_of_members);
	}

	/**
	 * get ハート未送信集合.
	 * @return hearts_unsent ハート未送信集合
	 */
	public static Set<Integer> get_hearts_unsent() {
		return instance.get_hearts_unsent();
	}

	/**
	 * set ハート未送信集合.
	 * @param hearts_unsent ハート未送信集合
	 */
	public static void set_hearts_unsent(Set<Integer> hearts_unsent) {
		instance.set_hearts_unsent(hearts_unsent);
	}

	/**
	 * get ハート送信失敗集合.
	 * @return hearts_sent_failure
	 */
	public static Set<Integer> get_hearts_sent_failure() {
		return instance.get_hearts_sent_failure();
	}

	/**
	 * set ハート送信失敗集合.
	 * @param hearts_sent_failure ハート送信失敗集合
	 */
	public static void set_hearts_sent_failure(Set<Integer> hearts_sent_failure) {
		instance.set_hearts_sent_failure(hearts_sent_failure);
	}

	/**
	 * ♥送信日時を更新.
	 * @param number_rank ランキングNo.
	 * @param send_datetime ♥送信日時
	 * @param status ♥送信状態
	 */
	public static void update_send_datetime(
		Integer number_rank,
		LocalDateTime send_datetime,
		HeartSendState status) {
		// ステータスに応じた更新可否判定
		MemberModel update = instance.update_send_datetime(number_rank, send_datetime, status);
		if (Objects.nonNull(update)) {
			switch (status) {
			case COLOURFUL_TSUM:
				GillsActivity.update_send_datetime(
					update.getRankNumber(),
					update.getLastSendDatetime());
				break;
			default:
				GillsActivity.update_send_datetime_candidate(
					update.getRankNumber(),
					update.getLastSendDatetime());
				break;
			}
		}
	}

	/**
	 * タイトル戻し検出.
	 * @return 検出有無
	 */
	public static boolean is_returned_to_title() {
		return instance.is_returned_to_title();
	}

	/**
	 * タイトル戻し検出.
	 * @param was_returned_to_title 検出有無
	 */
	public static void set_returned_to_title(boolean was_returned_to_title) {
		instance.set_returned_to_title(was_returned_to_title);
	}

	/**
	 * ルート化を検出黒通知検出.
	 * @return 検出有無
	 */
	public static boolean is_root_exist_popup() {
		return instance.is_root_exist_popup();
	}

	/**
	 * ルート化を検出黒通知検出.
	 * @param was_root_exist_popup 検出有無
	 */
	public static void set_root_exist_popup(boolean was_root_exist_popup) {
		instance.set_root_exist_popup(was_root_exist_popup);
	}

	/**
	 * 延長遅延検出.
	 * @return 検出有無
	 */
	public static boolean is_extended_delay() {
		return instance.is_extended_delay();
	}

	/**
	 * 延長遅延検出.
	 * @param extended_delay 検出有無
	 */
	public static void set_extended_delay(boolean extended_delay) {
		instance.set_extended_delay(extended_delay);
	}

	/**
	 * 次回送信日時.
	 * @return 送信日時
	 */
	public static LocalDateTime get_next_datetime() {
		return instance.next_datetime;
	}

	/**
	 * 次回送信日時.
	 * @param datetime 送信日時
	 */
	public static void set_next_datetime(LocalDateTime datetime) {
		instance.next_datetime = datetime;
	}

	/**
	 * 先頭送信日時.
	 * @return 送信日時
	 */
	public static LocalDateTime get_first_send_datetime() {
		return instance.first_send_datetime;
	}

	/**
	 * 先頭送信日時.
	 * @param datetime 送信日時
	 */
	public static void set_first_send_datetime(LocalDateTime datetime) {
		instance.first_send_datetime = datetime;
	}

	/**
	 * 最終送信日時.
	 * @return 送信日時
	 */
	public static LocalDateTime get_last_send_datetime() {
		return instance.last_send_datetime;
	}

	/**
	 * 最終送信日時.
	 * @param datetime 送信日時
	 */
	public static void set_last_send_datetime(LocalDateTime datetime) {
		instance.last_send_datetime = datetime;
	}

	/**
	 * 前回先頭送信日時.
	 * @return 送信日時
	 */
	public static LocalDateTime get_last_time_first_send_datetime() {
		return instance.last_time_first_send_datetime;
	}

	/**
	 * 前回先頭送信日時.
	 * @param datetime 送信日時
	 */
	public static void set_last_time_first_send_datetime(LocalDateTime datetime) {
		instance.last_time_first_send_datetime = datetime;
	}

	/**
	 * 前回最終送信日時.
	 * @return 送信日時
	 */
	public static LocalDateTime get_last_time_last_send_datetime() {
		return instance.last_time_last_send_datetime;
	}

	/**
	 * 前回最終送信日時.
	 * @param datetime 送信日時
	 */
	public static void set_last_time_last_send_datetime(LocalDateTime datetime) {
		instance.last_time_last_send_datetime = datetime;
	}

	public static LocalDateTime store_members_list_datetime = null;
}
