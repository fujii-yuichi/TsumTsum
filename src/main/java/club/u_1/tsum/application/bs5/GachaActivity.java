/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application.bs5;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;

import club.u_1.tsum.application.ConfigureModel;
import club.u_1.tsum.application.my;
import club.u_1.tsum.application.mySS;

public class GachaActivity {

	// ----- 設定値定義 -----
	public static double WAIT_TIMEOUT = 0.001;

	// ----- 定数定義 -----
	static public Region reg_happiness_box = null;
	static public Region reg_premium_box = null;
	static public Region reg_pickup_gacha = null;
	static public Region reg_ok_lower = null;
	static public Region close_upper = null;
	static public Region reg_close_center = null;
	static public Region reg_ruby_exchange = null;
	static public Region reg_cancel = null;
	static public Region reg_store = null;
	static public Region reg_select_gacha = null;
	static public Region reg_ok = null;
	static public Region colorful = null;

	/**
	 * プレミアムボックス Pattern配列.
	 */
	static final public Pattern images_premium_box[] = {
		new Pattern("/bs5/premium_box.png").similar(0.80f),
		new Pattern("/bs5/premium_box_small.png").similar(0.80f),
	};

	/**
	 * ハピネスボックス Pattern配列.
	 */
	static final public Pattern images_happiness_box[] = {
		new Pattern("/bs5/happiness_box.png").similar(0.80f),
		new Pattern("/bs5/happiness_box_small.png").similar(0.80f),
	};

	static final public Pattern pickup_gacha_080 = new Pattern("/bs5/pickup_gacha.png").similar(0.80f);
	static final public Pattern select_box_080 = new Pattern("/bs5/select_box.png").similar(0.80f);
	static final public Pattern ruby_exchange_090 = new Pattern("/bs5/ruby_exchange.png").similar(0.90f);
	static final public Pattern sold_out_080 = new Pattern("/bs5/sold_out.png").similar(0.80f);//

	public static final int C_RETRY_EXISTS = 50;

	public static void set_region() {
		if (Objects.nonNull(BS5Regions.tsum)) {
			Region reg_tsum = BS5Regions.tsum;
			reg_happiness_box = new Region(reg_tsum.x + 66, reg_tsum.y + 544, 140, 68);
			reg_premium_box = new Region(reg_tsum.x + 322, reg_tsum.y + 544, 154, 68);
			reg_pickup_gacha = new Region(reg_tsum.x + 208, reg_tsum.y + 520, 120, 110);
			reg_ok_lower = new Region(reg_tsum.x + 309, reg_tsum.y + 710, 138, 68);
			close_upper = new Region(reg_tsum.x + 206, reg_tsum.y + 699, 115, 55);
			reg_close_center = new Region(reg_tsum.x + 206, reg_tsum.y + 679, 120, 200);
			reg_ruby_exchange = new Region(reg_tsum.x + 301, reg_tsum.y + 536, 160, 50);
			reg_cancel = new Region(reg_tsum.x + 88, reg_tsum.y + 538, 138, 71);
			reg_store = new Region(reg_tsum.x + 397, reg_tsum.y + 814, 100, 70);
			reg_select_gacha = new Region(reg_tsum.x + 218, reg_tsum.y + 528, 100, 66);
			reg_ok = new Region(reg_tsum.x + 309, reg_tsum.y + 538, 138, 71);
			colorful = new Region(reg_tsum.x + 217, reg_tsum.y + 526, 97, 46);
		}
	}

	public static void roll_all_gacha() {
		boolean is_completion = false;
		if (Objects.nonNull(BS5Regions.title)) {
			if (Objects.nonNull(BS5Regions.title.exists(BS5Patterns.title_weekly_ranking_094, WAIT_TIMEOUT))) {
				// 左上の1ドットの色を取得し、0.5より上かを判定する
				boolean is_bright = BS5Activity.is_brightness(BS5Regions.title.getLastMatch());
				if (is_bright) {
					is_completion = true;
				}
			}
		}
		while (!is_completion) {
			// ツム再起動＆Regionセット
			Region reg_tsum = BS5Activity.stop_and_create(BS5Activity.LIMIT_SECONDS_START);
			if (Objects.nonNull(reg_tsum)) {
				is_completion = true;
				BS5Regions.set_region(reg_tsum);
			}
		}

		BS5Activity.recovery_to_ranking(120);
		if (BS5Instance.getNo() == 0) {
			GachaActivity.roll_all_pickup_gacha();
			GachaActivity.roll_all_gacha_select();
		}
		GachaActivity.roll_all_gacha_premium();
		// GachaActivity.roll_all_gacha_happiness();
		BS5Activity.recovery_to_ranking(120);
	}

