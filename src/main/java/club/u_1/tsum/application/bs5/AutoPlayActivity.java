/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application.bs5;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.sikuli.script.Location;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;
import org.sikuli.script.ScreenImage;

import club.u_1.tsum.application.ConfigureModel;
import club.u_1.tsum.application.RetryOverException;
import club.u_1.tsum.application.my;
import club.u_1.tsum.application.myLogger;
import club.u_1.tsum.application.mySS;

public class AutoPlayActivity {

	// ----- 設定値定義 -----
	public static double WAIT_TIMEOUT = 0.001;
	public static int RETRY_EXISTS = 50;

	//----- 静的フィールド -----
	static final int W_RANGE = 300;
	static final int H_RANGE = 300;
	static final int DIVIDED = 5;
	static final int INT_LIST[] = new int[DIVIDED];
	static int[][][] loc_pack = new int[][][] {
		{ { 53, 696 }, { 101, 686 }, { 126, 731 }, { 231, 755 }, },
		{ { 41, 546 }, { 63, 618 }, { 102, 672 }, { 165, 732 }, },
		{ { 53, 735 }, { 119, 713 }, { 145, 765 }, { 200, 713 }, { 269, 750 }, },
		{ { 262, 671 }, { 302, 605 }, { 332, 528 }, { 356, 449 }, },
		{ { 80, 504 }, { 143, 447 }, { 173, 374 }, { 236, 407 }, },
	};

