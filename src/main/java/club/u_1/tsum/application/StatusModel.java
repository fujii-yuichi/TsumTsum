/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class StatusModel {
	/** 自身のランキングNo. */
	private int number_of_my_rank = -1;

	/**
	 * get 自身のランキングNo.
	 * @return 自身のランキングNo.
	 */
	public int get_number_of_my_rank() {
		return number_of_my_rank;
	}

	/**
	 * set 自身のランキングNo.
	 * @param number_of_my_rank 自身のランキングNo.
	 */
	public void set_number_of_my_rank(int number_of_my_rank) {
		if (this.number_of_my_rank != number_of_my_rank) {
			// 記憶している自身の位置と異なるなら除外対象を入れ替える
			if (hearts_unsent.contains(number_of_my_rank)) {
				// 変動があるなら
				// 記憶している自身の位置を追加する
				hearts_unsent.add(this.number_of_my_rank);
				// 新たな自身の位置を除外する
				hearts_unsent.remove(number_of_my_rank);
			}
		}
		this.number_of_my_rank = number_of_my_rank;
	}

	/** ランキングメンバー数. */
	private int number_of_members = -1;

	/**
	 * get ランキングメンバー数.
	 * @return ランキングメンバー数
	 */
	public int get_number_of_members() {
		return this.number_of_members;
	}

	/**
	 * set ランキングメンバー数.
	 * @param number_of_members ランキングメンバー数
	 */
	public void set_number_of_members(int number_of_members) {
		this.number_of_members = number_of_members;
	}

	/** ハート未送信集合. */
	private Set<Integer> hearts_unsent = new LinkedHashSet<Integer>();

	/**
	 * get ハート未送信集合.
	 * @return hearts_unsent ハート未送信集合
	 */
	public Set<Integer> get_hearts_unsent() {
		return this.hearts_unsent;
	}

	/**
	 * set ハート未送信集合.
	 * @param hearts_unsent ハート未送信集合
	 */
	public void set_hearts_unsent(Set<Integer> hearts_unsent) {
		this.hearts_unsent = hearts_unsent;
	}

	/** ハート送信失敗集合. */
	private Set<Integer> hearts_sent_failure = new HashSet<Integer>();

	/**
	 * get ハート送信失敗集合.
	 * @return hearts_sent_failure
	 */
	public Set<Integer> get_hearts_sent_failure() {
		return this.hearts_sent_failure;
	}

	/**
	 * set ハート送信失敗集合.
	 * @param hearts_sent_failure ハート送信失敗集合
	 */
	public void set_hearts_sent_failure(Set<Integer> hearts_sent_failure) {
		this.hearts_sent_failure = hearts_sent_failure;
	}

	/** ♥送信中ランキングNo. */
	private Integer sending_number_rank = null;

	/**
	 * ♥送信日時を更新.
	 * @param number_rank ランキングNo.
	 * @param send_datetime ♥送信日時
	 * @param status ♥送信状態
	 * @return 更新対象情報
	 * <br>※使用可能な情報はランキングNo.と前回送信日時
	 * <br>≠null：更新可
	 * <br>＝null：更新不可
	 */
	public MemberModel update_send_datetime(
		Integer number_rank,
		LocalDateTime send_datetime,
		HeartSendState status) {

		// ♥送信状態毎の処理
		switch (status) {
		case HEART_TAP:
			// ◆♥タップ
			if (Objects.nonNull(sending_number_rank)) {
				// 未完了があれば、ハート送信失敗として処理する
				get_hearts_sent_failure().add(sending_number_rank);
			}
			// ♥送信中ランキングNo.セット
			sending_number_rank = number_rank;
			break;
		case COLOURFUL_TSUM:
			// ◆カラフルツム認識
			if (Objects.nonNull(number_rank)) {
				// ランキングNo.の指定あり
				if (number_rank.equals(sending_number_rank)) {
					// ♥タップしたのと同じランキングNo.なら、無処理
				} else {
					if (Objects.nonNull(sending_number_rank)) {
						// 未完了があれば、ハート送信失敗として処理する
						get_hearts_sent_failure().add(sending_number_rank);
					}
				}
			} else {
				// ランキングNo.の指定なし
				// ♥送信中ランキングNo.を採用
				number_rank = sending_number_rank;
			}
			// ♥送信中ランキングNo.クリア
			sending_number_rank = null;
			break;
		default:
			// ♥送信中ランキングNo.クリア
			sending_number_rank = null;
			break;
		}
		if (Objects.isNull(send_datetime)) {
			// ♥送信日時の指定が無ければ、現在時刻で補間
			send_datetime = LocalDateTime.now();
		}
		if (Objects.nonNull(number_rank)) {
			// ♥を送信した日時を更新
			MemberModel result = new MemberModel();
			result.rankNumber = number_rank;
			result.lastSendDatetime = send_datetime;
			return result;
		}
		return null;
	}

	/** タイトル戻し検出. */
	private boolean returned_to_title = false;

	/**
	 * タイトル戻し検出.
	 * @return 検出有無
	 */
	public boolean is_returned_to_title() {
		return this.returned_to_title;
	}

	/**
	 * タイトル戻し検出.
	 * @param was_returned_to_title 検出有無
	 */
	public void set_returned_to_title(boolean was_returned_to_title) {
		this.returned_to_title = was_returned_to_title;
	}

	/** ルート化を検出黒通知検出. */
	private boolean root_exist_popup = false;

	/**
	 * ルート化を検出黒通知検出.
	 * @return 検出有無
	 */
	public boolean is_root_exist_popup() {
		return this.root_exist_popup;
	}

	/**
	 * ルート化を検出黒通知検出.
	 * @param was_root_exist_popup 検出有無
	 */
	public void set_root_exist_popup(boolean was_root_exist_popup) {
		this.root_exist_popup = was_root_exist_popup;
	}

	/** 延長遅延検出. */
	private boolean extended_delay = false;

	/**
	 * 延長遅延検出.
	 * @return 検出有無
	 */
	public boolean is_extended_delay() {
		return this.extended_delay;
	}

	/**
	 * 延長遅延検出.
	 * @param extended_delay 検出有無
	 */
	public void set_extended_delay(boolean extended_delay) {
		this.extended_delay = extended_delay;
	}

	public LocalDateTime next_datetime = null;
	public LocalDateTime last_time_first_send_datetime = null;
	public LocalDateTime last_time_last_send_datetime = null;
	public LocalDateTime first_send_datetime = null;
	public LocalDateTime last_send_datetime = null;
	public LocalDateTime store_members_list_datetime = null;
	public int count_hearts = 0;
}