	/**
	 * ピックアップガチャを引く.
	 */
	public static void roll_all_pickup_gacha() {
		// Region定義
		set_region();

		boolean breaked = false;

		// お金がなくなるまでピックアップガチャを引く
		LocalDateTime next_datetime_with_gacha = LocalDateTime.now();
		if (Objects.nonNull(BS5Status.get_next_datetime())) {
			next_datetime_with_gacha = BS5Status.get_next_datetime().plusMinutes(BS5Activity.OFFSET_MINUTES_RESTART);
		} else {
			next_datetime_with_gacha = LocalDateTime.now().plusSeconds(120);
		}
		while (next_datetime_with_gacha.isAfter(LocalDateTime.now())) {
			// リカバリ
			if (!breaked) {
				Match matches_happiness = reg_happiness_box.findBestList(Arrays.asList((Object[]) images_happiness_box));
				Match matches_premium = reg_premium_box.findBestList(Arrays.asList((Object[]) images_premium_box));
				if (Objects.nonNull(matches_happiness) && Objects.nonNull(matches_premium)) {
					// ストア画面に遷移していたら抜ける
					breaked = true;
					continue;
				}
				// 初回 or 迷子になったら、週間ランキングへ戻る
				BS5Activity.recovery_to_ranking(BS5Activity.LIMIT_SECONDS_RECOVERY_TO_RANKING);
				set_region();
			}

			// ピックアップガチャ表示待ち
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				Match matches_happiness = reg_happiness_box.findBestList(Arrays.asList((Object[]) images_happiness_box));
				Match matches_premium = reg_premium_box.findBestList(Arrays.asList((Object[]) images_premium_box));
				Match matches_pickup = reg_pickup_gacha.exists(pickup_gacha_080, WAIT_TIMEOUT);
				if (Objects.nonNull(matches_happiness) && Objects.nonNull(matches_premium)) {
					if (Objects.nonNull(matches_pickup)) {
						breaked = true;
						break;
					} else {
						if (Objects.nonNull(close_upper.exists(BS5Patterns.close_088, WAIT_TIMEOUT))) {
							my.println("ラスト賞をタップ");
							my.single_tap_by_Location(close_upper.getTarget());
							my.sleep(200);
							continue;
						} else if (Objects.nonNull(reg_pickup_gacha.exists(sold_out_080, WAIT_TIMEOUT))) {
							// 完売？
							my.println("完売");
						} else {
							// ピックアップガチャの期間でない
							my.println("ピックアップガチャの期間ではない");
						}
						return;
					}
				}
				my.println("STOREをタップ");
				my.single_tap_by_Location(reg_store.getTarget());
				my.sleep(200);
			}
			if (!breaked) {
				continue;
			}

			// ピックアップガチャをタップ
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				Match matches = reg_pickup_gacha.exists(pickup_gacha_080, WAIT_TIMEOUT);
				if (Objects.isNull(matches)) {
					breaked = true;
					break;
				} else if (Objects.nonNull(reg_pickup_gacha.exists(sold_out_080, WAIT_TIMEOUT))) {
					// 完売？
					my.println("完売");
					return;
				}
				my.println("ピックアップガチャをタップ");
				my.single_tap_by_Location(reg_pickup_gacha.getTarget());
				my.sleep(300);
			}
			if (!breaked) {
				continue;
			}
			// OK表示待ち
			//			my.sleep(800);
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				if (Objects.nonNull(reg_ok_lower.exists(BS5Patterns.ok_090, WAIT_TIMEOUT))) {
					// OKを表示したら抜ける
					breaked = true;
					break;
				}
				my.println("OK表示待ち");
				my.sleep(100);
			}
			if (!breaked) {
				continue;
			}
			// OKをタップ
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				if (Objects.nonNull(reg_ok_lower.exists(BS5Patterns.ok_090, WAIT_TIMEOUT))) {
					my.println("OKをタップ");
					my.single_tap_by_Location(reg_ok_lower.getLastMatch().getTarget());
					my.sleep(100);
				} else {
					// OKを非表示したら抜ける
					breaked = true;
					break;
				}
			}
			if (!breaked) {
				continue;
			}
			// OK非表示待ち
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				if (Objects.isNull(reg_ok_lower.exists(BS5Patterns.ok_090, WAIT_TIMEOUT))) {
					// お金がなくなったら終了
					if (Objects.nonNull(reg_ruby_exchange.exists(ruby_exchange_090, WAIT_TIMEOUT))) {
						my.println("お金がなくなった");
						my.sleep(500);
						my.println("キャンセルをタップ");
						my.single_tap_by_Location(reg_cancel.getTarget());
						return;
					} else if (Objects.isNull(reg_close_center.exists(BS5Patterns.close_088, WAIT_TIMEOUT))) {
						breaked = true;
						break;
					}
				}
				my.println("OK非表示待ち");
				my.sleep(100);
			}
			if (!breaked) {
				continue;
			}
			my.sleep(800);
			// 真ん中とじる表示待ち
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				// お金がなくなったら終了
				if (Objects.nonNull(reg_ruby_exchange.exists(ruby_exchange_090, WAIT_TIMEOUT))) {
					my.println("お金がなくなった");
					my.sleep(500);
					my.println("キャンセルをタップ");
					my.single_tap_by_Location(reg_cancel.getTarget());
					return;
				}

				if (Objects.nonNull(reg_close_center.exists(BS5Patterns.close_088, WAIT_TIMEOUT))) {
					// 真ん中とじるを表示していたら抜ける
					breaked = true;
					break;
				}
				my.println("OKの位置をタップ");
				my.single_tap_by_Location(reg_ok_lower.getTarget());
				my.sleep(200);
			}
			if (!breaked) {
				continue;
			}
			// スクショを撮っておく
			mySS.take("ピックアップガチャを引いた");
			// 真ん中とじるをタップ
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				if (Objects.nonNull(reg_close_center.exists(BS5Patterns.close_088, WAIT_TIMEOUT))) {
					Match matches_happiness = reg_happiness_box.findBestList(Arrays.asList((Object[]) images_happiness_box));
					Match matches_premium = reg_premium_box.findBestList(Arrays.asList((Object[]) images_premium_box));
					if (Objects.nonNull(matches_happiness) && Objects.nonNull(matches_premium)) {
						// ストア画面に遷移していたら抜ける
						breaked = true;
						break;
					}

					my.println("真ん中とじるをタップ");
					my.single_tap_by_Location(reg_close_center.getLastMatch().getTarget());
					my.sleep(100);
				} else {
					// 真ん中とじるを非表示していたら抜ける
					breaked = true;
					break;
				}
			}
			if (!breaked) {
				continue;
			}

		}
	}

	/**
	 * セレクトBOXを引く.
	 */
	public static void roll_all_gacha_select() {
		// Region定義
		set_region();

		boolean breaked = false;

		// お金がなくなるまでセレクトBOXを引く
		LocalDateTime next_datetime_with_gacha = LocalDateTime.now();
		if (Objects.nonNull(BS5Status.get_next_datetime())) {
			next_datetime_with_gacha = BS5Status.get_next_datetime().plusMinutes(BS5Activity.OFFSET_MINUTES_RESTART);
		} else {
			next_datetime_with_gacha = LocalDateTime.now().plusSeconds(120);
		}
		while (next_datetime_with_gacha.isAfter(LocalDateTime.now())) {
			// リカバリ
			if (!breaked) {
				Match matches_happiness = reg_happiness_box.findBestList(Arrays.asList((Object[]) images_happiness_box));
				Match matches_premium = reg_premium_box.findBestList(Arrays.asList((Object[]) images_premium_box));
				if (Objects.nonNull(matches_happiness) && Objects.nonNull(matches_premium)) {
					// ストア画面に遷移していたら抜ける
					breaked = true;
					continue;
				}
				// 初回 or 迷子になったら、週間ランキングへ戻る
				BS5Activity.recovery_to_ranking(BS5Activity.LIMIT_SECONDS_RECOVERY_TO_RANKING);
				set_region();
			}

			// セレクトBOX表示待ち
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				Match matches_happiness = reg_happiness_box.findBestList(Arrays.asList((Object[]) images_happiness_box));
				Match matches_premium = reg_premium_box.findBestList(Arrays.asList((Object[]) images_premium_box));
				Match matches_select = reg_select_gacha.exists(select_box_080, WAIT_TIMEOUT);
				if (Objects.nonNull(matches_happiness) && Objects.nonNull(matches_premium)) {
					if (Objects.nonNull(matches_select)) {
						breaked = true;
						break;
					} else {
						// セレクトBOXの期間でない
						my.println("セレクトBOXの期間ではない");
						return;
					}
				}
				my.println("STOREをタップ");
				my.single_tap_by_Location(reg_store.getTarget());
				my.sleep(200);
			}
			if (!breaked) {
				continue;
			}

			// セレクトBOXをタップ
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				Match matches = reg_select_gacha.exists(select_box_080, WAIT_TIMEOUT);
				if (Objects.isNull(matches)) {
					breaked = true;
					break;
				}
				my.println("セレクトBOXをタップ");
				my.single_tap_by_Location(reg_select_gacha.getTarget());
				my.sleep(300);
			}
			if (!breaked) {
				continue;
			}
			// OK表示待ち
			//			my.sleep(800);
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				// カラフルツムがでたらフルコンプ→終了
				if (Objects.nonNull(colorful.exists(BS5Patterns.colorful_tsum_088, WAIT_TIMEOUT))) {
					my.println("フルコンプした");
					my.sleep(500);
					my.single_tap_by_Location(colorful.getTarget());
					return;
				}

				if (Objects.nonNull(reg_ok.exists(BS5Patterns.ok_090, WAIT_TIMEOUT))) {
					breaked = true;
					break;
				}
				my.println("OK表示待ち");
				my.sleep(100);
			}
			if (!breaked) {
				continue;
			}
			// OKをタップ
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				if (Objects.nonNull(reg_ok.exists(BS5Patterns.ok_090, WAIT_TIMEOUT))) {
					my.println("OKをタップ");
					my.single_tap_by_Location(reg_ok.getLastMatch().getTarget());
					my.sleep(100);
				} else {
					// OKを非表示したら抜ける
					breaked = true;
					break;
				}
			}
			if (!breaked) {
				continue;
			}
			// OK非表示待ち
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				if (Objects.isNull(reg_ok.exists(BS5Patterns.ok_090, WAIT_TIMEOUT))) {
					// お金がなくなったら終了
					if (Objects.nonNull(reg_ruby_exchange.exists(ruby_exchange_090, WAIT_TIMEOUT))) {
						my.println("お金がなくなった");
						my.sleep(500);
						my.println("キャンセルをタップ");
						my.single_tap_by_Location(reg_cancel.getTarget());
						return;
					} else if (Objects.isNull(reg_close_center.exists(BS5Patterns.close_088, WAIT_TIMEOUT))) {
						breaked = true;
						break;
					}
				}
				my.println("OK非表示待ち");
				my.sleep(100);
			}
			if (!breaked) {
				continue;
			}
			my.sleep(800);
			// 真ん中とじる表示待ち
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				// お金がなくなったら終了
				if (Objects.nonNull(reg_ruby_exchange.exists(ruby_exchange_090, WAIT_TIMEOUT))) {
					my.println("お金がなくなった");
					my.sleep(500);
					my.println("キャンセルをタップ");
					my.single_tap_by_Location(reg_cancel.getTarget());
					return;
				}

				if (Objects.nonNull(reg_close_center.exists(BS5Patterns.close_088, WAIT_TIMEOUT))) {
					// 真ん中とじるを表示していたら抜ける
					breaked = true;
					break;
				}
				my.println("OKの位置をタップ");
				my.single_tap_by_Location(reg_ok.getTarget());
				my.sleep(200);
			}
			if (!breaked) {
				continue;
			}
			// スクショを撮っておく
			mySS.take("セレクトBOXを引いた");
			// 真ん中とじるをタップ
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				if (Objects.nonNull(reg_close_center.exists(BS5Patterns.close_088, WAIT_TIMEOUT))) {
					Match matches_happiness = reg_happiness_box.findBestList(Arrays.asList((Object[]) images_happiness_box));
					Match matches_premium = reg_premium_box.findBestList(Arrays.asList((Object[]) images_premium_box));
					if (Objects.nonNull(matches_happiness) && Objects.nonNull(matches_premium)) {
						// ストア画面に遷移していたら抜ける
						breaked = true;
						break;
					}

					my.println("真ん中とじるをタップ");
					my.single_tap_by_Location(reg_close_center.getLastMatch().getTarget());
					my.sleep(100);
				} else {
					// 真ん中とじるを非表示していたら抜ける
					breaked = true;
					break;
				}
			}
			if (!breaked) {
				continue;
			}
		}
	}

	/**
	 * プレミアムBOXを引く.
	 */
	public static void roll_all_gacha_premium() {
		// Region定義
		set_region();

		boolean breaked = false;

		// お金がなくなるまでプレミアムBOXを引く
		LocalDateTime next_datetime_with_gacha = LocalDateTime.now();
		if (Objects.nonNull(BS5Status.get_next_datetime())) {
			next_datetime_with_gacha = BS5Status.get_next_datetime().plusMinutes(BS5Activity.OFFSET_MINUTES_RESTART);
		} else {
			next_datetime_with_gacha = LocalDateTime.now().plusSeconds(120);
		}
		while (next_datetime_with_gacha.isAfter(LocalDateTime.now())) {
			// リカバリ
			if (!breaked) {
				Match matches_happiness = reg_happiness_box.findBestList(Arrays.asList((Object[]) images_happiness_box));
				Match matches_premium = reg_premium_box.findBestList(Arrays.asList((Object[]) images_premium_box));
				if (Objects.nonNull(matches_happiness) && Objects.nonNull(matches_premium)) {
					// ストア画面に遷移していたら抜ける
					breaked = true;
					continue;
				}
				// 初回 or 迷子になったら、週間ランキングへ戻る
				BS5Activity.recovery_to_ranking(BS5Activity.LIMIT_SECONDS_RECOVERY_TO_RANKING);
				set_region();
			}

			// プレミアムBOX表示待ち
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				Match matches_happiness = reg_happiness_box.findBestList(Arrays.asList((Object[]) images_happiness_box));
				Match matches_premium = reg_premium_box.findBestList(Arrays.asList((Object[]) images_premium_box));
				if (Objects.nonNull(matches_happiness) && Objects.nonNull(matches_premium)) {
					breaked = true;
					break;
				}
				my.println("STOREをタップ");
				my.single_tap_by_Location(reg_store.getTarget());
				my.sleep(200);
			}
			if (!breaked) {
				continue;
			}

			// プレミアムBOXをタップ
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				Match matches = reg_premium_box.findBestList(Arrays.asList((Object[]) images_premium_box));
				if (Objects.isNull(matches)) {
					breaked = true;
					break;
				}
				my.println("プレミアムBOXをタップ");
				my.single_tap_by_Location(reg_premium_box.getTarget());
				my.sleep(300);
			}
			if (!breaked) {
				continue;
			}
			// OK表示待ち
			//			my.sleep(800);
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				// カラフルツムがでたらフルコンプ→終了
				if (Objects.nonNull(colorful.exists(BS5Patterns.colorful_tsum_088, WAIT_TIMEOUT))) {
					my.println("フルコンプした");
					my.sleep(500);
					my.single_tap_by_Location(colorful.getTarget());
					return;
				}

				if (Objects.nonNull(reg_ok.exists(BS5Patterns.ok_090, WAIT_TIMEOUT))) {
					breaked = true;
					break;
				}
				my.println("OK表示待ち");
				my.sleep(100);
			}
			if (!breaked) {
				continue;
			}
			// OKをタップ
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				if (Objects.nonNull(reg_ok.exists(BS5Patterns.ok_090, WAIT_TIMEOUT))) {
					my.println("OKをタップ");
					my.single_tap_by_Location(reg_ok.getLastMatch().getTarget());
					my.sleep(100);
				} else {
					// OKを非表示したら抜ける
					breaked = true;
					break;
				}
			}
			if (!breaked) {
				continue;
			}
			// OK非表示待ち
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				if (Objects.isNull(reg_ok.exists(BS5Patterns.ok_090, WAIT_TIMEOUT))) {
					// お金がなくなったら終了
					if (Objects.nonNull(reg_ruby_exchange.exists(ruby_exchange_090, WAIT_TIMEOUT))) {
						my.println("お金がなくなった");
						my.sleep(500);
						my.println("キャンセルをタップ");
						my.single_tap_by_Location(reg_cancel.getTarget());
						return;
					} else if (Objects.isNull(reg_close_center.exists(BS5Patterns.close_088, WAIT_TIMEOUT))) {
						breaked = true;
						break;
					}
				}
				my.println("OK非表示待ち");
				my.sleep(100);
			}
			if (!breaked) {
				continue;
			}
			my.sleep(800);
			// 真ん中とじる表示待ち
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				// お金がなくなったら終了
				if (Objects.nonNull(reg_ruby_exchange.exists(ruby_exchange_090, WAIT_TIMEOUT))) {
					my.println("お金がなくなった");
					my.sleep(500);
					my.println("キャンセルをタップ");
					my.single_tap_by_Location(reg_cancel.getTarget());
					return;
				}

				if (Objects.nonNull(reg_close_center.exists(BS5Patterns.close_088, WAIT_TIMEOUT))) {
					// 真ん中とじるを表示していたら抜ける
					breaked = true;
					break;
				}
				my.println("OKの位置をタップ");
				my.single_tap_by_Location(reg_ok.getTarget());
				my.sleep(200);
			}
			if (!breaked) {
				continue;
			}
			// スクショを撮っておく
			mySS.take("プレミアムBOXを引いた");
			// 真ん中とじるをタップ
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				if (Objects.nonNull(reg_close_center.exists(BS5Patterns.close_088, WAIT_TIMEOUT))) {
					Match matches_happiness = reg_happiness_box.findBestList(Arrays.asList((Object[]) images_happiness_box));
					Match matches_premium = reg_premium_box.findBestList(Arrays.asList((Object[]) images_premium_box));
					if (Objects.nonNull(matches_happiness) && Objects.nonNull(matches_premium)) {
						// ストア画面に遷移していたら抜ける
						breaked = true;
						break;
					}

					my.println("真ん中とじるをタップ");
					my.single_tap_by_Location(reg_close_center.getLastMatch().getTarget());
					my.sleep(100);
				} else {
					// 真ん中とじるを非表示していたら抜ける
					breaked = true;
					break;
				}
			}
			if (!breaked) {
				continue;
			}

		}
	}

	/**
	 * ハピネスBOXを引く.
	 */
	public static void roll_all_gacha_happiness() {
		// Region定義
		set_region();

		boolean breaked = false;

		// お金がなくなるまでハピネスBOXを引く
		LocalDateTime next_datetime_with_gacha = LocalDateTime.now();
		if (Objects.nonNull(BS5Status.get_next_datetime())) {
			next_datetime_with_gacha = BS5Status.get_next_datetime().plusMinutes(BS5Activity.OFFSET_MINUTES_RESTART);
		} else {
			next_datetime_with_gacha = LocalDateTime.now().plusSeconds(120);
		}
		while (next_datetime_with_gacha.isAfter(LocalDateTime.now())) {
			// リカバリ
			if (!breaked) {
				Match matches_happiness = reg_happiness_box.findBestList(Arrays.asList((Object[]) images_happiness_box));
				Match matches_premium = reg_premium_box.findBestList(Arrays.asList((Object[]) images_premium_box));
				if (Objects.nonNull(matches_happiness) && Objects.nonNull(matches_premium)) {
					// ストア画面に遷移していたら抜ける
					breaked = true;
					continue;
				}
				// 初回 or 迷子になったら、週間ランキングへ戻る
				BS5Activity.recovery_to_ranking(BS5Activity.LIMIT_SECONDS_RECOVERY_TO_RANKING);
				set_region();
			}

			// ハピネスBOX表示待ち
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				Match matches_happiness = reg_happiness_box.findBestList(Arrays.asList((Object[]) images_happiness_box));
				Match matches_premium = reg_premium_box.findBestList(Arrays.asList((Object[]) images_premium_box));
				if (Objects.nonNull(matches_happiness) && Objects.nonNull(matches_premium)) {
					breaked = true;
					break;
				}
				my.println("STOREをタップ");
				my.single_tap_by_Location(reg_store.getTarget());
				my.sleep(200);
			}
			if (!breaked) {
				continue;
			}

			// ハピネスBOXをタップ
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				Match matches = reg_happiness_box.findBestList(Arrays.asList((Object[]) images_premium_box));
				if (Objects.isNull(matches)) {
					breaked = true;
					break;
				}
				my.println("ハピネスBOXをタップ");
				my.single_tap_by_Location(reg_happiness_box.getTarget());
				my.sleep(300);
			}
			if (!breaked) {
				continue;
			}
			// OK表示待ち
			//			my.sleep(800);
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				// カラフルツムがでたらフルコンプ→終了
				if (Objects.nonNull(colorful.exists(BS5Patterns.colorful_tsum_088, WAIT_TIMEOUT))) {
					my.println("フルコンプした");
					my.sleep(500);
					my.single_tap_by_Location(colorful.getTarget());
					return;
				}

				if (Objects.nonNull(reg_ok.exists(BS5Patterns.ok_090, WAIT_TIMEOUT))) {
					breaked = true;
					break;
				}
				my.println("OK表示待ち");
				my.sleep(100);
			}
			if (!breaked) {
				continue;
			}
			// OKをタップ
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				if (Objects.nonNull(reg_ok.exists(BS5Patterns.ok_090, WAIT_TIMEOUT))) {
					my.println("OKをタップ");
					my.single_tap_by_Location(reg_ok.getLastMatch().getTarget());
					my.sleep(100);
				} else {
					// OKを非表示したら抜ける
					breaked = true;
					break;
				}
			}
			if (!breaked) {
				continue;
			}
			// OK非表示待ち
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				if (Objects.isNull(reg_ok.exists(BS5Patterns.ok_090, WAIT_TIMEOUT))) {
					// お金がなくなったら終了
					if (Objects.nonNull(reg_ruby_exchange.exists(ruby_exchange_090, WAIT_TIMEOUT))) {
						my.println("お金がなくなった");
						my.sleep(500);
						my.println("キャンセルをタップ");
						my.single_tap_by_Location(reg_cancel.getTarget());
						return;
					} else if (Objects.isNull(reg_close_center.exists(BS5Patterns.close_088, WAIT_TIMEOUT))) {
						breaked = true;
						break;
					}
				}
				my.println("OK非表示待ち");
				my.sleep(100);
			}
			if (!breaked) {
				continue;
			}
			my.sleep(800);
			// 真ん中とじる表示待ち
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				// お金がなくなったら終了
				if (Objects.nonNull(reg_ruby_exchange.exists(ruby_exchange_090, WAIT_TIMEOUT))) {
					my.println("お金がなくなった");
					my.sleep(500);
					my.println("キャンセルをタップ");
					my.single_tap_by_Location(reg_cancel.getTarget());
					return;
				}

				if (Objects.nonNull(reg_close_center.exists(BS5Patterns.close_088, WAIT_TIMEOUT))) {
					// 真ん中とじるを表示していたら抜ける
					breaked = true;
					break;
				}
				my.println("OKの位置をタップ");
				my.single_tap_by_Location(reg_ok.getTarget());
				my.sleep(200);
			}
			if (!breaked) {
				continue;
			}
			// スクショを撮っておく
			mySS.take("ハピネスBOXを引いた");
			// 真ん中とじるをタップ
			breaked = false;
			for (int i = 0; i < C_RETRY_EXISTS; i++) {
				if (Objects.nonNull(reg_close_center.exists(BS5Patterns.close_088, WAIT_TIMEOUT))) {
					Match matches_happiness = reg_happiness_box.findBestList(Arrays.asList((Object[]) images_happiness_box));
					Match matches_premium = reg_premium_box.findBestList(Arrays.asList((Object[]) images_premium_box));
					if (Objects.nonNull(matches_happiness) && Objects.nonNull(matches_premium)) {
						// ストア画面に遷移していたら抜ける
						breaked = true;
						break;
					}

					my.println("真ん中とじるをタップ");
					my.single_tap_by_Location(reg_close_center.getLastMatch().getTarget());
					my.sleep(100);
				} else {
					// 真ん中とじるを非表示していたら抜ける
					breaked = true;
					break;
				}
			}
			if (!breaked) {
				continue;
			}
		}
	}

	public static void config(ConfigureModel config) {
		WAIT_TIMEOUT = config.WAIT_TIMEOUT;
	}

	public static void test() {
		//		new Region(NoxRegions.tsum.x + 112, NoxRegions.tsum.y + 509, 209, 73).highlight(2);
		//		reg_happiness_box.highlight(1);
		//		reg_premium_box.highlight(1);
		//		reg_pickup_gacha.highlight(2);
		//		reg_ok_lower.highlight(1);
		//		close_upper.highlight(1);
		reg_close_center.highlight(2);
		//		reg_ruby_exchange.highlight(1);
		//		reg_cancel.highlight(1);
		//		reg_store.highlight(1);
		//		reg_select_gacha.highlight(1);
		//		reg_ok.highlight(1);
		//		colorful.highlight(1);
	}
}