	// 自前ツムプレイ
	public static void play_tsum_tsum() {

		for (int i = 0; i < INT_LIST.length; i++) {
			INT_LIST[i] = i;
		}

		// 週間ランキングへ戻る
		BS5Activity.recovery_to_ranking(60);

		long start_time = System.currentTimeMillis();
		long elapsed_time = start_time;
		Region reg_pause = new Region(BS5Regions.tsum.x + 439, BS5Regions.tsum.y + 150, 32, 32);
		Region reg_count = new Region(BS5Regions.tsum.x + 64, BS5Regions.tsum.y + 150, 32, 32);
		Location loc_shuffle = new Location(BS5Regions.tsum.x + 454, BS5Regions.tsum.y + 841);
		Location loc_skill1 = new Location(BS5Regions.tsum.x + 56, BS5Regions.tsum.y + 841);
		Location loc_skill2 = new Location(BS5Regions.tsum.x + 102, BS5Regions.tsum.y + 873);

		List<Location> locs = new ArrayList<Location>();

		boolean breaked = false;

		my.println("▼▼▼ TSUM TSUM PLAY STARTED  ▼▼▼");
		try {
			// プレイをタップ
			breaked = false;
			for (int i = 0; i < RETRY_EXISTS; i++) {
				if (Objects.nonNull(BS5Regions.retry.exists(BS5Patterns.retry_090, WAIT_TIMEOUT))) {
					myLogger.fine("リトライをタップ");
					my.single_tap_by_Location(BS5Regions.retry.getTarget());
					my.sleep(1000);
				} else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.play_090, WAIT_TIMEOUT))) {
					my.println("プレイをタップ");
					my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
					my.sleep(1000);
				} else {
					breaked = true;
					break;
				}
			}
			if (!breaked) {
				throw new RetryOverException("RETRY OVER プレイをタップ");
			}
			// スタートをタップ
			breaked = false;
			for (int i = 0; i < RETRY_EXISTS; i++) {
				if (BS5Instance.getNo() == 0) {
					if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.bs5_score_090, WAIT_TIMEOUT))) {
						myLogger.fine("+Scoreをタップ");
						my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
						my.sleep(1000);
					}
					if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.bs5_coin_090, WAIT_TIMEOUT))) {
						myLogger.fine("+Coinをタップ");
						my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
						my.sleep(1000);
					}
					if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.bs5_exp_090, WAIT_TIMEOUT))) {
						myLogger.fine("+Expをタップ");
						my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
						my.sleep(1000);
					}
					if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.bs5_time_090, WAIT_TIMEOUT))) {
						myLogger.fine("+Timeをタップ");
						my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
						my.sleep(1000);
					}
					if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.bs5_bomb_090, WAIT_TIMEOUT))) {
						myLogger.fine("+Bombをタップ");
						my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
						my.sleep(1000);
					}
					if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.bs5_5_4_090, WAIT_TIMEOUT))) {
						myLogger.fine("5▶4をタップ");
						my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
						my.sleep(1000);
					}
					if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.bs5_combo_090, WAIT_TIMEOUT))) {
						myLogger.fine("+Comboをタップ");
						my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
						my.sleep(1000);
					}
				}
				if (Objects.nonNull(BS5Regions.retry.exists(BS5Patterns.retry_090, WAIT_TIMEOUT))) {
					myLogger.fine("リトライをタップ");
					my.single_tap_by_Location(BS5Regions.retry.getTarget());
					my.sleep(1000);
				} else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.start_090, WAIT_TIMEOUT))) {
					my.println("スタートをタップ");
					my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
					my.sleep(1000);
				} else {
					breaked = true;
					break;
				}
			}
			if (!breaked) {
				throw new RetryOverException("RETRY OVER スタートをタップ");
			}

			LocalDateTime limit_datetime = LocalDateTime.now().plusSeconds(120);

			// スクリーンキャプチャ
			ScreenImage baseImage = mySS.take_for_tsum_clock(reg_count, "temp");
			Pattern basePattern = new Pattern(baseImage).exact();
			LocalDateTime ldt_for_check = LocalDateTime.now().plus(10, ChronoUnit.SECONDS);

			Match pause = reg_pause.wait(BS5Patterns.bs5_pause_on_090, 60);
			// タイムアウトを待ちながらプレイ終了待ち
			while (limit_datetime.isAfter(LocalDateTime.now())) {
				// フリーズチェック
				if (ldt_for_check.isBefore(LocalDateTime.now())) {
					if (Objects.nonNull(reg_count.exists(basePattern, 0))) {
						// フリーズ検出
						throw new RetryOverException("プレイ画面フリーズ疑惑");
					} else {
						// 更新
						baseImage = mySS.take_for_tsum_clock(reg_count, "temp");
						basePattern = new Pattern(baseImage).exact();
						ldt_for_check = LocalDateTime.now().plus(10, ChronoUnit.SECONDS);
					}
				}

				// サイズチェック
				Region reg_size = BS5App.focus();
				if (Objects.nonNull(reg_size)) {
					if ((reg_size.w != 568) || (reg_size.h != 983)) {
						BS5App.move_resize_window(0, 0, 568, 983);
					}
				}
				// プレイ終了
				if (Objects.nonNull(reg_count.exists(BS5Patterns.bs5_count0_090, WAIT_TIMEOUT))) {
					my.println("プレイ終了");
					breaked = true;
					break;
				}
				// プレイ画面
				else if (Objects.nonNull(reg_pause.exists(BS5Patterns.bs5_pause_on_090, WAIT_TIMEOUT))) {
					// int x = random.nextInt(reg_playarea.w) + reg_playarea.x; //random.randint(reg_playarea.x, reg_playarea.x + reg_playarea.w);
					// int y = random.nextInt(reg_playarea.h) + reg_playarea.y; //random.randint(reg_playarea.y, reg_playarea.y + reg_playarea.h);
					// my.println(String.format("reg_tsumtap(x,y)=(%1$d,%2$d)", x, y));
					// // 枠内へ座標補正
					// if ((reg_playarea.x + reg_playarea.w) < (x + C_W_RANGE)) {
					// 	x -= (x + C_W_RANGE) - (reg_playarea.x + reg_playarea.w);
					// }
					// if ((reg_playarea.y + reg_playarea.h) < (y + C_H_RANGE)) {
					// 	y -= (y + C_H_RANGE) - (reg_playarea.y + reg_playarea.h);
					// }
					// Region reg_tsumtap = new Region(x, y, C_W_RANGE, C_H_RANGE);
					// //random.sample(C_INT_LIST, C_DIVIDED)
					// int[] xl = new int[C_INT_LIST.length];
					// for (int xl_i = 0; xl_i < xl.length; xl_i++)
					// 	xl[xl_i] = C_INT_LIST[xl_i];
					// //C_INT_LIST[:]
					// int[] yl = new int[C_INT_LIST.length];
					// for (int yl_i = 0; yl_i < yl.length; yl_i++)
					// 	yl[yl_i] = C_INT_LIST[(C_INT_LIST.length - 1) - yl_i];
					// locs.clear();
					// for (int l = 0; l < C_DIVIDED; l++) {
					// 	locs.add(new Location(
					// 		reg_tsumtap.x + ((int) (reg_tsumtap.w / C_DIVIDED) * xl[l]),
					// 		reg_tsumtap.y + ((int) (reg_tsumtap.h / C_DIVIDED) * yl[l])));
					// }

					// my.swipe_by_Locations_for_play_tsum(locs);
					// my.sleep(100);
					// if ((i % 8) == 1) {
					// 	for (int p = 0; p < 2; p++) {
					// 		my.single_tap_by_Location(loc_shuffle);
					// 		my.sleep(200);
					// 	}
					// 	my.single_tap_by_Location(loc_skill);
					// }

					for (int[][] loc_list : loc_pack) {
						locs.clear();
						for (int[] loc_xy : loc_list) {
							locs.add(new Location(loc_xy[0], loc_xy[1]));
						}
						my.swipe_by_Locations_for_play_tsum(locs);
					}
					for (int[][] loc_list : loc_pack) {
						locs.clear();
						for (int[] loc_xy : loc_list) {
							locs.add(new Location(loc_xy[0] + 100, loc_xy[1] - 100));
						}
						my.swipe_by_Locations_for_play_tsum(locs);
					}
					for (int p = 0; p < 2; p++) {
						my.single_tap_by_Location(loc_shuffle);
						my.sleep(200);
					}
					my.single_tap_by_Location(loc_skill1);
					my.single_tap_by_Location(loc_skill2);
				}
			}
			if (!breaked) {
				throw new RetryOverException("RETRY OVER プレイ終了待ち");
			}

			my.sleep(2000);
			// マジカルタイム
			if (Objects.nonNull(BS5Regions.cancel_middle.exists(BS5Patterns.cancel_088, 1))) {
				my.println("マジカルタイム…キャンセル");
				my.single_tap_by_Location(BS5Regions.cancel_middle.getLastMatch().getTarget());
			}
			// プレイ終了
			for (int i = 0; i < RETRY_EXISTS; i++) {
				if (Objects.isNull(BS5Regions.tsum.exists(BS5Patterns.bs5_count0_090, WAIT_TIMEOUT))) {
					my.println("スコア画面");
					break;
				}
				my.sleep(500);
			}
			// 週間ランキングへ戻る
			BS5Activity.recovery_to_ranking(60);
		} catch (Exception e) {
			// 例外発生時
			// スタックトレース出力
			myLogger.error(e);
			mySS.take("AutoPlay Exception");
			// ツム再起動
			BS5Activity.stop_and_create(BS5Activity.LIMIT_SECONDS_RESTART);
		}
		elapsed_time = System.currentTimeMillis() - start_time;
		my.println("▲▲▲ TSUM TSUM PLAY FINISHED ▲▲▲");
		my.println(String.format("    elapsed time：%1$d (ms)", elapsed_time));
	}

	public static void config(ConfigureModel config) {
		WAIT_TIMEOUT = config.WAIT_TIMEOUT;
		RETRY_EXISTS = config.RETRY_EXISTS;
	}
}
