/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application.bs5;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.sikuli.script.App;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Location;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;

import club.u_1.tsum.application.ConfigureModel;
import club.u_1.tsum.application.HeartSendState;
import club.u_1.tsum.application.LineNotify;
import club.u_1.tsum.application.RetryOverException;
import club.u_1.tsum.application.my;
import club.u_1.tsum.application.myDateTime;
import club.u_1.tsum.application.myIO;
import club.u_1.tsum.application.myLogger;
import club.u_1.tsum.application.mySS;

public class BS5Activity implements Runnable {

	// ----- 設定値定義 -----
	public static double WAIT_TIMEOUT = 0.001;
	public static double WAIT_TIMEOUT_FOR_RECOVERY = 0.100;
	public static int LAUNCH_INTERVAL_HOURS = 1;
	public static int LAUNCH_INTERVAL_MINUTES = 0;
	public static int LAUNCH_INTERVAL_SECONDS = -20;
	public static long TAP_INTERVAL_MILLIS = 200;
	public static int OFFSET_MINUTES_RESTART = -4;
	public static int OFFSET_MINUTES_GILLS_CREATE = -5;
	public static int OFFSET_MINUTES_FOR_MONDAY = -4;
	public static int RETRY_PRE_PROCESS = 2;
	public static int RETRY_POST_PROCESS = 2;
	public static int RETRY_CONNECT_ADB = 1;
	public static int RETRY_SEND_HEARTS_ALL = 10;
	public static int RETRY_EXISTS = 50;
	public static int RETRY_EXISTS_NUMBER_ON_MICKEY = 3;
	public static int RETRY_EXISTS_WITH_COMMUNICATION = 500;
	public static int RETRY_EXISTS_UPPER_CLOSE = 42;
	public static int RETRY_EXISTS_OK = 36;
	public static int RETRY_EXISTS_HIGH_SCORE = 200;
	public static int RETRY_DISAPPEAR_HIGH_SCORE = 50;
	public static int RETRY_TAP_HIGH_SCORE = 50;
	public static int RETRY_TAP_UPPER_CLOSE = 50;
	public static int RETRY_TAP_HEART = 50;
	public static int RETRY_TAP_HEART_PRESENT_OK = 50;
	public static int RETRY_SEEK = 30;
	public static long LIMIT_SECONDS_RESTART = 240;
	public static long LIMIT_SECONDS_START = 90;
	public static long LIMIT_SECONDS_GIVE_A_HEART = 10;
	public static long LIMIT_SECONDS_TAKE_ALL_HEART = 10;
	public static long LIMIT_SECONDS_EXISTS_HEART = 3600;
	public static long LIMIT_SECONDS_RECOVERY_TO_RANKING = 30;
	public static long LIMIT_SECONDS_ROOT_EXIST = 30;
	public static long LIMIT_SECONDS_PLAY_ROBOTMON = 600;
	public static long LIMIT_SECONDS_LOGIN_LINE = 60;
	public static long AFTER_SECONDS_POPS_UP_HEART = 3610;
	public static long LIMIT_SECONDS_POPS_UP_HEART = 3660;
	public static long LIMIT_SECONDS_FREEZED = 12;
	public static Integer[] SYSTEM_REBOOT_SCHEDULE_HOUR = new Integer[] { 0, 6, 12, 18 };

	public static String DATETIME_ALIVE = "";
	public static String DATETIME_NEXT = "";

	public static String PATH_LONG_CLOSE_HIGHSCORE = "";

	public static int OFFSET_X_FROM_LEADING_EDGE_OF_RANK_LIST = 222;

	public static String LINE_API_TOKEN_ALERT = "";
	public static String LINE_API_TOKEN_NOTIFICATION = "";

	// ----- 静的フィールド -----
	public static int count_hearts = 0;
	public static List<Integer> number_rank_list_cache = null;
	public static long min_close_highscore_ms = 300;

	/**
	 * [with Stalker] メインループ.
	 * InstanceNoを指定して動作するメインループ
	 */
	@Override
	public void run() {
		// メインループ
		while (true) {
			for (int i = 0; i < BS5Instance.instances.size(); i++) {
				// 次回送信日時を比較して小さいInstanceNoを選択
				int no = BS5Instance.getNextNo();
				my.println(String.format("no=%1$d", no));
				BS5Instance.setNo(no);
				try {
					// メインループ実装
					run_implementation();
				} catch (Exception e) {
					// 例外発生時
					// スタックトレース出力
					myLogger.error(e);
				}
			}

			// システム再起動スケジュールチェック
			Integer hour = BS5Status.get_first_send_datetime().getHour();
			if (Arrays.asList(SYSTEM_REBOOT_SCHEDULE_HOUR).contains(hour)) {
				bs_exit(60);
				my.reboot_system();
			}

		}

	}

	/**
	 * メインループ実装.
	 */
	public void run_implementation() {
		// ローカル変数定義
		Set<Integer> hearts_unsent = new LinkedHashSet<Integer>();
		LocalDateTime next_datetime_with_tsum_play = LocalDateTime.MAX;
		LocalDateTime next_datetime = LocalDateTime.MAX;

		// ハイスコア非表示時間(ms)を復元
		Long valuLong = myIO.<Long> read(Paths.get(PATH_LONG_CLOSE_HIGHSCORE), Long.class);
		if (Objects.nonNull(valuLong)) {
			min_close_highscore_ms = valuLong.longValue();
			myLogger.info("ハイスコア非表示時間(ms)を復元");
		}
		myLogger.info(String.format("min close highscore ms = %1$d",
			min_close_highscore_ms));

		// 次回実行日時を復元
		next_datetime = myDateTime.get_datetime(BS5Instance.get_datetime_next());
		if (Objects.isNull(next_datetime)) {
			// 復元に失敗したら、即実行
			next_datetime = LocalDateTime.now();
		} else {
			myLogger.info("次回実行日時を復元");
		}
		BS5Status.set_next_datetime(next_datetime);
		myLogger.info(String.format("next datetime = %1$s",
			myDateTime.formatter.format(BS5Status.get_next_datetime())));
		// ファイル日時の設定
		myDateTime.set_FileDateTime(BS5Status.get_next_datetime());
		// 最終送信日時を復元
		BS5Status.set_last_time_last_send_datetime(
			myDateTime.get_datetime(BS5Instance.get_datetime_last_time_last_send()));
		if (Objects.nonNull(BS5Status.get_last_time_last_send_datetime())) {
			myLogger.info("最終送信日時を復元");
			myLogger.info(String.format("last sent datetime = %1$s",
				myDateTime.formatter.format(BS5Status.get_last_time_last_send_datetime())));
		}
		// 全メンバー情報ストア日時を復元
		BS5Status.store_members_list_datetime = myDateTime.get_datetime(BS5Instance.get_datetime_store_members_list());
		if (Objects.nonNull(BS5Status.store_members_list_datetime)) {
			myLogger.info("全メンバー情報ストア日時を復元");
			myLogger.info(String.format("store members list datetime = %1$s",
				myDateTime.formatter.format(BS5Status.store_members_list_datetime)));
		}
		// 全メンバー情報を復元
		GillsActivity.load();
		myLogger.info("全メンバー情報を復元");

		// 生存通知
		myDateTime.set_datetime(LocalDateTime.now(), DATETIME_ALIVE);

		// エミュセット
		if (BS5App.is_running(1)) {
			Region reg_tsum = Screen.getPrimaryScreen();
			// ウィンドウ移動&リサイズ
			reg_tsum = BS5App.move_resize_window(0, 0, 568, 983);
			// Regionセット
			BS5Regions.set_region(reg_tsum);
		} else {
			// エミュ強制終了・・・異なるインスタンスが起動されている可能性があるため
			bs_force_exit(LIMIT_SECONDS_RESTART);
			// エミュ再起動
			bs_restart(LIMIT_SECONDS_RESTART);
		}

		// 生存通知
		myDateTime.set_datetime(LocalDateTime.now(), DATETIME_ALIVE);

		// 余興を楽しませてもらおう
		next_datetime_with_tsum_play = BS5Status.get_next_datetime().plusMinutes(OFFSET_MINUTES_RESTART).plusMinutes(OFFSET_MINUTES_GILLS_CREATE);
		if (next_datetime_with_tsum_play.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
			next_datetime_with_tsum_play = next_datetime_with_tsum_play.plusMinutes(OFFSET_MINUTES_FOR_MONDAY);
		}
		if (next_datetime_with_tsum_play.isAfter(LocalDateTime.now())) {
			// プレミアムボックス・セレクトボックス・ピックアップガチャ
			GachaActivity.roll_all_gacha();
		}
		if (next_datetime_with_tsum_play.isAfter(LocalDateTime.now())) {
			// オートプレイ
			AutoPlayActivity.play_tsum_tsum();
		}

		// if (BS5Instance.getNo() != 0) {
		while (next_datetime_with_tsum_play.isAfter(LocalDateTime.now())) {
			for (int i = 0; i < 10; i++) {
				my.mouse_move(new Location(1, 1));
				my.sleep(1000);
			}

			myLogger.fine(String.format("play datetime = %1$s",
				myDateTime.formatter.format(next_datetime_with_tsum_play)));
			myLogger.fine(String.format("next datetime = %1$s",
				myDateTime.formatter.format(BS5Status.get_next_datetime())));
			myLogger.fine(String.format("last sent datetime = %1$s",
				myDateTime.formatter.format(BS5Status.get_last_time_last_send_datetime())));
			// 生存通知
			myDateTime.set_datetime(LocalDateTime.now(), DATETIME_ALIVE);

			if (next_datetime_with_tsum_play.isAfter(LocalDateTime.now())) {
				// オートプレイ
				AutoPlayActivity.play_tsum_tsum();
			}
		}
		// }
		// 余興は終わりだ

		// ファイル日時の設定
		myDateTime.set_FileDateTime(BS5Status.get_next_datetime());

		// ハート送信前処理
		myLogger.info_members("♣︎♣︎♣︎ START ♣︎♣︎♣︎");
		pre_process();
		hearts_unsent = BS5Status.get_hearts_unsent();
		myLogger.flush();

		// 開始待ち
		{
			boolean last_set_datetime = false;
			while (BS5Status.get_next_datetime().isAfter(LocalDateTime.now())) {
				my.sleep(1);
				// 生存通知
				if (LocalDateTime.now().getSecond() == 0) {
					if (!last_set_datetime) {
						myDateTime.set_datetime(LocalDateTime.now(), DATETIME_ALIVE);
						last_set_datetime = true;
					}
				} else {
					last_set_datetime = false;
				}
			}
		}

		// エミュフォーカスセット
		BS5App.focus();
		// 生存通知
		myDateTime.set_datetime(LocalDateTime.now(), DATETIME_ALIVE);
		// 実行日時セット
		LocalDateTime started_datetime = LocalDateTime.now();
		// 仮の次回実行日時セット
		BS5Status.set_next_datetime(started_datetime
			.plusHours(LAUNCH_INTERVAL_HOURS)
			.plusMinutes(LAUNCH_INTERVAL_MINUTES)
			.plusSeconds(LAUNCH_INTERVAL_SECONDS));
		// 初期化
		BS5Status.set_last_time_first_send_datetime(BS5Status.get_first_send_datetime());
		BS5Status.set_first_send_datetime(null);
		count_hearts = 0;
		BS5Status.set_extended_delay(false);

		myLogger.info("♥♥♥ START ♥♥♥");
		myLogger.flush();
		// ハート送信ループ
		int retry = 0;
		LocalDateTime limit_datetime_for_all = LocalDateTime.now().plusSeconds(LIMIT_SECONDS_EXISTS_HEART);
		if (Objects.nonNull(BS5Status.get_last_time_last_send_datetime())) {
			limit_datetime_for_all = BS5Status.get_last_time_last_send_datetime()
				.plusSeconds(LIMIT_SECONDS_POPS_UP_HEART);
			if (limit_datetime_for_all.isBefore(LocalDateTime.now())) {
				// タイムアウト時刻が不正の場合、現在時刻から補正する
				LocalDateTime.now().plusSeconds(LIMIT_SECONDS_EXISTS_HEART);
			}
		}
		while (limit_datetime_for_all.isAfter(LocalDateTime.now())) {
			try {
				recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);

				// ハート送信開始
				give_all_hearts(hearts_unsent);
				// 最終送信日時を記憶
				if (Objects.nonNull(BS5Status.get_last_send_datetime())) {
					BS5Status.set_last_time_last_send_datetime(BS5Status.get_last_send_datetime());
				}
			} catch (RetryOverException e) {
				// リトライオーバー発生時
				// スタックトレース出力
				e.printStackTrace();
				mySS.take_screen("RETRY_OVER_heart_send");
				// ツム再起動
				stop_and_create(LIMIT_SECONDS_RESTART);
				// ループを抜けず、以降リトライ
			} catch (Exception e) {
				// その他例外発生時
				// スタックトレース出力
				e.printStackTrace();
				mySS.take_screen("EXCEPTION_heart_send");
				// ツム再起動
				stop_and_create(LIMIT_SECONDS_RESTART);
				// ループを抜けず、以降リトライ
			}
			if (hearts_unsent.size() == 0) {
				// ♥を送り終わったら終了
				break;
			}
			////////// 以降がループを抜けずリトライ時の処理
			// 自身の位置取得
			int new_number_of_my_rank = to_my_ranking();
			BS5Status.set_number_of_my_rank(new_number_of_my_rank);
			// レジューム位置シーク
			int number_of_next_rank = BS5Status.get_number_of_members();
			Iterator<Integer> iterator_integer = hearts_unsent.iterator();
			if (iterator_integer.hasNext()) {
				number_of_next_rank = iterator_integer.next().intValue();
			}
			myLogger.info(String.format("再開位置 %1$d位", number_of_next_rank));
			try {
				seek_resume_position(number_of_next_rank);
			} catch (RetryOverException e) {
				e.printStackTrace();
			}
			myLogger.fine(String.format("main リトライ:%1$d/%2$d, heartsUnsent = %3$s",
				retry++, RETRY_SEND_HEARTS_ALL, Arrays.toString(hearts_unsent.toArray())));
		}
		// 全メンバー情報を保存
		GillsActivity.store();
		// 全メンバー情報ストア日時を設定
		myDateTime.set_datetime(LocalDateTime.now(), BS5Instance.get_datetime_store_members_list());
		// first_send_datetime が未設定だったら started_datetime を使う
		if (Objects.isNull(BS5Status.get_first_send_datetime())) {
			BS5Status.set_first_send_datetime(started_datetime);
		}
		myLogger.info("♥♥♥ FINISH ♥♥♥");
		myLogger.flush();
		Duration elapsed_time = Duration.between(LocalDateTime.now(), BS5Status.get_first_send_datetime()).abs();
		myLogger.info(String.format("elapsed time = %02d:%02d.%03d",
			elapsed_time.getSeconds() / 60,
			elapsed_time.getSeconds() % 60,
			elapsed_time.toMillis() % 1000));
		// next_datetime を最初に♥送信した時刻を基に再設定
		myLogger.info(String.format("first sent datetime = %1$s",
			myDateTime.formatter.format(BS5Status.get_first_send_datetime())));
		BS5Status.set_next_datetime(BS5Status.get_first_send_datetime()
			.plusHours(LAUNCH_INTERVAL_HOURS)
			.plusMinutes(LAUNCH_INTERVAL_MINUTES)
			.plusSeconds(LAUNCH_INTERVAL_SECONDS));
		myDateTime.set_datetime(BS5Status.get_next_datetime(), BS5Instance.get_datetime_next());
		myDateTime.set_datetime(BS5Status.get_next_datetime(), DATETIME_NEXT);
		myLogger.info(String.format("next datetime = %1$s",
			myDateTime.formatter.format(BS5Status.get_next_datetime())));
		// 最終送信日時を設定
		myDateTime.set_datetime(BS5Status.get_last_time_last_send_datetime(),
			BS5Instance.get_datetime_last_time_last_send());
		myLogger.info(String.format("last sent datetime = %1$s",
			myDateTime.formatter.format(BS5Status.get_last_time_last_send_datetime())));
		// 多い日も安心
		try {
			recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);
			hearts_unsent = GillsActivity.get_hearts_send_queue();
			follow_up_missing_hearts(hearts_unsent);
			recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);
			// 最終送信日時を記憶
			if (Objects.nonNull(BS5Status.get_last_send_datetime())) {
				BS5Status.set_last_time_last_send_datetime(BS5Status.get_last_send_datetime());
			}
		} catch (Exception e) {
			// 例外発生時
			// スタックトレース出力
			e.printStackTrace();
			mySS.take("follow_up_E");
			// ツム再起動
			stop_and_create(LIMIT_SECONDS_RESTART);
		}
		// 全メンバー情報を保存
		GillsActivity.store();
		// 全メンバー情報ストア日時を設定
		myDateTime.set_datetime(LocalDateTime.now(), BS5Instance.get_datetime_store_members_list());
		myLogger.fine(String.format("next datetime = %1$s",
			myDateTime.formatter.format(BS5Status.get_next_datetime())));
		// 最終送信日時を再設定
		myDateTime.set_datetime(BS5Status.get_last_time_last_send_datetime(),
			BS5Instance.get_datetime_last_time_last_send());
		myLogger.info(String.format("last sent datetime = %1$s",
			myDateTime.formatter.format(BS5Status.get_last_time_last_send_datetime())));

		// デバッグ用
		myLogger.info_members("♣︎♣︎♣︎ FINISH ♣︎♣︎♣︎");
		myLogger.info_members(String.format("members_list InstanceNo=%1$d", BS5Instance.getNo()));
		GillsActivity.members_list.values().stream()
			.sorted(GillsActivity.comparator)
			.forEach(mm -> {
				myLogger.info_members(String.format("%1$d位, %2$s, 同分回数=%3$d, 同秒回数=%4$d",
					mm.rankNumber,
					myDateTime.formatter.format(mm.lastSendDatetime),
					mm.timesOfSameMin,
					mm.timesOfSameSec));
			});
		// 整理整頓
		GillsActivity.cleaning();

		myLogger.fine("全ての♥送信を終了しました");
		myLogger.flush();
		try {
			LineNotify lineNotify = new LineNotify(LINE_API_TOKEN_NOTIFICATION);
			lineNotify.notify("\n全ての♥送信を終了しました。\nTsum" + (BS5Instance.getNo() + 1));
		} catch (Exception e) {
			// 例外発生時
			// スタックトレース出力
			myLogger.error(e);
		}

		// エミュ停止
		bs_exit(LIMIT_SECONDS_RESTART);

		for (int i = 0; i < 20; i++) {
			my.sleep(1000);
		}
	}

	/**
	 * ホーム画面へ行って帰る.
	 */
	public static void to_home_and_return() {
		Match m = null;
		// ツムツムが表示されるまでホームボタンタップ
		while (Objects.isNull(BS5Regions.tsum.exists(BS5Patterns.bs5_gamecenter_088, WAIT_TIMEOUT))) {
			// ADB経由ホームキーイベント送信
			// my.println("ADB経由ホームキーイベント送信");
			ADBWrapper.sendkey_home();
			my.sleep(1000);
		}
		m = BS5Regions.tsum.getLastMatch();
		// ツムツムが消えるまでツムツムタップ
		while (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.bs5_gamecenter_088, WAIT_TIMEOUT))) {
			// ADB経由ツムツム起動
			// my.println("ADB経由ツムツム起動");
			ADBWrapper.start_TsumTsum();
			my.sleep(2000);
		}
		// 週間ランキングが表示されるまで待つ
		for (int i = 0; i < RETRY_EXISTS_WITH_COMMUNICATION; i++) {
			if (Objects.nonNull(BS5Regions.title.exists(BS5Patterns.title_weekly_ranking_094, WAIT_TIMEOUT))) {
				break;
			}
		}
	}

	/**
	 * 輝度を判定する.
	 * 
	 * @param reg リージョン
	 * @return true:明るい or false:暗い
	 */
	public static boolean is_brightness(Region reg) {
		return is_brightness(reg, 0.5f);
	}

	/**
	 * [基準値指定]輝度を判定する.
	 * 
	 * @param reg       リージョン
	 * @param reference 基準値
	 * @return true:明るい or false:暗い
	 */
	public static boolean is_brightness(Region reg, float reference) {
		// 左上の1ドットの色を取得し、0.5より上かを判定する
		Color color_TopLeft = reg.getTopLeft().getColor();
		int[] rgb = new int[] {
			color_TopLeft.getRed(),
			color_TopLeft.getGreen(),
			color_TopLeft.getBlue(),
		};
		float[] hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], null);
		my.println(String.format("hsb bright %1$f", hsb[2]));
		return (hsb[2] > reference);
	}

	/**
	 * ルート化を検出「許可する」タップ.
	 * 
	 * @param limit_seconds 制限時間(s)
	 */
	public static void permit_root_exist(long limit_seconds) {
		if (BS5Status.is_root_exist_popup()) {
			return;
		}

		// タイムアウト時刻設定
		LocalDateTime limit_datetime = LocalDateTime.now().plusSeconds(limit_seconds);

		boolean breaked = false;

		// タイムアウトを待ちながらルート化を検出の表示待ち→「許可する」タップ
		while (limit_datetime.isAfter(LocalDateTime.now())) {
			try {
				if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.root_exist_088, WAIT_TIMEOUT))) {
					myLogger.fine("ルート化を検出黒通知：許可するをタップ");
					my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
					BS5Status.set_root_exist_popup(true);
					breaked = true;
					break;
				}
				my.sleep(100);
			} catch (Exception e) {
				// スタックトレース出力
				e.printStackTrace();
			}
		}
		if (!breaked) {
			myLogger.warn("タイムアウト：ルート化を検出「許可する」タップ");
		}
	}

	/**
	 * リカバリ to 週間ランキング表示.
	 * <p>
	 * ツムツムアプリ終了から起動を許可する
	 * 
	 * @param limit_seconds 制限時間(s)
	 */
	public static void recovery_to_ranking(long limit_seconds) {
		try {
			recovery_to_ranking(limit_seconds, true);
		} catch (Exception e) {
			// スタックトレース出力
			e.printStackTrace();
			myLogger.warn("あり得ない!!");
			mySS.take_screen("あり得ない!!");
		}
	}

	/**
	 * リカバリ to 週間ランキング表示.
	 * 
	 * @param limit_seconds         制限時間(s)
	 * @param allow_stop_and_create ツムツムアプリ終了から起動を許可
	 * @throws RetryOverException リトライオーバー
	 */
	public static void recovery_to_ranking(long limit_seconds, boolean allow_stop_and_create)
		throws RetryOverException {
		// タイムアウト時刻設定
		LocalDateTime limit_datetime = LocalDateTime.now().plusSeconds(limit_seconds);
		// フラグ初期化
		int is_appeared = 0;
		int general_taps = 0;
		boolean is_appeared_home = false;
		boolean is_bright = false;
		// タイムアウトを待ちながらランキング画面への復帰を試みる
		boolean breaked = false;
		while (limit_datetime.isAfter(LocalDateTime.now())) {
			// BS5Regions.title.highlight(2);
			// ランキング画面に戻れたかチェック
			if (Objects.nonNull(BS5Regions.title.exists(BS5Patterns.title_weekly_ranking_094, WAIT_TIMEOUT_FOR_RECOVERY))) {
				// 左上の1ドットの色を取得し、0.5より上かを判定する
				is_bright = is_brightness(BS5Regions.title.getLastMatch());

				if ((is_appeared > 0) && is_bright) {
					// 何かしたあとは、少し時間を空けて2度読みしてみる
					my.sleep(800);
					if (Objects.nonNull(BS5Regions.title.exists(BS5Patterns.title_weekly_ranking_094, WAIT_TIMEOUT))) {
						// 左上の1ドットの色を取得し、0.5より上かを判定する
						is_bright = is_brightness(BS5Regions.title.getLastMatch());

						if (is_bright) {
							breaked = true;
							break;
						}
					}
				} else {
					if (is_bright) {
						breaked = true;
						break;
					}
				}
			}

			// ランキング表示されていなければ、いろいろやってみる
			is_appeared = 0;
			// BS5アップデート：閉じる
			if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.bs5_updater_close_090, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.warn("BS5アップデート：×をタップ");
				mySS.take("BS5アップデート");
				my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
			}
			// BS5エンジン再起動：閉じる
			else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.bs5_engine_reboot_090, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.warn("BS5エンジン再起動をタップ");
				mySS.take("BS5エンジン再起動");
				my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
			}
			// BS5おすすめアプリ：閉じる
			else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.bs5_recommends_close_090, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.warn("BS5おすすめアプリ：×をタップ");
				mySS.take("BS5おすすめアプリ");
				my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
			}
			// BS5パフォーマンスに関する警告：続行する
			else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.bs5_continue_090, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.warn("BS5パフォーマンスに関する警告：続行する");
				mySS.take("BS5パフォーマンスに関する警告");
				my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
			}
			// BS5メモリの低下：OK
			else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.bs5_low_memory_ok_090, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.warn("BS5メモリの低下：OK");
				mySS.take("BS5メモリの低下");
				my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
			}
			// Android：問題が発生したため…を終了します：OK
			else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.android_app_abort_ok_090, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.warn("Android：問題が発生したため…を終了します：OKをタップ");
				my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
			}
			// ルート化を検出黒通知：許可する
			else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.root_exist_088, WAIT_TIMEOUT))) {
				is_appeared += 1;
				if (is_appeared_home) {
					// ホーム画面で'ツムツム'をタップしていたら、
					// ツムツム再起動とみなす
					is_appeared_home = false;
					BS5Status.set_root_exist_popup(false);
				}
				myLogger.fine("ルート化を検出黒通知：許可するをタップ");
				my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
				BS5Status.set_root_exist_popup(true);
			}
			// 3つボタン黒通知
			else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.notice_popup_3buttons_088, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.fine("3つボタン黒通知をタップ");
				my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
			}
			// 2つボタン黒通知
			else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.notice_popup_2buttons_088, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.fine("2つボタン黒通知をタップ");
				my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
			}
			// OK
			else if (Objects.nonNull(BS5Regions.ok_upper.exists(BS5Patterns.ok_090, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.fine("OKをタップ");
				my.single_tap_by_Location(BS5Regions.ok_upper.getLastMatch().getTarget());
			}
			// カラフルツム表示→週間
			else if (Objects.nonNull(BS5Regions.colorful.exists(BS5Patterns.colorful_tsum_088, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.fine("カラフルツム表示→週間をタップ");
				my.single_tap_by_Location(BS5Regions.title.getTarget());
				// ♥を送信した日時を更新
				BS5Status.update_send_datetime(null, null, HeartSendState.COLOURFUL_TSUM);
			}
			// リトライ
			else if (Objects.nonNull(BS5Regions.retry.exists(BS5Patterns.retry_090, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.fine("リトライをタップ");
				my.single_tap_by_Location(BS5Regions.retry.getTarget());
			}
			// 受け取らない
			else if (Objects
				.nonNull(BS5Regions.not_accept_push.exists(BS5Patterns.not_accept_push_088, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.fine("受け取らないをタップ");
				my.single_tap_by_Location(BS5Regions.not_accept_push.getTarget());
			}
			// 上段キャンセル
			else if (Objects.nonNull(BS5Regions.cancel_upper.exists(BS5Patterns.cancel_088, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.fine("上段キャンセルをタップ");
				my.single_tap_by_Location(BS5Regions.cancel_upper.getTarget());
			}
			// 中段キャンセル
			else if (Objects.nonNull(BS5Regions.cancel_middle.exists(BS5Patterns.cancel_088, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.fine("中段キャンセルをタップ");
				my.single_tap_by_Location(BS5Regions.cancel_middle.getLastMatch().getTarget());
			}
			// 下段キャンセル
			else if (Objects.nonNull(BS5Regions.cancel_lower.exists(BS5Patterns.cancel_088, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.fine("下段キャンセルをタップ");
				my.single_tap_by_Location(BS5Regions.cancel_lower.getTarget());
			}
			// 上段とじる
			else if (Objects.nonNull(BS5Regions.close_upper.exists(BS5Patterns.close_088, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.fine("上段とじるをタップ");
				my.single_tap_by_Location(BS5Regions.close_upper.getTarget());
			}
			// 上段左とじる
			else if (Objects.nonNull(BS5Regions.close_upper_left.exists(BS5Patterns.close_088, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.fine("上段左とじるをタップ");
				my.single_tap_by_Location(BS5Regions.close_upper_left.getTarget());
			}
			// 中段とじる
			else if (Objects.nonNull(BS5Regions.close_middle.exists(BS5Patterns.close_088, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.fine("中段とじるをタップ");
				my.single_tap_by_Location(BS5Regions.close_middle.getTarget());
			}
			// 中段左とじる
			else if (Objects.nonNull(BS5Regions.close_middle_left.exists(BS5Patterns.close_088, WAIT_TIMEOUT))) {
				is_appeared += 1;
				my.println("中段左とじるをタップ");
				my.single_tap_by_Location(BS5Regions.close_middle_left.getTarget());
			}
			// 下段とじる
			else if (Objects.nonNull(BS5Regions.close_lower.exists(BS5Patterns.close_088, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.fine("下段とじるをタップ");
				my.single_tap_by_Location(BS5Regions.close_lower.getTarget());
			}
			// 下段左とじる
			else if (Objects.nonNull(BS5Regions.close_lower_left.exists(BS5Patterns.close_088, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.fine("下段左とじるをタップ");
				my.single_tap_by_Location(BS5Regions.close_lower_left.getTarget());
			}
			// 下段最左とじる
			else if (Objects
				.nonNull(BS5Regions.close_lower_most_left.exists(BS5Patterns.close_small_088, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.fine("下段最左とじるをタップ");
				my.single_tap_by_Location(BS5Regions.close_lower_most_left.getTarget());
			}
			// もどる
			else if (Objects.nonNull(BS5Regions.turn_back.exists(BS5Patterns.turn_back_088, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.fine("もどるをタップ");
				my.single_tap_by_Location(BS5Regions.turn_back.getTarget());
			}
			// ホーム画面
			else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.ranking_094, WAIT_TIMEOUT))) {
				is_appeared += 1;
				Location l = BS5Regions.tsum.getLastMatch().getTarget();
				if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.my_play_history_088, WAIT_TIMEOUT))) {
					// 自身の位置取得
					int number_of_my_rank = BS5Activity.recognition_number_on_my_ranking(BS5Regions.my_rank);
					myLogger.info(String.format("自身%1$d位", number_of_my_rank));
					BS5Status.set_number_of_my_rank(number_of_my_rank);
				}
				myLogger.fine("ランキングをタップ");
				my.single_tap_by_Location(l);
			}
			// LINE
			else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.LINE_088, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.fine("LINEログインを実施(2)");
				login_LINE(LIMIT_SECONDS_LOGIN_LINE);
			}
			// LINE GAME → LINEログイン
			else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.LINE_GAME_088, WAIT_TIMEOUT))) {
				is_appeared += 1;
				myLogger.fine("LINEログインを実施(1)");
				login_LINE(LIMIT_SECONDS_LOGIN_LINE);
			}
			// TAP TO START
			else if (Objects.nonNull(BS5Regions.tap_to_start.exists(BS5Patterns.tap_to_start_088, WAIT_TIMEOUT))) {
				myLogger.fine("TAP TO STARTを検出");
				BS5Status.set_returned_to_title(true);
				if (is_appeared_home) {
					// ホーム画面で'ツムツム'をタップしていたら、
					// ツムツム再起動とみなす
					is_appeared_home = false;
					BS5Status.set_root_exist_popup(false);
				} else {
					is_appeared += 1;
					myLogger.fine("TAP TO STARTをタップ");
					my.single_tap_by_Location(BS5Regions.tap_to_start.getTarget());
				}
				// 2021.09.05 一時的にルート化を検出「許可する」の待ちを無くす
				// // 制限時間をプラス
				// limit_datetime = limit_datetime.plusSeconds(C_LIMIT_SECONDS_ROOT_EXIST + 10);
				// // ルート化を検出「許可する」
				// permit_root_exist(C_LIMIT_SECONDS_ROOT_EXIST);
			}
			// ゲームセンター画面
			else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.bs5_gamecenter_088, WAIT_TIMEOUT))) {
				myLogger.warn("ツム落ち発生？");
				mySS.take("ツム落ち疑惑");
				is_appeared_home = true;
				// ADB経由ツムツム起動
				ADBWrapper.start_TsumTsum();
				my.sleep(2000);
			}

			// これは必ず最後に実施する
			if (is_appeared == 0) {
				// 汎用タップ
				if (Objects.isNull(BS5Regions.play.exists(BS5Patterns.start_090, WAIT_TIMEOUT))) {
					general_taps += 1;
					is_appeared += 1;
					// 1度目は実際のタップをスキップ
					if (general_taps > 1) {
						myLogger.fine("汎用タップ");
						my.single_tap_by_Location(BS5Regions.play.getTarget().offset(0, -16));
					}
				}
			} else {
				general_taps = 0;
			}
			// もういっちょ最後に実施する
			if (general_taps > 3) {
				// Region reg_tsum = App.focusedWindow();
				// if (BS5App.is_running(2)) {
				// reg_tsum = BS5App.focus();
				// }
				// ウィンドウ移動&リサイズ
				Region reg_tsum = BS5App.move_resize_window(0, 0, 568, 983);
				if (Objects.isNull(reg_tsum)) {
					// エミュ再起動
					BS5Activity.bs_restart(BS5Activity.LIMIT_SECONDS_RESTART);
					reg_tsum = BS5App.focus();
				} else {
					BS5Regions.set_region(reg_tsum);
				}
				// ホーム画面(縦)で'ツムツム'
				if (Objects.nonNull(reg_tsum.exists(BS5Patterns.tsum_app_icon_portrait_088, WAIT_TIMEOUT))) {
					myLogger.fine("ホーム画面(縦)で'ツムツム'をタップ");
					is_appeared_home = true;
					mySS.take("recovery");
					// my.single_tap_by_Location(reg_tsum.getLastMatch().getTarget());
					// my.sleep(2000);
					// ADB経由ツムツム起動
					// my.println("ADB経由ツムツム起動");
					ADBWrapper.start_TsumTsum();
					my.sleep(2000);
					// Regionセットしなおす
					reg_tsum = BS5App.focus();
					BS5Regions.set_region(reg_tsum);
				}
			}
		}

		if (!breaked) {
			myLogger.warn("recovery failed by timeout");
			mySS.take_screen("recovery_failed");

			if (allow_stop_and_create) {
				// ツム再起動
				stop_and_create(LIMIT_SECONDS_RESTART);
			} else {
				// リトライオーバー
				throw new RetryOverException("recovery_to_ranking");
			}
		}
	}

	/**
	 * 自身のランキングを表示.
	 */
	public static int to_my_ranking() {
		// 週間ランキングを表示
		recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);
		// ホームを表示
		if (Objects.isNull(BS5Regions.title.exists(BS5Patterns.title_home_094, 1.0))) {
			Location l = new Location(BS5Regions.tsum.x + 29, BS5Regions.tsum.y + 532);
			my.single_tap_by_Location(l);
			my.sleep(1400);
		}
		// 自身の位置取得
		int number_of_my_rank = BS5Activity.recognition_number_on_my_ranking(BS5Regions.my_rank);
		myLogger.info(String.format("自身%1$d位", number_of_my_rank));
		// 再び週間ランキングを表示
		recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);
		return number_of_my_rank;
	}

	/**
	 * 自身のランキングNo.を数値化.
	 * 
	 * @param reg
	 * @return
	 */
	public static int recognition_number_on_my_ranking(Region reg) {
		long start_datetime = System.nanoTime();
		// それぞれの数字画像を検索し、
		// X座標とインデックスをペアにしてリストへ格納
		Map<Integer, Integer> numbers = new HashMap<Integer, Integer>();
		for (int index = 0; index < BS5Patterns.numbers_array_on_my_ranking.length; index++) {
			Pattern pattern = BS5Patterns.numbers_array_on_my_ranking[index];
			final int number = index;
			try {
				reg.findAll(pattern).forEachRemaining(r -> {
					numbers.put(Integer.valueOf((r.getX() - reg.getX()) / 13), number);
					// my.println("index=" + Integer.toString(number));
				});
			} catch (FindFailed e) {
				// 見つからないこともある
				// e.printStackTrace();
				// my.println("findAll failed. : number=" + number);
			}
		}
		// ヒットしない場合は -1
		if (numbers.isEmpty()) {
			return -1;
		}
		// my.println("numbers=" + numbers.toString());
		// X座標でソート
		numbers.entrySet().stream().sorted(java.util.Map.Entry.comparingByKey());
		// 順番にインデックスを取り出し数値化
		int result = numbers.values().stream().reduce(0, (accum, value) -> accum * 10 + value);
		// 処理時間計測
		long elapsed_time = System.nanoTime() - start_datetime;
		my.println(String.format("recognition number of rank one  : elapsed time = %1$d (msec)", elapsed_time / 1000000));
		// 数値を返す
		return result;
	}

	/**
	 * ランキングNo.(ミッキー化含む)を数値化.
	 * 
	 * @param reg
	 * @return
	 */
	public static int recognition_number_on_mickey(Region reg) {
		// Region regTake = new Region(reg);
		long start_datetime = System.nanoTime();
		// それぞれの数字画像を検索し、
		// X座標とインデックスをペアにしてリストへ格納
		Map<Integer, Integer> numbers = new HashMap<Integer, Integer>();
		// 2桁目をベストマッチ
		Region regSub2 = new Region(reg.x + 0, reg.y, 25, reg.h);
		// regSub2.highlight(1);
		Match match2 = regSub2.findBestList(Arrays.asList((Object[]) BS5Patterns.numbers_array_on_ranking23));
		if (Objects.nonNull(match2)) {
			if (match2.getIndex() < 10) {
				// 2桁目がヒットした
				numbers.put(Integer.valueOf((match2.getX() - reg.getX()) / 12), match2.getIndex());
				// my.println("match2=" + match2.toString());
				// my.println("index2=" + Integer.toString(match2.getIndex()) + ", x=" +
				// Integer.toString(match2.getX()));
				// regTake = new Region(match2.getX() - 1, match2.getY() - 1, match2.getW() + 2,
				// match2.getH() + 2);
				// mySS.take(regTake, "22-" + Integer.toString(match2.getIndex()),
				// LocalDateTime.now());
				// 1桁目をベストマッチ
				Region regSub21 = new Region(reg.x + 22, reg.y, 15, reg.h);
				// regSub21.highlight(1);
				Match match21 = regSub21.findBestList(Arrays.asList((Object[]) BS5Patterns.numbers_array_on_ranking2));
				if (Objects.nonNull(match21)) {
					// 1桁目がヒットした
					numbers.put(Integer.valueOf((match21.getX() - reg.getX()) / 12), match21.getIndex());
					// my.println("match21=" + match21.toString());
					// my.println("index21=" + Integer.toString(match21.getIndex()) + ", x=" +
					// Integer.toString(match21.getX()));
					// regTake = new Region(match21.getX() - 1, match21.getY() - 1, match21.getW() +
					// 2, match21.getH() + 2);
					// mySS.take(regTake, "21-" + Integer.toString(match21.getIndex()),
					// LocalDateTime.now());
				}
			} else {
				// 3桁目がヒットした
				numbers.put(Integer.valueOf((match2.getX() - reg.getX()) / 12), match2.getIndex() - 10);
				// my.println("match3=" + match2.toString());
				// my.println("index3=" + Integer.toString(match2.getIndex()) + ", x=" +
				// Integer.toString(match2.getX()));
				// regTake = new Region(match2.getX() - 1, match2.getY() - 1, match2.getW() + 2,
				// match2.getH() + 2);
				// mySS.take(regTake, "33-" + Integer.toString(match2.getIndex() - 10),
				// LocalDateTime.now());
				// 1桁目をベストマッチ
				Region regSub31 = new Region(reg.x + 28, reg.y, 15, reg.h);
				// regSub31.highlight(1);
				Match match31 = regSub31.findBestList(Arrays.asList((Object[]) BS5Patterns.numbers_array_on_ranking3));
				if (Objects.nonNull(match31)) {
					// 1桁目がヒットした
					numbers.put(Integer.valueOf((match31.getX() - reg.getX()) / 12), match31.getIndex());
					// my.println("match31=" + match31.toString());
					// my.println("index31=" + Integer.toString(match31.getIndex()) + ", x=" +
					// Integer.toString(match31.getX()));
					// regTake = new Region(match31.getX() - 1, match31.getY() - 1, match31.getW() +
					// 2, match31.getH() + 2);
					// mySS.take(regTake, "31-" + Integer.toString(match31.getIndex() - 10),
					// LocalDateTime.now());
				}
				// 2桁目をベストマッチ
				Region regSub32 = new Region(reg.x + 14, reg.y, 15, reg.h);
				// regSub32.highlight(1);
				Match match32 = regSub32.findBestList(Arrays.asList((Object[]) BS5Patterns.numbers_array_on_ranking3));
				if (Objects.nonNull(match32)) {
					// 2桁目がヒットした
					numbers.put(Integer.valueOf((match32.getX() - reg.getX()) / 12), match32.getIndex());
					// my.println("match32=" + match32.toString());
					// my.println("index32=" + Integer.toString(match32.getIndex()) + ", x=" +
					// Integer.toString(match32.getX()));
					// regTake = new Region(match32.getX() - 1, match32.getY() - 1, match32.getW() +
					// 2, match32.getH() + 2);
					// mySS.take(regTake, "32-" + Integer.toString(match32.getIndex() - 10),
					// LocalDateTime.now());
				}
			}
		} else {
			// 2桁目がヒットしない
			// 1桁目をベストマッチ
			Match match1 = reg.findBestList(Arrays.asList((Object[]) BS5Patterns.numbers_array_on_ranking1));
			if (Objects.nonNull(match1)) {
				// 1桁目がヒットした
				numbers.put(Integer.valueOf((match1.getX() - reg.getX()) / 12), match1.getIndex());
				// my.println("match1=" + match1.toString());
				// my.println("index1=" + Integer.toString(match1.getIndex()) + ", x=" +
				// Integer.toString(match1.getX()));
			}
		}
		// ヒットしない場合は -1
		if (numbers.isEmpty()) {
			// my.println("numbers.isEmpty");
			return -1;
		}
		// my.println("numbers=" + numbers.toString());
		// X座標でソート
		numbers.entrySet().stream().sorted(java.util.Map.Entry.comparingByKey());
		// 順番にインデックスを取り出し数値化
		int result = numbers.values().stream().reduce(0, (accum, value) -> accum * 10 + value);
		// 処理時間計測
		long elapsed_time = System.nanoTime() - start_datetime;
		my.println(
			String.format("recognition number of rank one  : elapsed time = %1$d (msec)", elapsed_time / 1000000));
		// 数値を返す
		return result;
	}

	/**
	 * ページ内の各人のランキング数認識.
	 * 
	 * @return ランキングNo.リスト
	 */
	public static List<Integer> recognition_numbers_in_a_page() {
		int[] result = null;
		long start_datetime = System.nanoTime();
		// ページ単位各人のランキング数認識
		boolean breaked = false;
		for (int i = 0; i < RETRY_EXISTS_NUMBER_ON_MICKEY; i++) {
			// 2と3で認識
			{
				int number_rank3 = recognition_number_on_mickey(BS5Regions.rank3);
				int number_rank2 = recognition_number_on_mickey(BS5Regions.rank2);
				int[] temporary = new int[] { number_rank2, number_rank3 };

				if (IntStream.of(temporary).anyMatch(number -> number == -1)) {
					my.waitForIdle(10);
					myLogger.info("[recognition] number == -1 : " + Arrays.toString(temporary));
				} else {
					// 検証
					int total_sum = IntStream.of(temporary).sum();
					int verification = (number_rank2 * 2) + 1;
					if (total_sum == verification) {
						result = new int[] { number_rank2 - 1, number_rank2, number_rank3, number_rank3 + 1 };
						breaked = true;
						break;
					}
					// 検証結果不一致
					my.println(String.format("[recognition] total_sum=%1$d, verification=%2$d", total_sum, verification));
					my.println(Arrays.toString(temporary));
				}
			}
			// 1と2で再度認識
			{
				int number_rank2 = recognition_number_on_mickey(BS5Regions.rank2);
				int number_rank1 = recognition_number_on_mickey(BS5Regions.rank1);
				int[] temporary = new int[] { number_rank1, number_rank2 };
				if (IntStream.of(temporary).anyMatch(number -> number == -1)) {
					my.waitForIdle(10);
					myLogger.info("[recognition] number == -1 : " + Arrays.toString(temporary));
				} else {
					// 検証
					int total_sum = IntStream.of(temporary).sum();
					int verification = (number_rank1 * 2) + 1;
					if (total_sum == verification) {
						result = new int[] { number_rank1, number_rank2, number_rank2 + 1, number_rank2 + 2 };
						breaked = true;
						break;
					}
					// 検証結果不一致
					my.println(String.format("[recognition] total_sum=%1$d, verification=%2$d", total_sum, verification));
					my.println(Arrays.toString(temporary));
				}
			}
		}
		if (!breaked) {
			my.println("RETRY OVER recognition_number_rank_in_a_page");
		}

		// 処理時間計測
		long elapsed_time = System.nanoTime() - start_datetime;
		my.println(
			String.format("recognition number of rank four : elapsed time = %1$d (msec)", elapsed_time / 1000000));
		// 結果を返す
		if (Objects.isNull(result)) {
			return null;
		}
		// リスト変換
		return Arrays.stream(result).boxed().collect(Collectors.toList());
	}

	/**
	 * ページ内対象段のランキングNo.認識.
	 * 
	 * @param row ページ内対象段
	 * @return ランキングNo.
	 */
	public static Integer recognition_numbers_on_row(int row) {
		if (row > 4) {
			return null;
		}

		final Region[] reg_ranks = new Region[] {
			BS5Regions.rank4,
			BS5Regions.rank1,
			BS5Regions.rank2,
			BS5Regions.rank3,
			BS5Regions.rank4,
		};
		Region reg_rank = reg_ranks[row];
		int result = -1;
		boolean breaked = false;
		for (int i = 0; i < RETRY_EXISTS_NUMBER_ON_MICKEY; i++) {
			result = recognition_number_on_mickey(reg_rank);
			if (result == -1) {
				my.waitForIdle(10);
				my.println("[recognition] number == -1");
				continue;
			}
			breaked = true;
			break;
		}
		if (!breaked) {
			my.println("RETRY OVER recognition_number_rank_on_row");
		}

		// 結果を返す
		if (result == -1) {
			return null;
		}
		return Integer.valueOf(result);
	}

	public static final double C_HEIGHT_OF_ONE_BLOCK = 100d;
	public static final int C_HEIGHT_OF_ONE_BLOCK_INT = BigDecimal.valueOf(C_HEIGHT_OF_ONE_BLOCK)
		.setScale(0, RoundingMode.HALF_UP).intValue();

	/**
	 * n人分スワイプ.
	 */
	public static void swipe_n(double blocks) {
		// 高さ計算
		double height = C_HEIGHT_OF_ONE_BLOCK * blocks;
		int start_y = 20;
		if (0 < blocks) {
			start_y = BS5Regions.rank_list.h - 20;
		}
		int end_y = BigDecimal.valueOf(start_y).subtract(BigDecimal.valueOf(height)).setScale(0, RoundingMode.HALF_UP)
			.intValue();
		my.println(String.format("blocks=%1$f, start_y=%2$d, end_y=%3$d", blocks, start_y, end_y));
		// 位置決め
		Location base = BS5Regions.rank_list.getTopLeft().offset(OFFSET_X_FROM_LEADING_EDGE_OF_RANK_LIST, 0);
		Location startLoc = base.offset(0, start_y);
		Location middleLoc = base.offset(0, end_y);
		Location endLoc = base.offset(40, end_y);
		// スワイプ実施
		my.swipe_with_middle_by_Location(startLoc, middleLoc, endLoc);
		// my.swipe_with_middle_by_Location_to_dividely(startLoc, middleLoc, endLoc);
	}

	/**
	 * 下までスクロールダウン.
	 */
	public static void swipe_down_to_bottom() {
		// スクロールダウンループ
		for (int i = 0; i < 100; i++) {
			if (Objects.nonNull(BS5Regions.rank_list.exists(BS5Patterns.invitation_085, WAIT_TIMEOUT))) {
				// mySS.take("招待認識A");
				if (Objects.isNull(BS5Regions.rank_list.exists(BS5Patterns.invitation_085, WAIT_TIMEOUT))) {
					myLogger.info("招待見つからない");
				}
				// mySS.take("招待認識B");
				break;
			}
			swipe_n(6.0);
			// ランキング表示に戻れているかチェック
			recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);
		}
		// 落ち着くまで待機
		my.sleep(1000);
		// ^_^mySS.take("招待隠し疑惑");
		for (int i = 0; i < 50; i++) {
			// 念の為更に下へスワイプしておく
			swipe_n(2.0);
			swipe_n(2.0);
			// 落ち着くまで待機
			my.sleep(2000);
			// 招待認識判定
			if (Objects.nonNull(BS5Regions.rank_list.exists(BS5Patterns.invitation_085, WAIT_TIMEOUT))) {
				return;
			}
			// ランキング表示に戻れているかチェック
			recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);
		}
	}

	/**
	 * 区切り線を下端に合わせる.
	 */
	public static void fit_seperator_line_to_bottom_border() {
		try {
			// リスト区切りを検出
			Spliterator<Match> spliterator = Spliterators
				.spliteratorUnknownSize(BS5Regions.rank_list.findAll(BS5Patterns.separator_line_087), 0);
			Stream<Match> stream = StreamSupport.stream(spliterator, false);
			Comparator<Match> comparator = Comparator.comparing(Match::getY).reversed();
			List<Match> sorted_lines = stream.sorted(comparator).collect(Collectors.toList());
			// 位置決め
			Location baseLocation = BS5Regions.rank_list.getTopLeft().offset(OFFSET_X_FROM_LEADING_EDGE_OF_RANK_LIST, 0);
			Location startLocation = new Location(baseLocation.x, sorted_lines.get(0).getTarget().y);
			Location middleLocation = baseLocation.offset(0, BS5Regions.rank_list.h);
			Location endLocation = baseLocation.offset(40, BS5Regions.rank_list.h);
			// スワイプ実施
			my.swipe_with_middle_by_Location(startLocation, middleLocation, endLocation);
		} catch (FindFailed e) {
			e.printStackTrace();
		}
	}

	/**
	 * 区切り線を上端に合わせる.
	 */
	public static void fit_seperator_line_to_top_border() {
		try {
			// リスト区切りを検出
			Spliterator<Match> spliterator = Spliterators
				.spliteratorUnknownSize(BS5Regions.rank_list.findAll(BS5Patterns.separator_line_087), 0);
			Stream<Match> stream = StreamSupport.stream(spliterator, false);
			Comparator<Match> comparator = Comparator.comparing(Match::getY);
			List<Match> sorted_lines = stream.sorted(comparator).collect(Collectors.toList());
			// 位置決め
			Location baseLocation = BS5Regions.rank_list.getTopLeft().offset(OFFSET_X_FROM_LEADING_EDGE_OF_RANK_LIST, 0);
			Location startLocation = new Location(baseLocation.x, sorted_lines.get(0).getTarget().y);
			Location middleLocation = baseLocation.offset(0, 0);
			Location endLocation = baseLocation.offset(40, 0);
			// スワイプ実施
			my.swipe_with_middle_by_Location(startLocation, middleLocation, endLocation);
		} catch (FindFailed e) {
			e.printStackTrace();
		}
	}

	// /**
	// * レジューム位置シーク.
	// * <P>
	// * 指定のランキングNo.をページ4段目にくるようスワイプする
	// * @param lowest_number_rank 4段目のランキングNo.
	// * @param number_rank_list_cache ランキングNo.リストのキャッシュ
	// * @throws RetryOverException リトライオーバー
	// */
	// public static void seek_resume_position(int lowest_number_rank, List<Integer>
	// number_rank_list_cache) throws RetryOverException {
	// seek_resume_position(lowest_number_rank, number_rank_list_cache, false);
	// }
	/**
	 * レジューム位置シーク.
	 * <P>
	 * 指定のランキングNo.をページ4段目にくるようスワイプする
	 * 
	 * @param lowest_number_rank 4段目のランキングNo.
	 * @throws RetryOverException リトライオーバー
	 */
	public static void seek_resume_position(int lowest_number_rank) throws RetryOverException {
		seek_resume_position(lowest_number_rank, false);
	}

	// /**
	// * レジューム位置シーク.
	// * <P>
	// * 指定のランキングNo.をページ4段目にくるようスワイプする
	// * @param lowest_number_rank 4段目のランキングNo.
	// * @param number_rank_list_cache ランキングNo.リストのキャッシュ
	// * @param info ロガーへの情報出力有無
	// * @throws RetryOverException リトライオーバー
	// */
	// public static void seek_resume_position(int lowest_number_rank, List<Integer>
	// number_rank_list_cache, boolean info) throws RetryOverException {
	/**
	 * レジューム位置シーク.
	 * <P>
	 * 指定のランキングNo.をページ4段目にくるようスワイプする
	 * 
	 * @param lowest_number_rank 4段目のランキングNo.
	 * @param info               ロガーへの情報出力有無
	 * @throws RetryOverException リトライオーバー
	 */
	public static void seek_resume_position(int lowest_number_rank, boolean info) throws RetryOverException {
		List<Integer> number_rank_list = number_rank_list_cache;

		my.println("seek resume position");
		int des_lowest_number_rank = lowest_number_rank;
		if (lowest_number_rank < 4) {
			// 所望の4段目が4未満なら、4に補正する
			des_lowest_number_rank = 4;
		}
		int trials = 0;
		for (; trials < RETRY_SEEK; trials++) {
			if (0 >= lowest_number_rank) {
				break;
			}
			if (Objects.isNull(number_rank_list)) {
				boolean breaked = false;
				for (int i = 0; i < 3; i++) {
					// ページ単位各人のランキング数認識
					number_rank_list = recognition_numbers_in_a_page();
					if (Objects.isNull(number_rank_list)) {
						// ランキング表示に戻れているかチェック
						recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);
						mySS.take_screen("missed swipe");
						// ページ位置を戻す
						fit_seperator_line_to_bottom_border();
					} else {
						// 左上の1ドットの色を取得し、0.5より上かを判定する
						boolean is_bright = is_brightness(BS5Regions.title);
						if (is_bright) {
							breaked = true;
							break;
						} else {
							// ランキング表示に戻れているかチェック
							recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);
							mySS.take_screen("missed swipe darkness");
						}
					}
				}
				if (!breaked) {
					// リトライオーバー
					// フリーズした可能性があるため、例外送出
					mySS.take("RETRY OVER seek");
					throw new RetryOverException("RETRY OVER seek");
				}
			}

			try {
				// 該当のランキング数が表示されているかチェック
				if (number_rank_list.stream().anyMatch(number -> number.equals(lowest_number_rank))) {
					// 終了判定
					if (lowest_number_rank < 4) {
						// 4未満なら終了
						break;
					} else {
						if (number_rank_list.get(3).equals(des_lowest_number_rank)) {
							// 4段目に来たら終了
							break;
						} else {
							if (info) {
								myLogger.info(String.format("現在の4段目：%1$d, 所望の4段目：%2$d", number_rank_list.get(3),
									lowest_number_rank));
							}
							// 4段目に来るまで、ブロック分スワイプする
							double blocks = 0.0;
							if (des_lowest_number_rank > number_rank_list.get(3)) {
								blocks = Math.min(des_lowest_number_rank - number_rank_list.get(3).intValue(), 6.0d);
							} else {
								blocks = Math.max(des_lowest_number_rank - number_rank_list.get(3).intValue(), -6.0d);
							}
							swipe_n(blocks);
							my.waitForIdle(10);
							// キャッシュクリア
							number_rank_list = null;
							continue;
						}
					}
				}
				// 上下どちらかへスワイプ
				int src_lowest_number_rank = number_rank_list.get(3);
				if (des_lowest_number_rank > src_lowest_number_rank) {
					my.println("現在の表示より大きい");
					// 現在の表示より大きければ、下へスワイプ
					while (des_lowest_number_rank > src_lowest_number_rank) {
						my.println(String.format("現在の4段目：%1$d, 所望の4段目：%2$d", src_lowest_number_rank,
							des_lowest_number_rank));
						double blocks = Math.min(des_lowest_number_rank - src_lowest_number_rank, 6.0d);
						swipe_n(blocks);
						if (info) {
							myLogger.info(String.format("下へスワイプ：%1$f", blocks));
						}
						src_lowest_number_rank += (int) blocks;
					}
				} else if (des_lowest_number_rank < src_lowest_number_rank) {
					my.println("現在の表示より小さい");
					// 現在の表示より小さければ、上へスワイプ
					while (des_lowest_number_rank < src_lowest_number_rank) {
						my.println(String.format("現在の4段目：%1$d, 所望の4段目：%2$d", src_lowest_number_rank,
							des_lowest_number_rank));
						double blocks = Math.max(des_lowest_number_rank - src_lowest_number_rank, -6.0d);
						swipe_n(blocks);
						if (info) {
							myLogger.info(String.format("上へスワイプ：%1$f", blocks));
						}
						src_lowest_number_rank += (int) blocks;
					}
				}
				my.waitForIdle(10);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// キャッシュクリア
			number_rank_list = null;
		}
		// キャッシュ更新
		number_rank_list_cache = number_rank_list;

		if (trials >= RETRY_SEEK) {
			// リトライオーバー
			// フリーズした可能性があるため、例外送出
			mySS.take_screen("RETRY OVER seek_resume_position");
			throw new RetryOverException("RETRY OVER seek_resume_position");
		}
	}

	/**
	 * 4段目ランキングNo.を先にチェックして、レジューム位置シーク.
	 * <P>
	 * 指定のランキングNo.をページ4段目にくるようスワイプする
	 * 
	 * @param lowest_number_rank 4段目のランキングNo.
	 * @param info               ロガーへの情報出力有無
	 * @throws RetryOverException リトライオーバー
	 */
	public static void seek_resume_position_with_check_row4(int lowest_number_rank, boolean info)
		throws RetryOverException {
		int des_lowest_number_rank = lowest_number_rank;
		if (lowest_number_rank < 4) {
			// 所望の4段目が4未満なら、4に補正する
			des_lowest_number_rank = 4;
		}
		// 4段目のランキングNo.認識
		Integer number_rank_4 = recognition_numbers_on_row(4);
		if (Objects.nonNull(number_rank_4)) {
			if (number_rank_4.equals(des_lowest_number_rank)) {
				// 4段目のランキングNo.のみで一致したら、
				// 終了
				my.println("no seek required");
				return;
			}
		}

		// 4段目のランキングNo.のみで一致しなかったら、シーク
		seek_resume_position(lowest_number_rank, info);
	}

	/**
	 * タップ位置定義.
	 */
	public static final int ROW_TO_OFFSET[] = new int[] { 329, 47, 141, 235 };

	/**
	 * ハイスコア表示→非表示.
	 * 
	 * @param row ページ内タップ対象段
	 */
	public static void show_and_hide_high_score(int row) throws RetryOverException {
		show_and_hide_high_score(row, LocalDateTime.now());
	}

	/**
	 * ハイスコア表示→非表示.
	 * 
	 * @param row            ページ内タップ対象段
	 * @param limit_datetime 非表示の期限日時
	 */
	public static void show_and_hide_high_score(int row, LocalDateTime limit_datetime) throws RetryOverException {
		try {
			Location l = BS5Regions.rank_list.getTopLeft().offset(OFFSET_X_FROM_LEADING_EDGE_OF_RANK_LIST, ROW_TO_OFFSET[row]);
			myLogger.fine(String.format("ランキングをタップ Y=%1$d", ROW_TO_OFFSET[row]));
			for (int i = 0; i < RETRY_TAP_HIGH_SCORE; i++) {
				if (Objects.isNull(BS5Regions.title.exists(BS5Patterns.title_weekly_ranking_094, 0))) {
					break;
				}
				my.single_tap_by_Location(l);
			}
			// これまでのハイスコアが表示されているか判定
			boolean breaked = false;
			for (int i = 0; i < RETRY_EXISTS_HIGH_SCORE; i++) {
				if (Objects.nonNull(
					BS5Regions.high_score_so_far.exists(BS5Patterns.high_score_so_far_090, WAIT_TIMEOUT))) {
					myLogger.fine("ハイスコア表示");
					breaked = true;
					break;
				}
				if (Objects.nonNull(BS5Regions.retry.exists(BS5Patterns.retry_090, WAIT_TIMEOUT))) {
					myLogger.warn_members("リトライをタップ");
					my.single_tap_by_Location(BS5Regions.retry.getTarget());
				}
				my.sleep(100);
			}
			if (!breaked) {
				throw new RetryOverException(
					String.format("ハイスコア表示されていない？ Y=%1$d",
						ROW_TO_OFFSET[row]));
			}
			my.sleep(300);

			// 期限まで待つ
			final LocalDateTime limit_datetime_for_check = limit_datetime.minus(min_close_highscore_ms,
				ChronoUnit.MILLIS);
			ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
			ScheduledFuture<?> watchdog = service.schedule(() -> {
				myLogger.info(String.format(
					"期限まで待った, %1$s",
					myDateTime.formatter.format(limit_datetime_for_check)));
			}, ChronoUnit.NANOS.between(LocalDateTime.now(), limit_datetime_for_check), TimeUnit.NANOSECONDS);
			watchdog.get();
			service.shutdown();

			myLogger.fine("とじるをタップ");
			long start_time = System.nanoTime();
			long elapsed_time = start_time;
			LocalDateTime single_tap_limit_datetime = LocalDateTime.now().plus(TAP_INTERVAL_MILLIS, ChronoUnit.MILLIS);
			l = BS5Regions.close_lower.getCenter();
			for (int i = 0; i < RETRY_TAP_UPPER_CLOSE; i++) {
				if (LocalDateTime.now().isAfter(single_tap_limit_datetime)) {
					break;
				}
				my.single_tap_by_Location(l);
				my.sleep(40);
			}
			// これまでのハイスコアが非表示されているか判定
			breaked = false;
			for (int i = 0; i < RETRY_DISAPPEAR_HIGH_SCORE; i++) {
				if (Objects.nonNull(BS5Regions.title.exists(BS5Patterns.title_weekly_ranking_094, WAIT_TIMEOUT))) {
					myLogger.fine("ハイスコア非表示");
					breaked = true;
					break;
				}
				my.sleep(100);
			}
			if (!breaked) {
				throw new RetryOverException("ハイスコア非表示されていない？");
			}
			elapsed_time = System.nanoTime() - start_time;

			// ハイスコア非表示時間を更新
			long earlier_close_highscore_ms = elapsed_time / 1000000;
			myLogger.fine(String.format("ハイスコア非表示：%1$d (ms)", earlier_close_highscore_ms));
			if ((min_close_highscore_ms > earlier_close_highscore_ms)
				&& (earlier_close_highscore_ms > TAP_INTERVAL_MILLIS)) {
				min_close_highscore_ms = earlier_close_highscore_ms;
				myIO.<Long> write(Long.valueOf(min_close_highscore_ms), Paths.get(PATH_LONG_CLOSE_HIGHSCORE));
			}
		} catch (RetryOverException e) {
			// リトライオーバー発生時
			mySS.take_screen("show_and_hide_high_score_RetryOverException");
			throw e;
		} catch (Exception e) {
			// その他例外発生時
			// スタックトレース出力
			e.printStackTrace();
			mySS.take("show_and_hide_high_score_E");
		}
	}

	/**
	 * ♡を受け取る.
	 * 
	 * @param limit_seconds 制限時間(s)
	 */
	public static void take_all_hearts(long limit_seconds) {
		// タイムアウト時刻設定
		LocalDateTime limit_datetime = LocalDateTime.now().plusSeconds(limit_seconds);

		// タイムアウトを待ちながら♡の受け取りを試みる
		boolean breaked = false;
		while (limit_datetime.isAfter(LocalDateTime.now())) {
			// メールボックスの表示を試みる
			for (int i = 0; i < LIMIT_SECONDS_TAKE_ALL_HEART; i++) {
				if (Objects.nonNull(BS5Regions.retry.exists(BS5Patterns.retry_090, WAIT_TIMEOUT))) {
					my.println("リトライをタップ");
					my.single_tap_by_Location(BS5Regions.retry.getTarget());
					my.sleep(1000);
				} else if (Objects.isNull(BS5Regions.mailbox.exists(BS5Patterns.title_mailbox_088, WAIT_TIMEOUT))) {
					my.println("メールボックスアイコンをタップ");
					my.single_tap_by_Location(BS5Regions.mail_icon.getTarget());
				} else {
					// まとめて受け取るが表示されていなければ、
					if (Objects
						.isNull(BS5Regions.collectively_receive.exists(BS5Patterns.collectively_receive_090, 2))) {
						// ここでループ終了を立てる
						breaked = true;
					}
					break;
				}
				my.sleep(300);
			}
			// まとめて受け取るが表示されていれば、
			if (Objects.nonNull(BS5Regions.collectively_receive.exists(BS5Patterns.collectively_receive_090, 2))) {
				// まとめて受け取るを試みる
				for (int i = 0; i < LIMIT_SECONDS_TAKE_ALL_HEART; i++) {
					Match collectively_receive = BS5Regions.collectively_receive.getLastMatch();
					if (Objects.isNull(BS5Regions.ok_upper.exists(BS5Patterns.ok_090, WAIT_TIMEOUT))) {
						my.println("まとめて受け取るをタップ");
						my.single_tap_by_Location(collectively_receive.getTarget());
					} else {
						breaked = true;
						break;
					}
					my.sleep(300);
				}
			}
			// 週間ランキングへ戻る
			long limit_seconds_remain = ChronoUnit.SECONDS.between(LocalDateTime.now(), limit_datetime);
			recovery_to_ranking(limit_seconds_remain);
			// まとめて受け取るをタップしていれば終了
			if (breaked) {
				break;
			}
		}
	}

	/**
	 * 1個の♥を贈る.
	 * 
	 * @param heart       ♥
	 * @param number_rank ランキングNo.
	 * @return 送信時刻
	 */
	public static LocalDateTime give_a_heart(Match heart, Integer number_rank) {
		// 変数定義
		LocalDateTime result = null;
		LocalDateTime limit_datetime = LocalDateTime.now().plusSeconds(LIMIT_SECONDS_GIVE_A_HEART);
		boolean breaked = false;

		try {
			long start_time = 0;
			long elapsed_time = start_time;
			myLogger.fine("ハートをタップ");
			while (limit_datetime.isAfter(LocalDateTime.now())) {
				my.single_tap_by_Location(heart.getTarget().offset(12, 0));
				// if
				// (Objects.isNull(BS5Regions.title.exists(BS5Patterns.title_weekly_ranking_094,
				// 0))) {
				if (!is_brightness(BS5Regions.title)) {
					myLogger.fine("ハートをプレゼント");
					breaked = true;
					break;
				}
			}
			if (!breaked) {
				throw new RetryOverException("TIMEOUT give_a_heart");
			}
			breaked = false;
			start_time = System.nanoTime();
			elapsed_time = start_time;
			result = LocalDateTime.now();
			while (limit_datetime.isAfter(LocalDateTime.now())) {
				my.single_tap_by_Location(BS5Regions.ok_upper.getTarget());
				// if
				// (Objects.nonNull(BS5Regions.title.exists(BS5Patterns.title_weekly_ranking_094,
				// 0))) {
				if (is_brightness(BS5Regions.title)) {
					myLogger.fine("OKをタップ");
					result = LocalDateTime.now();
					breaked = true;
					break;
				}
			}
			if (!breaked) {
				throw new RetryOverException("TIMEOUT give_a_heart");
			}
			breaked = false;
			myLogger.fine("カラフルツム表示待ち");
			// タイムアウトを待ちながらカラフルツム表示待ち
			while (limit_datetime.isAfter(LocalDateTime.now())) {
				if (Objects.nonNull(BS5Regions.colorful.exists(BS5Patterns.colorful_tsum_088, WAIT_TIMEOUT))) {
					// 差を求めて1/2した値を足す
					Duration duration = Duration.between(result, LocalDateTime.now());
					result = result.plus(duration.abs().dividedBy(2).toNanos(), ChronoUnit.NANOS);
					myLogger.fine(String.format(
						"ツム鯖受付, %1$s",
						myDateTime.formatter.format(result)));

					elapsed_time = System.nanoTime() - start_time;

					myLogger.fine("カラフルツム表示");
					breaked = true;
					break;
				}
				// if (Objects.nonNull(BS5Regions.colorful.exists(BS5Patterns.colorful_tsum_088,
				// my.C_WAIT_EXISTS))) {
				// myLogger.fine("カラフルツム表示");
				// result = LocalDateTime.now();
				// elapsed_time = System.nanoTime() - start_time;
				//// result = result
				//// .plus(elapsed_time / 2, ChronoUnit.MILLIS);
				//// result = result
				//// .minus(200, ChronoUnit.MILLIS);
				// breaked = true;
				// break;
				// }

				// リトライ頻発対策
				if (Objects.nonNull(BS5Regions.retry.exists(BS5Patterns.retry_090, 0))) {
					myLogger.fine("リトライをタップ");
					my.single_tap_by_Location(BS5Regions.retry.getTarget());
				}
			}
			if (!breaked) {
				throw new RetryOverException("TIMEOUT give_a_heart");
			}
			breaked = false;
			myLogger.fine(String.format("OKタップ→カラフル表示：%1$d (ms)", elapsed_time / 1000000));
			my.println("週間ランキングをタップ");
			while (limit_datetime.isAfter(LocalDateTime.now())) {
				my.single_tap_by_Location(BS5Regions.title.getTarget());
				// if
				// (Objects.nonNull(BS5Regions.title.exists(BS5Patterns.title_weekly_ranking_094,
				// 0))) {
				if (is_brightness(BS5Regions.title)) {
					myLogger.fine("カラフルツム非表示");
					breaked = true;
					break;
				}
			}
			if (!breaked) {
				throw new RetryOverException("TIMEOUT give_a_heart");
			}
			breaked = false;
		} catch (Exception e) {
			// その他例外発生時
			myLogger.warn(String.format("%1$d位 TIMEOUT give_a_heart", number_rank));
			mySS.take_screen(String.format("%1$d位_TIMEOUT_give_a_heart", number_rank));
			// スタックトレース出力
			myLogger.error(e);
		}

		return result;
	}

	/**
	 * ページ単位のハート送信対象辞書(Map)生成.
	 * 
	 * @param target_set 送信対象Set
	 * @return List of Map [ページ単位のハート送信対象, ページ単位のランキングNo]
	 */
	public static List<Map<Integer, Integer>> generate_dict_per_page(Set<Integer> target_set) {
		// ページ単位のハート送信対象辞書(Map)
		HashMap<Integer, Integer> target_per_page = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> number_rank_per_page = new HashMap<Integer, Integer>();

		try {
			List<Integer> number_rank_list = number_rank_list_cache;
			if (Objects.isNull(number_rank_list)) {
				for (int i = 0; i < 2; i++) {
					// ページ単位各人のランキング数認識
					number_rank_list = recognition_numbers_in_a_page();
					if (Objects.isNull(number_rank_list)) {
						mySS.take_screen("missed swipe");
						fit_seperator_line_to_bottom_border();
						seek_resume_position(Collections.max(target_set).intValue());
					} else {
						break;
					}
				}
			}
			if (Objects.nonNull(number_rank_list)) {
				// ページ単位のハート送信対象をセット
				final Integer number_rank_list_3 = number_rank_list.get(3);
				final Integer number_rank_list_2 = number_rank_list.get(2);
				final Integer number_rank_list_1 = number_rank_list.get(1);
				final Integer number_rank_list_0 = number_rank_list.get(0);
				if (target_set.stream().anyMatch(number -> number.equals(number_rank_list_3))) {
					target_per_page.put(number_rank_list_3, 0);
				}
				if (target_set.stream().anyMatch(number -> number.equals(number_rank_list_2))) {
					target_per_page.put(number_rank_list_2, 3);
				}
				if (target_set.stream().anyMatch(number -> number.equals(number_rank_list_1))) {
					target_per_page.put(number_rank_list_1, 2);
				}
				if (target_set.stream().anyMatch(number -> number.equals(number_rank_list_0))) {
					target_per_page.put(number_rank_list_0, 1);
				}
				// ページ単位のランキング数をセット
				number_rank_per_page.put(0, number_rank_list_0);
				number_rank_per_page.put(1, number_rank_list_1);
				number_rank_per_page.put(2, number_rank_list_2);
				number_rank_per_page.put(3, number_rank_list_3);
			}
		} catch (Exception e) {
			// その他例外発生時
			// スタックトレース出力
			e.printStackTrace();
			mySS.take("generate_dict_per_page_E");
		}

		return Arrays.asList(target_per_page, number_rank_per_page);
	}

	/**
	 * [with Gills] ページ単位のハート送信対象辞書(Map)生成.
	 * <P>
	 * ランキングNo.が連続するものだけを対象とする。
	 * 
	 * @param target_set 送信対象Set
	 * @return List of Map [ページ単位のハート送信対象, ページ単位のランキングNo]
	 */
	private static List<Map<Integer, Integer>> generate_dict_per_page_with_stalker(Set<Integer> target_set) {
		Set<Integer> re_target_set = new LinkedHashSet<Integer>();
		Iterator<Integer> iterator_integer = target_set.iterator();
		Integer number_rank = null;
		Integer prev_number_rank = null;
		// 最高4回
		for (int i = 0; (i < 4 && iterator_integer.hasNext()); i++) {
			number_rank = iterator_integer.next();
			if (prev_number_rank != null) {
				if (number_rank.intValue() != (prev_number_rank.intValue() - 1)) {
					break;
				}
			}
			re_target_set.add(number_rank);
			prev_number_rank = number_rank;
		}
		return generate_dict_per_page(re_target_set);
	}

	/**
	 * [with Gills] 全ての♥を贈る.
	 * 
	 * @param target_set ハート未送信集合
	 * @return ハート未送信集合
	 * @throws RetryOverException リトライオーバー
	 */
	public static Set<Integer> give_all_hearts(Set<Integer> target_set) throws RetryOverException {
		List<Integer> number_rank_list = null;
		// mySS.take("開始位置");

		// タイムアウトまで♥送信を行う
		// ※タイムアウト判定はwhileループ内で実施
		while (true) {
			try {
				// ページ単位のハート送信対象辞書(dict)
				List<Map<Integer, Integer>> dict = generate_dict_per_page_with_stalker(target_set);
				Map<Integer, Integer> target_per_page = dict.get(0);
				Map<Integer, Integer> number_rank_per_page = dict.get(1);
				number_rank_list = new ArrayList<Integer>();
				for (int i = 0; i < 4; i++) {
					number_rank_list.add(number_rank_per_page.get(Integer.valueOf(i)));
				}
				// ページ単位のハート送信対象があればハート送信を実施する
				if (0 < target_per_page.size()) {
					// 回復タップ位置セット
					int row = new ArrayList<Integer>(target_per_page.values()).get(0).intValue();
					// 最下位位置セット
					int max_number_rank = number_rank_per_page.get(Integer.valueOf(3));
					// タイムアウト設定
					LocalDateTime else_datetime = LocalDateTime.MIN;
					// if (Objects.nonNull(BS5Status.get_last_time_last_send_datetime())) {
					// else_datetime = BS5Status.get_last_time_last_send_datetime();
					// }
					LocalDateTime last_send_datetime_per_page = target_per_page.entrySet().stream()
						.map(entry -> {
							Integer no = entry.getKey();
							LocalDateTime ldt = GillsActivity.get_send_datetime(no);
							if (Objects.isNull(ldt)) {
								return LocalDateTime.MIN;
							} else if (ldt.isEqual(LocalDateTime.MAX)) {
								return LocalDateTime.MIN;
							}
							return ldt;
						})
						.sorted(Comparator.reverseOrder())
						.findFirst()
						.orElse(LocalDateTime.MIN);
					if (last_send_datetime_per_page.isEqual(LocalDateTime.MAX)) {
						myLogger.warn(String.format(
							"last_send_datetime_per_page is incorrect, %1$s",
							myDateTime.formatter.format(last_send_datetime_per_page)));
						last_send_datetime_per_page = else_datetime;
					}
					LocalDateTime limit_datetime = last_send_datetime_per_page
						.plusSeconds(LIMIT_SECONDS_POPS_UP_HEART);
					// ページ単位のハート送信実施
					boolean is_timeout = false;
					while (!is_timeout) {
						if (Objects.nonNull(BS5Regions.hearts.exists(BS5Patterns.heart_full_093, WAIT_TIMEOUT))) {
							Spliterator<Match> spliterator = Spliterators
								.spliteratorUnknownSize(BS5Regions.hearts.findAll(BS5Patterns.heart_full_093), 0);
							Stream<Match> stream = StreamSupport.stream(spliterator, false);
							Comparator<Match> comparator = Comparator.comparing(Match::getY).reversed();
							List<Match> sorted_hearts = stream.sorted(comparator).collect(Collectors.toList());
							for (Match heart : sorted_hearts) {
								int offset_heart_y = heart.y - (BS5Regions.rank_list.y - 2);
								int num_index = (int) (offset_heart_y / C_HEIGHT_OF_ONE_BLOCK_INT);
								if (num_index < 0) {
									mySS.take("heart_pos_-1");
									num_index = 0;
								}
								if (3 < num_index) {
									mySS.take("heart_pos_+1");
									num_index = 3;
								}
								Integer number_rank = number_rank_per_page.get(num_index);
								myLogger.info(String.format("heart(%1$d,%2$d), %3$d位,実際(Tsum)=%4$s",
									heart.x - BS5Regions.tsum.x,
									heart.y - BS5Regions.tsum.y,
									number_rank,
									myDateTime.formatter.format(TsumClock.now())));
								// ♥を贈る
								BS5Status.update_send_datetime(number_rank, null, HeartSendState.HEART_TAP);
								LocalDateTime result = give_a_heart(heart, number_rank);
								if (Objects.nonNull(result)) {
									// 最初に♥を送信した日時を更新
									if (Objects.isNull(BS5Status.get_first_send_datetime())) {
										BS5Status.set_first_send_datetime(result);
										// ファイル日時の設定
										myDateTime.set_FileDateTime(BS5Status.get_first_send_datetime());
									}
									count_hearts += 1;
									// ♥を送信した日時を更新
									BS5Status.update_send_datetime(number_rank, result, HeartSendState.COLOURFUL_TSUM);
									// 最終送信日時を記憶
									BS5Status.set_last_send_datetime(result);
									// 全体のハート送信対象から抜く
									try {
										myLogger.fine(String.format("♥%1$d個,%2$d位,%3$s",
											count_hearts,
											number_rank,
											myDateTime.formatter.format(result)));
										myLogger.fine(String.format("♥%1$d個,%2$d位,%3$s (truncated)",
											count_hearts,
											number_rank,
											myDateTime.formatter.format(TsumClock.tsum_date_time(result)
												.truncatedTo(ChronoUnit.SECONDS))));
										if (!target_set.remove(number_rank)) {
											myLogger.warn(String.format("♥二重送信の疑い,%1$d位", number_rank));
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
									// ページ単位のハート送信対象から抜く
									try {
										if (Objects.isNull(target_per_page.remove(number_rank))) {
											// mySS.take("ページ移動か二重送信の疑い");
											myLogger.fine(String.format("ページ移動か二重送信の疑い,%1$d位", number_rank));
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
									// ♥を送信した
									BS5Status.set_returned_to_title(false);
								} else {
									// ♥送信失敗
									BS5Status.get_hearts_sent_failure().add(Integer.valueOf(number_rank));
								}
								// ランキング表示に戻れているかチェック
								recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);
								if (BS5Status.is_returned_to_title()) {
									// 自身の位置再取得
									int number_of_my_rank = to_my_ranking();
									BS5Status.set_number_of_my_rank(number_of_my_rank);
								}
								if (target_per_page.size() > 0) {
									// ページ移動が発生していないかチェック
									// ※ページ単位のハート送信対象がなくなったらループを抜けるのでそれ以外の時だけ実施
									// キャッシュクリア
									number_rank_list_cache = null;
									seek_resume_position(max_number_rank, true);
								}
							}
						}
						// ♥見つけられなかった
						else {
							// ♥湧き待ちタイムアウトであるか
							if (limit_datetime.isAfter(LocalDateTime.now())) {
								LocalDateTime ldt = TsumClock.now();
								LocalDateTime ldt_for_check = LocalDateTime.now();
								Iterator<Integer> iterator_integer = target_set.iterator();
								if (iterator_integer.hasNext()) {
									Integer no = iterator_integer.next();
									ldt = GillsActivity.get_send_datetime(no);
									if (Objects.nonNull(ldt)) {
										if (!ldt.isEqual(LocalDateTime.MAX)) {

											// コントロール
											LocalDateTime ldt_in_regards = ldt
												.truncatedTo(ChronoUnit.SECONDS)
												.plus(1, ChronoUnit.HOURS);
											// ldt_for_check = TsumClock.system_date_time(ldt_in_regards);
											ldt_for_check = ldt_in_regards.minus(TsumClock.get_offset_nanos(),
												ChronoUnit.NANOS);

											myLogger.info(String.format(
												"湧き待ち,%1$d位,記録(Tsum)=%2$s,理想(Tsum)=%3$s,目標(System)=%4$s",
												no,
												myDateTime.formatter.format(ldt),
												myDateTime.formatter.format(ldt_in_regards),
												myDateTime.formatter.format(ldt_for_check)));
										}
									} else {
										ldt = TsumClock.now();
										myLogger.fine(String.format("送信日時無し,%1$d位,%2$s",
											no,
											myDateTime.formatter.format(ldt)));
									}
								}
								// 処理時間計測 start
								long start_datetime = System.nanoTime();
								// 上下スワイプ start
								// if (8 < BS5Status.get_number_of_members()) {
								// // 上下振り
								// if (8 < max_number_rank) {
								// swipe_n(-5.0d);
								// swipe_n(5.0d);
								// } else {
								// swipe_n(5.0d);
								// swipe_n(-5.0d);
								// }
								// } else {
								// show_high_score(row);
								// }
								// try {
								// // キャッシュクリア
								// number_rank_list_cache = null;
								// // ページ移動が発生していないかチェック
								// seek_resume_position_with_check_row4(max_number_rank, true);
								// } catch (Exception e) {
								// // ランキング表示に戻れているかチェック
								// recovery_to_ranking(C_LIMIT_SECONDS_RECOVERY_TO_RANKING);
								// }
								// 上下スワイプ end

								// ハイスコア表示→非表示 start

								// myLogger.info(String.format("湧き待ち期限日時,%1$s",
								// myDateTime.formatter.format(ldt_for_check)));
								if (ldt_for_check.isAfter(LocalDateTime.now())) {
									// ハイスコア表示→非表示で湧き待ち試み
									show_and_hide_high_score(row, ldt_for_check);
								} else {
									// 湧き待ち保険発動
									myLogger.warn("湧き待ち保険発動");
									if (8 < BS5Status.get_number_of_members()) {
										// 上下振り
										if (8 < max_number_rank) {
											swipe_n(-5.0d);
											swipe_n(5.0d);
										} else {
											swipe_n(5.0d);
											swipe_n(-5.0d);
										}
									} else {
										show_and_hide_high_score(row);
									}
									try {
										// キャッシュクリア
										number_rank_list_cache = null;
										// ページ移動が発生していないかチェック
										seek_resume_position_with_check_row4(max_number_rank, true);
									} catch (Exception e) {
										// ランキング表示に戻れているかチェック
										recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);
									}
								}
								// ハイスコア表示→非表示 end

								// 処理時間計測 end
								long elapsed_time = System.nanoTime() - start_datetime;
								myLogger.fine(String.format("湧き待ち elapsed time = %1$d (ms)", elapsed_time / 1000000));
							}
							// ♥湧き待ちタイムアウト
							else {
								is_timeout = true;
								// フリーズ検出のためのハイスコア表示
								show_and_hide_high_score(row);
							}
							// ハート送信失敗集合の中に含まれているか？
							// →含まれているのだとするとリトライなので、試行済みのため湧き待ち不要なので、集合から削除する
							for (Integer failure_number_rank : BS5Status.get_hearts_sent_failure()) {
								if (target_per_page.containsKey(failure_number_rank)) {
									myLogger.warn(String.format("♥未送信の疑い,%1$d位", failure_number_rank));
									// ♥を送信した日時を更新
									BS5Status.update_send_datetime(failure_number_rank, null,
										HeartSendState.COLOURFUL_TSUM);
									// 全体のハート送信対象から抜く
									try {
										target_set.remove(failure_number_rank);
									} catch (Exception e) {
										e.printStackTrace();
									}
									// ページ単位のハート送信対象から抜く
									try {
										target_per_page.remove(failure_number_rank);
									} catch (Exception e) {
										e.printStackTrace();
									}
									// ハート送信失敗集合から抜く
									try {
										BS5Status.get_hearts_sent_failure().remove(failure_number_rank);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						}
						// ページ単位のハート送信対象がなくなったらループを抜ける
						if (target_per_page.size() <= 0) {
							if (Objects
								.isNull(BS5Regions.hearts.exists(BS5Patterns.heart_full_093, WAIT_TIMEOUT))) {
								break;
							} else {
								// ページ内にハートがまだ残っているかもしれない
								// 再度、ページ単位のハート送信対象に詰める
								Spliterator<Match> spliterator = Spliterators.spliteratorUnknownSize(
									BS5Regions.hearts.findAll(BS5Patterns.heart_full_093), 0);
								Stream<Match> stream = StreamSupport.stream(spliterator, false);
								Comparator<Match> comparator = Comparator.comparing(Match::getY).reversed();
								List<Match> sorted_hearts = stream.sorted(comparator).collect(Collectors.toList());
								for (Match heart : sorted_hearts) {
									int offset_heart_y = heart.y - (BS5Regions.rank_list.y - 2);
									int num_index = (int) (offset_heart_y / C_HEIGHT_OF_ONE_BLOCK_INT);
									if (num_index < 0) {
										mySS.take("heart_pos_-1");
										num_index = 0;
									}
									if (3 < num_index) {
										mySS.take("heart_pos_+1");
										num_index = 3;
									}
									Integer number_rank = number_rank_per_page.get(num_index);
									myLogger.fine(String.format("heart catch, %1$d位", number_rank));
									try {
										target_per_page.put(number_rank, row);
									} catch (Exception e) {
										mySS.take("heart_catch_E");
										myLogger.info("    heart_catch_E");
									}
								}
							}
						}
						// 想定される時間を過ぎても♥が湧かない？
						if (Objects.nonNull(BS5Status.get_last_time_first_send_datetime())) {
							if (Objects.isNull(BS5Status.get_first_send_datetime())) {
								LocalDateTime next_send_datetime = BS5Status.get_last_time_first_send_datetime()
									.plusSeconds(AFTER_SECONDS_POPS_UP_HEART);
								if (next_send_datetime.isBefore(LocalDateTime.now())) {
									myLogger.warn("想定される時間を過ぎても♥が湧かない→ホーム画面表示");
									to_home_and_return();
								}
							}
						}
						// 延長遅延検出した？
						if (BS5Status.is_extended_delay()) {
							myLogger.warn("ツム時計同期");
							to_home_and_return();
							BS5Status.set_extended_delay(false);
						}
					} // while
					if (is_timeout) {
						myLogger.warn(String.format(
							"♥湧き待ちタイムアウト, %1$s",
							myDateTime.formatter.format(limit_datetime)));
						for (Integer number_rank : target_per_page.keySet()) {
							// 全体のハート送信対象から抜く
							try {
								if (target_set.remove(number_rank)) {
									myLogger.warn(String.format("♥未送信の疑い,%1$d位", number_rank));
									mySS.take_screen(String.format("%1$d位_♥未送信の疑い", number_rank));
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			} catch (RetryOverException e) {
				// リトライオーバー発生時
				// スタックトレース出力
				myLogger.error(e);
				throw e;
			} catch (Exception e) {
				// その他例外発生時
				// スタックトレース出力
				e.printStackTrace();
				continue;
			}
			// ハート未送信がなくなったらループを抜ける
			if (target_set.size() <= 0) {
				// mySS.take("終了判定");
				break;
			}
			// mySS.take("スワイプ前");
			// レジューム位置シーク
			Iterator<Integer> iterator = target_set.iterator();
			seek_resume_position(iterator.next());
			// mySS.take("スワイプ後");
		}

		return target_set;
	}

	/**
	 * ♥送信モレフォローアップ.
	 * <p>
	 * ニックネーム：多い日も安心
	 * 
	 * @param lowest_number_rank 最下位ランキングNo.
	 * @throws RetryOverException リトライオーバー
	 */
	public static void follow_up_missing_hearts(Set<Integer> target_set) throws RetryOverException {
		List<Integer> number_rank_list = null;

		// キャッシュクリア
		number_rank_list_cache = null;
		// レジューム位置シーク
		int number_of_next_rank = BS5Status.get_number_of_members();
		Iterator<Integer> iterator_integer = target_set.iterator();
		if (iterator_integer.hasNext()) {
			number_of_next_rank = iterator_integer.next().intValue();
		}
		seek_resume_position(number_of_next_rank);

		while (true) {
			try {
				// ページ単位のハート送信対象辞書(dict)
				List<Map<Integer, Integer>> dict = generate_dict_per_page_with_stalker(target_set);
				Map<Integer, Integer> target_per_page = dict.get(0);
				Map<Integer, Integer> number_rank_per_page = dict.get(1);
				number_rank_list = new ArrayList<Integer>();
				for (int i = 0; i < 4; i++) {
					number_rank_list.add(number_rank_per_page.get(Integer.valueOf(i)));
				}
				// ページ単位のハート送信対象があればハート送信を実施する
				if (0 < target_per_page.size()) {
					// 最下位位置セット
					int max_number_rank = number_rank_per_page.get(Integer.valueOf(3));
					if (Objects.nonNull(BS5Regions.hearts.exists(BS5Patterns.heart_full_093, WAIT_TIMEOUT))) {
						Spliterator<Match> spliterator = Spliterators
							.spliteratorUnknownSize(BS5Regions.hearts.findAll(BS5Patterns.heart_full_093), 0);
						Stream<Match> stream = StreamSupport.stream(spliterator, false);
						Comparator<Match> comparator = Comparator.comparing(Match::getY).reversed();
						List<Match> sorted_hearts = stream.sorted(comparator).collect(Collectors.toList());
						for (Match heart : sorted_hearts) {
							int offset_heart_y = heart.y - (BS5Regions.rank_list.y - 2);
							int num_index = (int) (offset_heart_y / C_HEIGHT_OF_ONE_BLOCK_INT);
							if (num_index < 0) {
								mySS.take("heart_pos_-1");
								num_index = 0;
							}
							if (3 < num_index) {
								mySS.take("heart_pos_+1");
								num_index = 3;
							}
							Integer number_rank = number_rank_per_page.get(num_index);
							myLogger.info(String.format("heart(%1$d,%2$d), %3$d位,%4$s",
								heart.x - BS5Regions.tsum.x,
								heart.y - BS5Regions.tsum.y,
								number_rank,
								myDateTime.formatter.format(TsumClock.now())));
							// ♥を贈る
							BS5Status.update_send_datetime(number_rank, null, HeartSendState.HEART_TAP);
							LocalDateTime result = give_a_heart(heart, number_rank);
							if (Objects.nonNull(result)) {
								// 最初に♥を送信した日時を更新
								if (Objects.isNull(BS5Status.get_first_send_datetime())) {
									BS5Status.set_first_send_datetime(result);
									// ファイル日時の設定
									myDateTime.set_FileDateTime(BS5Status.get_first_send_datetime());
								}
								count_hearts += 1;
								myLogger.info(String.format("♥送信モレをキャッチ、%1$d位", number_rank));
								// ♥を送信した日時を更新
								BS5Status.update_send_datetime(number_rank, result, HeartSendState.COLOURFUL_TSUM);
								// 最終送信日時を記憶
								BS5Status.set_last_send_datetime(result);
								// 全体のハート送信対象から抜く
								try {
									target_set.remove(number_rank);
								} catch (Exception e) {
									e.printStackTrace();
								}
								// ページ単位のハート送信対象から抜く
								try {
									target_per_page.remove(number_rank);
								} catch (Exception e) {
									e.printStackTrace();
								}
								// ♥を送信した
								BS5Status.set_returned_to_title(false);
								// 多い日も安心で送信した場合の送信後待機時間
								my.sleep(800);
							} else {
								// ♥送信失敗
								BS5Status.get_hearts_sent_failure().add(Integer.valueOf(number_rank));
							}
							// ランキング表示に戻れているかチェック
							recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);
							if (BS5Status.is_returned_to_title()) {
								// 自身の位置再取得
								BS5Status.set_number_of_my_rank(to_my_ranking());
							}
							if (target_per_page.size() > 0) {
								// ページ移動が発生していないかチェック
								// ※ページ単位のハート送信対象がなくなったらループを抜けるのでそれ以外の時だけ実施
								// キャッシュクリア
								number_rank_list_cache = null;
								seek_resume_position(max_number_rank, true);
							}
						}
					} else {
						for (Iterator<Integer> iterator = target_per_page.keySet().iterator(); iterator.hasNext();) {
							Integer number_rank = (Integer) iterator.next();
							try {
								myLogger.fine(String.format("♥なし、%1$d位", number_rank));
								target_set.remove(number_rank);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						target_per_page.clear();
					}
				}
			} catch (RetryOverException e) {
				// リトライオーバー発生時
				// スタックトレース出力
				e.printStackTrace();
				throw e;
			} catch (Exception e) {
				// その他例外発生時
				// スタックトレース出力
				e.printStackTrace();
				continue;
			}
			// ハート未送信がなくなったらループを抜ける
			if (target_set.size() <= 0) {
				break;
			}
			// レジューム位置シーク
			Iterator<Integer> iterator = target_set.iterator();
			seek_resume_position(iterator.next());
		}
		myLogger.fine("フォロー終了");
	}

	/**
	 * 前処理.
	 */
	public static void pre_process() {
		while (true) {
			// ハート送信前処理
			try {
				// ツム再起動＆Regionセット
				Region reg_tsum = stop_and_create(LIMIT_SECONDS_RESTART);
				if (Objects.isNull(reg_tsum)) {
					// ツム再起動失敗ならやり直し
					continue;
				}
				BS5Regions.set_region(reg_tsum);

				// ツム時計取得
				LocalDateTime limit_datetime = BS5Status.get_next_datetime().plusMinutes(OFFSET_MINUTES_GILLS_CREATE);
				if (BS5Status.get_next_datetime().getDayOfWeek().equals(DayOfWeek.MONDAY)) {
					limit_datetime = limit_datetime.plusMinutes(OFFSET_MINUTES_FOR_MONDAY);
				}
				myLogger.info(String.format("TsumClock limit datetime = %1$s",
					myDateTime.formatter.format(limit_datetime)));
				// Long offset_nanos = TsumClock.lottery_tsum_clock(limit_datetime);
				// if (Objects.nonNull(offset_nanos)) {
				// myLogger.info(String.format("offset, %1$d(ms)", offset_nanos / 1000000));
				// }

				// ♥受け取り
				take_all_hearts(LIMIT_SECONDS_RECOVERY_TO_RANKING);
				recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);

				// 時計を取得する
				boolean isGetDate = false;
				LocalDateTime remoteDateTime = null;
				for (int i = 0; i < 1 || !isGetDate; i++) {
					while (!isGetDate) {
						remoteDateTime = ADBWrapper.get_datetime();
						if (Objects.isNull(remoteDateTime)) {
							// エミュ再起動
							bs_restart(LIMIT_SECONDS_RESTART);
							// ツム再起動＆Regionセット
							reg_tsum = stop_and_create(LIMIT_SECONDS_RESTART);
							BS5Regions.set_region(reg_tsum);
						} else {
							myLogger.info(String.format("get date : %s", myDateTime.formatter.format(remoteDateTime)));
							isGetDate = true;
						}
					}
				}

				// 生存通知
				myDateTime.set_datetime(LocalDateTime.now(), DATETIME_ALIVE);

				// 自身の位置取得
				int number_of_my_rank = to_my_ranking();
				BS5Status.set_number_of_my_rank(number_of_my_rank);
				// ランキング最下位へスワイプ
				swipe_down_to_bottom();
				fit_seperator_line_to_bottom_border();
				// ランキングメンバー数取得
				int number_of_members = -1;
				for (int i = 0; (i < RETRY_PRE_PROCESS) || (number_of_members <= 0); i++) {
					List<Integer> number_rank_list = recognition_numbers_in_a_page();
					if (Objects.isNull(number_rank_list)) {
						// 失敗した
						recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);
						swipe_down_to_bottom();
						fit_seperator_line_to_bottom_border();
						continue;
					}
					number_of_members = number_rank_list.get(2).intValue();
				}
				if (number_of_members <= 0) {
					throw new RetryOverException("ランキングメンバー数取得失敗");
				}
				BS5Status.set_number_of_members(number_of_members);
				myLogger.info_members(String.format("メンバー数, %1$d人", number_of_members));

				// 全メンバー情報を作成・保存
				if (BS5Status.get_next_datetime().getDayOfWeek().equals(DayOfWeek.MONDAY)) {
					// ランキングリセット用
					GillsActivity.create_for_ranking_reset();
				} else {
					// 10分以内の再起動なら作り直さない
					LocalDateTime store_members_list_datetime = myDateTime.get_datetime(BS5Instance.get_datetime_store_members_list());
					LocalDateTime range_datetime = LocalDateTime.now().minus(10, ChronoUnit.MINUTES);
					if (range_datetime.isAfter(store_members_list_datetime)) {
						// 通常用
						GillsActivity.create();
					} else {
						myLogger.info_members(String.format("Store members list datetime = %1$s",
							myDateTime.formatter.format(store_members_list_datetime)));
					}
				}
				GillsActivity.store();
				// 全メンバー情報ストア日時を設定
				myDateTime.set_datetime(LocalDateTime.now(), BS5Instance.get_datetime_store_members_list());

				// ハート未送信集合
				Set<Integer> hearts_send_queue = GillsActivity.get_hearts_send_queue();
				BS5Status.set_hearts_unsent(hearts_send_queue);
				Integer final_number_of_members = number_of_members;
				Set<Integer> filtered_queue = hearts_send_queue.stream()
					.filter(mm -> (mm <= final_number_of_members))
					.collect(Collectors.toCollection(LinkedHashSet::new));
				BS5Status.set_hearts_unsent(filtered_queue);
				Set<Integer> hearts_unsent = BS5Status.get_hearts_unsent();

				// ハート送信失敗集合初期化
				BS5Status.set_hearts_sent_failure(new HashSet<Integer>());
				// キャッシュクリア
				number_rank_list_cache = null;
				// レジューム位置シーク
				int number_of_next_rank = number_of_members;
				Iterator<Integer> iterator_integer = hearts_unsent.iterator();
				if (iterator_integer.hasNext()) {
					number_of_next_rank = iterator_integer.next().intValue();
				}
				seek_resume_position(number_of_next_rank);
				// 最後に抜ける
				break;
			} catch (Exception e) {
				// 例外発生時
				// スタックトレース出力
				e.printStackTrace();
				mySS.take("pre_process_E");
				// ループ再始動
				continue;
			}
		}
	}

	/**
	 * 後処理.
	 */
	public static void post_process() {
		// ハート送信後処理
		try {
			// BS再起動
			bs_restart(LIMIT_SECONDS_RESTART);
		} catch (Exception e) {
			// 例外発生時
			// スタックトレース出力
			e.printStackTrace();
			mySS.take("post_process_E");
			// ツム再起動
			stop_and_create(LIMIT_SECONDS_RESTART);
		}
	}

	/**
	 * LINE認証.
	 * 
	 * @param reg リージョン
	 */
	public static void login_LINE(long limit_seconds) {
		// タイムアウト時刻設定
		LocalDateTime limit_datetime = LocalDateTime.now().plusSeconds(limit_seconds);

		// タイムアウトを待ちながらLINE認証を試みる
		boolean breaked = true;
		Location l = null;
		while (limit_datetime.isAfter(LocalDateTime.now())) {
			try {
				if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.consent_checkbox_094, WAIT_TIMEOUT))) {
					// ADB経由Google Chrome起動
					ADBWrapper.start_GoogleChrome();
					myLogger.fine("ADB経由Google Chrome起動");
					my.sleep(5000);

					// ADB経由ツムツム起動
					ADBWrapper.start_TsumTsum();
					myLogger.fine("ADB経由ツムツム起動");
					my.sleep(2000);

					// TAP TO START
					while (limit_datetime.isAfter(LocalDateTime.now())) {
						if (Objects.nonNull(BS5Regions.tap_to_start.exists(BS5Patterns.tap_to_start_088, WAIT_TIMEOUT))) {
							myLogger.fine("TAP TO STARTを検出");
							my.single_tap_by_Location(BS5Regions.tap_to_start.getTarget());
							break;
						} else {
							my.sleep(500);
						}
					}
					// LINE GAME
					while (limit_datetime.isAfter(LocalDateTime.now())) {
						if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.consent_checkbox_094, WAIT_TIMEOUT))) {
							myLogger.fine("LINE GAME");
							break;
						} else {
							my.sleep(500);
						}
					}
				}

				if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.consent_checkbox_094, WAIT_TIMEOUT))) {
					breaked = false;
					l = BS5Regions.tsum.getLastMatch().getTarget();
					for (int i = 0; i < RETRY_EXISTS; i++) {
						if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.consent_checkbox_094, WAIT_TIMEOUT))) {
							my.println("すべて同意のチェックボックスをタップ");
							my.single_tap_by_Location(l);
						} else {
							breaked = true;
							break;
						}
						my.sleep(1000);
					}
					if (!breaked) {
						throw new RetryOverException("RETRY OVER consent_checkbox");
					}
					l = BS5Regions.tsum.find(BS5Patterns.consent_dialog_accept_088).getTarget();
					breaked = false;
					for (int i = 0; i < RETRY_EXISTS; i++) {
						if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.consent_dialog_accept_088, WAIT_TIMEOUT))) {
							my.println("同意するをタップ");
							my.single_tap_by_Location(l);
						} else {
							breaked = true;
							break;
						}
						my.sleep(1000);
					}
					if (!breaked) {
						throw new RetryOverException("同意するが表示されない");
					}
					breaked = false;
					while (limit_datetime.isAfter(LocalDateTime.now())) {
						if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.LINE_088, WAIT_TIMEOUT))) {
							breaked = true;
							break;
						}
						if (Objects.nonNull(BS5Regions.tap_to_start.exists(BS5Patterns.tap_to_start_088, WAIT_TIMEOUT))) {
							myLogger.fine("TAP TO STARTを検出");
							break;
						}
						my.sleep(1000);
					}
					if (!breaked) {
						throw new RetryOverException("LINE認証が表示されない");
					}
				}
				if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.LINE_088, WAIT_TIMEOUT))) {
					l = BS5Regions.tsum.getLastMatch().getTarget();
					breaked = false;
					myLogger.info("LINE認証");
					if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.textbox_mailaddress_088, WAIT_TIMEOUT))) {
						App.setClipboard(BS5Instance.get_line_id());
						my.long_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
						my.sleep(1000);
						my.println("メールアドレス貼り付け");
						if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.menu_paste_088, 2))) {
							my.println("貼り付けをタップ");
							my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
							my.sleep(500);
						}

						// BS5Regions.tsum.paste(my.C_USER_ID);
						// my.sleep(500);
						my.single_tap_by_Location(l);
						my.sleep(500);
					}
					if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.textbox_password_088, WAIT_TIMEOUT))) {
						App.setClipboard(BS5Instance.get_line_password());
						my.long_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
						my.sleep(1000);
						my.println("パスワード貼り付け");
						if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.menu_paste_088, 2))) {
							my.println("貼り付けをタップ");
							my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
							my.sleep(500);
						}
						// BS5Regions.tsum.paste(my.C_PASSWORD);
						// my.sleep(500);
						my.single_tap_by_Location(l);
						my.sleep(500);
					}
					if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.login_reCAPTCHA_088, WAIT_TIMEOUT))) {
						mySS.take("画像に表示されている文字を入力してください");
						my.println("画像に表示されている文字を入力してください");
						try {
							LineNotify lineNotify = new LineNotify(LINE_API_TOKEN_ALERT);
							lineNotify.notify("\n画像に表示されている文字を入力してください");
							my.sleep(5000);

							my.println("Exiting by LINE認証");
							myLogger.close();
							if (Objects.nonNull(my.r)) {
								my.r.waitForIdle();
								my.sleep(100);
							}
							my.println("Exit");
							my.exit(0);
						} catch (Exception e) {
							// 例外発生時
							// スタックトレース出力
							myLogger.error(e);
						}
					} else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.login_094, WAIT_TIMEOUT))) {
						// mySS.take("ログインをタップ");
						my.println("ログインをタップ");
						my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
						my.sleep(1000);
					}
				}
				l = null;
				if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.accept_088, WAIT_TIMEOUT))) {
					breaked = false;
					l = BS5Regions.tsum.getLastMatch().getTarget();
					my.sleep(500);
				}
				if (Objects.nonNull(l)) {
					while (limit_datetime.isAfter(LocalDateTime.now())) {
						my.println("許可するをタップ");
						my.single_tap_by_Location(l);
						if (Objects.isNull(BS5Regions.tsum.exists(BS5Patterns.accept_088, WAIT_TIMEOUT))) {
							breaked = true;
							break;
						}
						my.sleep(500);
					}
				}
				// 許可するをタップしていれば終了
				if (breaked) {
					break;
				}
				// 上段とじる
				if (Objects.nonNull(BS5Regions.close_upper.exists(BS5Patterns.close_088, WAIT_TIMEOUT))) {
					myLogger.fine("上段とじるをタップ");
					my.single_tap_by_Location(BS5Regions.close_upper.getTarget());
				}
				// ルート化を検出黒通知：許可する
				else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.root_exist_088, WAIT_TIMEOUT))) {
					myLogger.fine("ルート化を検出黒通知：許可するをタップ");
					my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
					BS5Status.set_root_exist_popup(true);
				}
				// TAP TO START
				else if (Objects.nonNull(BS5Regions.tap_to_start.exists(BS5Patterns.tap_to_start_088, WAIT_TIMEOUT))) {
					myLogger.fine("TAP TO STARTを検出");
					my.single_tap_by_Location(BS5Regions.tap_to_start.getTarget());
				}
			} catch (Exception e) {
				mySS.take("login_LINE_E");
			}
		}
	}

	/**
	 * ツムツムアプリ終了から起動.
	 * 
	 * @param limit_seconds 制限時間(s)
	 * @return フォーカスセットしたウィンドウのリージョン
	 */
	public static Region stop_and_create(long limit_seconds) {
		// タイムアウト時刻設定
		LocalDateTime limit_datetime = LocalDateTime.now().plusSeconds(limit_seconds);

		return stop_and_create(limit_datetime);
	}

	/**
	 * ツムツムアプリ終了から起動.
	 * 
	 * @param limit_datetime 制限時刻
	 * @return フォーカスセットしたウィンドウのリージョン
	 */
	public static Region stop_and_create(LocalDateTime limit_datetime) {
		// my.println("stub ツムツムアプリ終了から起動");

		Region reg_tsum = null;
		boolean breaked = false;
		long limit_seconds_remain;

		if (BS5App.is_running(2)) {
			reg_tsum = BS5App.focus();
		}

		// フリーズ対策
		boolean isFreezed = false;
		LocalDateTime limit_datetime_freezed = LocalDateTime.now().plusSeconds(LIMIT_SECONDS_FREEZED);
		ScreenImage baseImage = mySS.take_for_freezed_exists(reg_tsum, "temp");
		Pattern basePattern = new Pattern(baseImage).exact();

		myLogger.info("ツムツム再起動");
		// タイムアウトを待ちながらツムツム起動を試みる
		while (limit_datetime.isAfter(LocalDateTime.now())) {
			try {

				// ADB接続
				if (!ADBWrapper.connect_adb()) {
					// BS再起動
					limit_seconds_remain = ChronoUnit.SECONDS.between(LocalDateTime.now(), limit_datetime);
					bs_restart(limit_seconds_remain);
					continue;
				}

				// ADB経由ツムツム停止
				ADBWrapper.stop_TsumTsum();
				myLogger.fine("ADB経由ツムツム停止");

				breaked = false;
				for (int i = 0; i < RETRY_EXISTS; i++) {
					my.sleep(1000);

					// マイアプリが表示されるまで待つ
					if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.bs5_gamecenter_088, WAIT_TIMEOUT))) {
						breaked = true;
						break;
					}
				}
				if (!breaked) {
					throw new RetryOverException("マイアプリが表示されない");
				}

				BS5Status.set_root_exist_popup(false);

				// ADB経由ツムツム起動
				ADBWrapper.start_TsumTsum();
				myLogger.fine("ADB経由ツムツム起動");

				my.sleep(2000);

				// Regionセット
				reg_tsum = BS5App.focus();
				BS5Regions.set_region(reg_tsum);

				// スクリーンキャプチャ
				limit_datetime_freezed = LocalDateTime.now().plusSeconds(LIMIT_SECONDS_FREEZED);
				baseImage = mySS.take_for_freezed_exists(reg_tsum, "temp");
				basePattern = new Pattern(baseImage).exact();

				// TAP TO START表示待ち
				Location l1 = new Location(BS5Regions.tsum.x + 440, BS5Regions.tsum.y + 612);
				// Location l2 = new Location(BS5Regions.tsum.x + 80, BS5Regions.tsum.y + 680);
				Location l2 = new Location(BS5Regions.tsum.x + 440, BS5Regions.tsum.y + 680);
				breaked = false;
				for (int i = 0; i < RETRY_EXISTS; i++) {
					if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.tap_to_start_088, WAIT_TIMEOUT))) {
						breaked = true;
						// 正常に抜ける場合は、ブラックアウトフラグを落とす
						isFreezed = false;
						// キャッシュクリア
						number_rank_list_cache = null;
						break;
					}

					// BS5アップデート：閉じる
					if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.bs5_updater_close_090, WAIT_TIMEOUT))) {
						myLogger.warn("BS5アップデート：×をタップ");
						mySS.take("BS5アップデート");
						my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
						my.sleep(500);
					}
					// BS5エンジン再起動：閉じる
					else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.bs5_engine_reboot_090, WAIT_TIMEOUT))) {
						myLogger.warn("BS5エンジン再起動をタップ");
						mySS.take("BS5エンジン再起動");
						my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
						my.sleep(500);
					}
					// BS5おすすめアプリ：閉じる
					else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.bs5_recommends_close_090, WAIT_TIMEOUT))) {
						myLogger.warn("BS5おすすめアプリ：×をタップ");
						mySS.take("BS5おすすめアプリ");
						my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
						my.sleep(500);
					}
					// BS5パフォーマンスに関する警告：続行する
					else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.bs5_continue_090, WAIT_TIMEOUT))) {
						myLogger.warn("BS5パフォーマンスに関する警告：続行する");
						mySS.take("BS5パフォーマンスに関する警告");
						my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
					}
					// ルート化を検出黒通知
					else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.root_exist_088, WAIT_TIMEOUT))) {
						myLogger.fine("ルート化を検出黒通知：許可するをタップ");
						BS5Status.set_root_exist_popup(true);
						my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
						my.sleep(500);
					}
					// とじるボタン
					else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.close_088, WAIT_TIMEOUT))) {
						my.println("とじるをタップ");
						mySS.take("とじる");
						my.single_tap_by_Location(BS5Regions.tsum.getLastMatch().getTarget());
						my.sleep(500);
					}
					// ゲームセンター画面→ADB経由ツムツム起動
					else if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.bs5_gamecenter_088, WAIT_TIMEOUT))) {
						myLogger.warn("ツム落ち発生？");
						mySS.take("ツム落ち疑惑");
						// ADB経由ツムツム起動
						ADBWrapper.start_TsumTsum();
						my.sleep(2000);
					}
					// どこにも引っかからなければ。。。
					else {
						// ブラックアウト画像を判定する
						if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.blackout_094, 1))) {
							my.sleep(4000);
							my.single_tap_by_Location(l2);
							my.single_tap_by_Location(l1);
							// 2度読みしてみる
							my.sleep(6000);
							if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.blackout_094, 1))) {
								// ブラックアウトと判定
								isFreezed = true;
								myLogger.fine("ブラックアウト疑惑");
								if (LocalDateTime.now().isAfter(limit_datetime_freezed)) {
									myLogger.info("ブラックアウト確定");
									// LineNotify lineNotify = new LineNotify(LINE_API_TOKEN_ALERT);
									// lineNotify.notify("\nブラックアウト");
									// forループを抜ける
									break;
								}
							}
						} else
						// ブラックアウト2画像を判定する
						if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.blackout2_094, 1))) {
							// 2度読みしてみる
							if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.blackout2_094, 1))) {
								// ブラックアウトと判定
								isFreezed = true;
								myLogger.info("ブラックアウト2確定");
								// forループを抜ける
								break;
							}
						}
						// 正常
						else {
							isFreezed = false;
						}
					}

					// LINE認証
					limit_seconds_remain = ChronoUnit.SECONDS.between(LocalDateTime.now(), limit_datetime);
					login_LINE(limit_seconds_remain);

					my.println("オープニングをタップ");
					my.single_tap_by_Location(l2);
					my.single_tap_by_Location(l1);

					// my.sleep(250);

					// フリーズ検出
					if (Objects.isNull(reg_tsum.exists(basePattern, 0.250))) {
						// 画面が異なれば更新する
						limit_datetime_freezed = LocalDateTime.now().plusSeconds(LIMIT_SECONDS_FREEZED);
						baseImage = mySS.take_for_freezed_exists(reg_tsum, "temp");
						basePattern = new Pattern(baseImage).exact();
					}
					// 同じ画面が表示され続けているならフリーズ確定
					else if (LocalDateTime.now().isAfter(limit_datetime_freezed)) {
						// フリーズと判定
						isFreezed = true;
						myLogger.info("フリーズ確定");
						break;
					}
				}
				if (!breaked) {
					// フリーズ対策
					if (isFreezed &&
						(limit_datetime_freezed.isBefore(LocalDateTime.now()))) {
						// フリーズした
						myLogger.warn("フリーズ");
						mySS.take_screen("フリーズ");
						isFreezed = false;
						// ツムツム再起動
						myLogger.info("ツムツム再起動 by フリーズ");
						continue;
					}

					throw new RetryOverException("TAP TO STARTが表示されない");
				}

				// 週間ランキングへ戻る
				breaked = false;
				limit_seconds_remain = ChronoUnit.SECONDS.between(LocalDateTime.now(), limit_datetime);
				recovery_to_ranking(limit_seconds_remain, false);
			} catch (Exception e) {
				// スタックトレース出力
				e.printStackTrace();
				// BS再起動
				mySS.take("BS5再起動");
				bs_restart(LIMIT_SECONDS_RESTART);
				continue;
			}
			breaked = true;
			break;
		}
		if (!breaked) {
			myLogger.info("タイムアウト：ツムツム再起動");
			mySS.take_screen("stop_and_start_failed");
			return null;
		}

		return reg_tsum;
	}

	/**
	 * BS5再起動.
	 * 
	 * @param limit_seconds 制限時間(s)
	 */
	public static void bs_restart(long limit_seconds) {

		class QuitTimer extends TimerTask {
			Thread targeThread = null;

			public QuitTimer(Thread target) {
				this.targeThread = target;
			}

			@Override
			public void run() {
				this.targeThread.interrupt();
			}
		}

		// タイムアウト時刻設定
		LocalDateTime limit_datetime = LocalDateTime.now().plusSeconds(limit_seconds);
		LocalDateTime limit_datetime_start = LocalDateTime.now().plusSeconds(limit_seconds);

		Region reg = Screen.getPrimaryScreen();
		Region screen = Screen.getPrimaryScreen();
		boolean breaked = false;

		// BS5App.focus();
		my.println("BS5再起動");
		// タイムアウトを待ちながらBS5の停止→開始→ADB接続を試みる
		while (limit_datetime.isAfter(LocalDateTime.now())) {
			try {
				// タイムアウトを待ちながらBS5の停止を試みる
				while (limit_datetime.isAfter(LocalDateTime.now())) {
					// BS5を強制終了
					bs_force_exit(30);
					my.sleep(3000);
					if (BS5App.is_running(2)) {
						try {
							reg = BS5App.focus();
							my.println("BS5 has window.");
							my.sleep(800);
							// Location l = reg.getTopRight().offset(-50, 16);
							// my.single_tap_by_Location(l);
							// my.sleep(3000);

							bs_force_exit(30);

						} catch (Exception e) {
							my.println("BS5 doesn't have window.");
						}

						my.sleep(10);
						my.println("BS5停止");

						// タイマー設定
						QuitTimer task = new QuitTimer(Thread.currentThread());

						breaked = false;
						Timer timer = new Timer();
						try {
							timer.schedule((TimerTask) task, 20000);

							for (int i = 0; i < RETRY_EXISTS; i++) {
								if (!BS5App.is_running(2)) {
									my.println("BS5終了");
									breaked = true;
									break;
								}
								if (Objects.nonNull(screen.exists(BS5Patterns.bs5_confirm_exit_090, WAIT_TIMEOUT))) {
									my.single_tap_by_Location(screen.getLastMatch().getTarget());
								} else if (Objects.nonNull(screen.exists(BS5Patterns.bs5_exit_088, WAIT_TIMEOUT))) {
									my.single_tap_by_Location(screen.getLastMatch().getTarget());
								}
								my.println("BS5 is Running");
								Thread.sleep(1000);
								BS5App.instance = null;
							}

							// 抜けてきたということはフリーズはしていない⇒タイマーキャンセル
							timer.cancel();
						} catch (InterruptedException e) {
							myLogger.error("BS5フリーズ疑惑");
							mySS.take("BS5フリーズ疑惑");
							bs_force_exit(30);
						}
						// 念のためタイマー停止
						timer.cancel();

						if (!breaked) {
							my.println("BS5停止失敗");
							mySS.take("BS5停止失敗");
							// まっくろくろすけ対策
							try {
								my.println("BS5 has window.");
								bs_force_exit(30);

							} catch (Exception e) {
								my.println("BS5 doesn't have window.");
							}
							continue;
						}
						my.println("........Complete.");
						break;
					} else {
						my.println("BS5停止 Done.");
						break;
					}
				}
				// タイムアウトを待ちながらBS5の起動を試みる
				while (limit_datetime.isAfter(LocalDateTime.now())) {
					if (!BS5App.is_running(1)) {
						BS5App.open();
						my.println("BS5開始");
						my.sleep(1000);
						BS5App.instance = null;
					} else {
						breaked = false;
						for (int i = 0; i < RETRY_EXISTS; i++) {
							if (BS5App.is_running(2)) {
								// ウィンドウ移動&リサイズ
								reg = BS5App.move_resize_window(0, 0, 568, 983);
							}

							if (Objects.nonNull(screen.exists(BS5Patterns.bs5_gamecenter_088, 1.0f))) {
								my.println("ゲームセンターを見つけた");
								breaked = true;
								break;
							} else if (Objects.nonNull(screen.exists(BS5Patterns.tsum_app_icon_portrait_088, 1.0f))) {
								breaked = true;
								break;
							}

							if (Objects.nonNull(screen.exists(BS5Patterns.bs5_continue_090, WAIT_TIMEOUT))) {
								myLogger.warn("このまま続行をタップ");
								my.single_tap_by_Location(screen.getLastMatch().getTarget());
								my.sleep(2000);
							} else if (Objects.nonNull(screen.exists(BS5Patterns.bs5_low_memory_ok_090, WAIT_TIMEOUT))) {
								myLogger.warn("メモリの低下");
								my.single_tap_by_Location(screen.getLastMatch().getTarget());
								my.sleep(2000);
							}

							my.sleep(1000);
						}
						if (!breaked) {
							my.println("BS5開始失敗");
							mySS.take("BS5開始失敗");
							continue;
						}

						// BS5開始成功
						my.println("BS5開始成功");
						// ウィンドウ移動&リサイズ
						reg = BS5App.move_resize_window(0, 0, 568, 983);
						break;
					}
				}
				// タイムアウトを待ちながらBS5の起動完了を待つ
				limit_datetime_start = LocalDateTime.now().plusSeconds(LIMIT_SECONDS_START);
				breaked = false;
				while (limit_datetime_start.isAfter(LocalDateTime.now())) {
					if (BS5App.is_running(2)) {
						reg = BS5App.focus();
						if (Objects.nonNull(reg.exists(BS5Patterns.bs5_gamecenter_088, 1.0f))) {
							my.println("ゲームセンターを見つけた");
							breaked = true;
							break;
						} else if (Objects.nonNull(reg.exists(BS5Patterns.tsum_app_icon_portrait_088, WAIT_TIMEOUT))) {
							breaked = true;
							break;
						}

						if (Objects.nonNull(screen.exists(BS5Patterns.bs5_continue_090, WAIT_TIMEOUT))) {
							myLogger.warn("このまま続行をタップ");
							my.single_tap_by_Location(screen.getLastMatch().getTarget());
							my.sleep(2000);
						}

						my.sleep(1000);
						BS5App.instance = null;
					} else {
						break;
					}
				}
				if (!breaked) {
					myLogger.warn("BS5起動完了しない");
					mySS.take("BS5focus");
					mySS.take_screen("BS5起動完了しない");
					continue;
				}
				BS5Regions.set_region(reg);

				my.println("........Complete.");
				BS5Status.set_root_exist_popup(false);

				// ADB接続を試みる
				breaked = false;
				for (int i = 0; i < RETRY_CONNECT_ADB; i++) {
					if (ADBWrapper.connect_adb()) {
						// 成功したらループを抜ける
						myLogger.info("ADB接続成功");
						breaked = true;
						break;
					}
					// 失敗したらやり直し
					myLogger.fine(String.format("ADB接続失敗→試行%1$d回", i + 1));
					ADBWrapper.restart_server_adb();
					my.sleep(1000);
				}
				if (!breaked) {
					myLogger.warn("ADB接続失敗");
					continue;
				} else {
					break;
				}
			} catch (Exception e) {
				// スタックトレース出力
				e.printStackTrace();
			}
		}
		if (!breaked) {
			my.println("タイムアウト：BS5再起動");
			mySS.take_screen("タイムアウト：BS5再起動");
			// 緊急再起動
			my.reboot_system();
		}
	}

	/**
	 * BS5終了.
	 * 
	 * @param limit_seconds 制限時間(s)
	 * @param is_reboot     システム再起動か否か
	 */
	public static void bs_exit(long limit_seconds) {
		if (!BS5App.is_running(2)) {
			return;
		}

		class QuitTimer extends TimerTask {
			Thread targeThread = null;

			public QuitTimer(Thread target) {
				this.targeThread = target;
			}

			@Override
			public void run() {
				this.targeThread.interrupt();
			}
		}

		// タイムアウト時刻設定
		LocalDateTime limit_datetime = LocalDateTime.now().plusSeconds(limit_seconds);

		Region reg = Screen.getPrimaryScreen();
		Region screen = Screen.getPrimaryScreen();
		boolean breaked = false;

		myLogger.info("BS5終了");
		// タイムアウトを待ちながらBS5の停止を試みる
		try {
			// タイムアウトを待ちながらBS5の停止を試みる
			while (limit_datetime.isAfter(LocalDateTime.now())) {
				// BS5を強制終了
				bs_force_exit(30);
				my.sleep(3000);
				if (BS5App.is_running(2)) {
					try {
						reg = BS5App.focus();
						my.println("BS5 has window.");
						my.sleep(800);
						// Location l = reg.getTopRight().offset(-50, 16);
						// my.single_tap_by_Location(l);
						// my.sleep(3000);

						bs_force_exit(30);

					} catch (Exception e) {
						my.println("BS5 doesn't have window.");
					}

					my.sleep(10);
					my.println("BS5停止");

					// タイマー設定
					QuitTimer task = new QuitTimer(Thread.currentThread());

					breaked = false;
					Timer timer = new Timer();
					try {
						timer.schedule((TimerTask) task, 20000);

						for (int i = 0; i < RETRY_EXISTS; i++) {
							if (!BS5App.is_running(2)) {
								my.println("BS5終了");
								breaked = true;
								break;
							}
							if (Objects.nonNull(screen.exists(BS5Patterns.bs5_confirm_exit_090, WAIT_TIMEOUT))) {
								my.single_tap_by_Location(screen.getLastMatch().getTarget());
							} else if (Objects.nonNull(screen.exists(BS5Patterns.bs5_exit_088, WAIT_TIMEOUT))) {
								my.single_tap_by_Location(screen.getLastMatch().getTarget());
							}
							my.println("BS5 is Running");
							Thread.sleep(1000);
							BS5App.instance = null;
						}

						// 抜けてきたということはフリーズはしていない⇒タイマーキャンセル
						timer.cancel();
					} catch (InterruptedException e) {
						myLogger.error("BS5フリーズ疑惑");
						mySS.take("BS5フリーズ疑惑");
						bs_force_exit(30);
					}
					// 念のためタイマー停止
					timer.cancel();

					if (!breaked) {
						my.println("BS5停止失敗");
						mySS.take("BS5停止失敗");
						// まっくろくろすけ対策
						try {
							my.println("BS5 has window.");
							// if (Objects.nonNull(reg.exists(BS5Patterns.control_box_094.targetOffset(-20,
							// 0),
							// my.C_WAIT_EXISTS))) {
							// Match m = reg.getLastMatch();
							// my.single_tap_by_Location(m.getTarget());
							// my.sleep(3000);
							// }
							bs_force_exit(30);

						} catch (Exception e) {
							my.println("BS5 doesn't have window.");
						}
						continue;
					}
					my.println("........Complete.");
					break;
				} else {
					my.println("BS5停止 Done.");
					break;
				}
			}
		} catch (Exception e) {
			// スタックトレース出力
			e.printStackTrace();
		}
		if (!breaked) {
			myLogger.info("タイムアウト：BS5停止");
		}
	}

	/**
	 * エミュ強制終了.
	 * 
	 * @param limit_seconds 制限時間(s)
	 */
	public static void bs_force_exit(long limit_seconds) {
		// タイムアウト時刻設定
		LocalDateTime limit_datetime = LocalDateTime.now().plusSeconds(limit_seconds);

		my.println("BS5強制終了");
		// タイムアウトを待ちながらBS5の停止を試みる
		while (limit_datetime.isAfter(LocalDateTime.now())) {
			try {
				final String[] command = {
					"tasklist",
					"/fi",
					"\"Imagename eq " + BS5App.APP_PROCESS_NAME + ".exe" + "\"",
				};
				List<String> out = my.systemcall(command, "MS932");

				if (out.stream().anyMatch(x -> x.contains(BS5App.APP_PROCESS_NAME)) == false) {
					// BS5プロセスが動作していなければ終了
					break;
				}
				my.sleep(3000);

				final String[] command1 = {
					"taskkill",
					"/im",
					BS5App.APP_PROCESS_NAME + ".exe",
					"/f",
				};
				List<String> out1 = my.systemcall(command1, "MS932");
				my.sleep(3000);
			} catch (Exception e) {
				// スタックトレース出力
				e.printStackTrace();
				// スクリーンショット
				mySS.take("BS5強制終了");
			}

		}
	}

	/**
	 * エミュの日時をn秒進める.
	 * <p>
	 * 負の値を指定することも可
	 * 
	 * @param n 秒数
	 */
	public static boolean set_date_add_n_seconds(long n) {
		try {
			LocalDateTime remote = ADBWrapper.get_datetime();
			if (Objects.isNull(remote)) {
				return false;
			}
			myLogger.fine(String.format("get date : %s", myDateTime.formatter.format(remote)));

			long ms = remote.getLong(ChronoField.MILLI_OF_SECOND);
			my.sleep((int) (1000 - ms));
			LocalDateTime des = remote.plusSeconds(2).truncatedTo(ChronoUnit.SECONDS);
			des = des.plusSeconds(n);
			ADBWrapper.set_date(des);
			myLogger.fine(String.format("set date : %s", myDateTime.formatter.format(des)));

			remote = ADBWrapper.get_datetime();
			if (Objects.nonNull(remote)) {
				myLogger.fine(String.format("get date : %s", myDateTime.formatter.format(remote)));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 設定値反映
	 * @param config 設定値
	 */
	public static void config(ConfigureModel config) {
		WAIT_TIMEOUT = config.WAIT_TIMEOUT;
		WAIT_TIMEOUT_FOR_RECOVERY = config.WAIT_TIMEOUT_FOR_RECOVERY;
		LAUNCH_INTERVAL_HOURS = config.LAUNCH_INTERVAL_HOURS;
		LAUNCH_INTERVAL_MINUTES = config.LAUNCH_INTERVAL_MINUTES;
		LAUNCH_INTERVAL_SECONDS = config.LAUNCH_INTERVAL_SECONDS;
		TAP_INTERVAL_MILLIS = config.TAP_INTERVAL_MILLIS;
		OFFSET_MINUTES_RESTART = config.OFFSET_MINUTES_RESTART;
		OFFSET_MINUTES_GILLS_CREATE = config.OFFSET_MINUTES_GILLS_CREATE;
		OFFSET_MINUTES_FOR_MONDAY = config.OFFSET_MINUTES_FOR_MONDAY;
		RETRY_PRE_PROCESS = config.RETRY_PRE_PROCESS;
		RETRY_POST_PROCESS = config.RETRY_POST_PROCESS;
		RETRY_CONNECT_ADB = config.RETRY_CONNECT_ADB;
		RETRY_SEND_HEARTS_ALL = config.RETRY_SEND_HEARTS_ALL;
		RETRY_EXISTS = config.RETRY_EXISTS;
		RETRY_EXISTS_NUMBER_ON_MICKEY = config.RETRY_EXISTS_NUMBER_ON_MICKEY;
		RETRY_EXISTS_WITH_COMMUNICATION = config.RETRY_EXISTS_WITH_COMMUNICATION;
		RETRY_EXISTS_UPPER_CLOSE = config.RETRY_EXISTS_UPPER_CLOSE;
		RETRY_EXISTS_OK = config.RETRY_EXISTS_OK;
		RETRY_EXISTS_HIGH_SCORE = config.RETRY_EXISTS_HIGH_SCORE;
		RETRY_DISAPPEAR_HIGH_SCORE = config.RETRY_DISAPPEAR_HIGH_SCORE;
		RETRY_TAP_HIGH_SCORE = config.RETRY_TAP_HIGH_SCORE;
		RETRY_TAP_UPPER_CLOSE = config.RETRY_TAP_UPPER_CLOSE;
		RETRY_TAP_HEART = config.RETRY_TAP_HEART;
		RETRY_TAP_HEART_PRESENT_OK = config.RETRY_TAP_HEART_PRESENT_OK;
		RETRY_SEEK = config.RETRY_SEEK;
		LIMIT_SECONDS_RESTART = config.LIMIT_SECONDS_RESTART;
		LIMIT_SECONDS_START = config.LIMIT_SECONDS_START;
		LIMIT_SECONDS_GIVE_A_HEART = config.LIMIT_SECONDS_GIVE_A_HEART;
		LIMIT_SECONDS_TAKE_ALL_HEART = config.LIMIT_SECONDS_TAKE_ALL_HEART;
		LIMIT_SECONDS_EXISTS_HEART = config.LIMIT_SECONDS_EXISTS_HEART;
		LIMIT_SECONDS_RECOVERY_TO_RANKING = config.LIMIT_SECONDS_RECOVERY_TO_RANKING;
		LIMIT_SECONDS_ROOT_EXIST = config.LIMIT_SECONDS_ROOT_EXIST;
		LIMIT_SECONDS_PLAY_ROBOTMON = config.LIMIT_SECONDS_PLAY_ROBOTMON;
		LIMIT_SECONDS_LOGIN_LINE = config.LIMIT_SECONDS_LOGIN_LINE;
		AFTER_SECONDS_POPS_UP_HEART = config.AFTER_SECONDS_POPS_UP_HEART;
		LIMIT_SECONDS_POPS_UP_HEART = config.LIMIT_SECONDS_POPS_UP_HEART;
		LIMIT_SECONDS_FREEZED = config.LIMIT_SECONDS_FREEZED;
		SYSTEM_REBOOT_SCHEDULE_HOUR = config.SYSTEM_REBOOT_SCHEDULE_HOUR;

		DATETIME_ALIVE = config.DATETIME_ALIVE;
		DATETIME_NEXT = config.DATETIME_NEXT;

		PATH_LONG_CLOSE_HIGHSCORE = config.PATH_LONG_CLOSE_HIGHSCORE;

		OFFSET_X_FROM_LEADING_EDGE_OF_RANK_LIST = config.OFFSET_X_FROM_LEADING_EDGE_OF_RANK_LIST;

		LINE_API_TOKEN_ALERT = config.LINE_API_TOKEN_ALERT;
		LINE_API_TOKEN_NOTIFICATION = config.LINE_API_TOKEN_NOTIFICATION;
	}

	// ***** 以下、未使用 *****

	/**
	 * ♥送信モレフォローアップ.
	 * <p>
	 * ニックネーム：多い日も安心
	 * 
	 * @param lowest_number_rank 最下位ランキングNo.
	 * @throws RetryOverException リトライオーバー
	 */
	public static void follow_up_missing_hearts_0(int lowest_number_rank) throws RetryOverException {
		List<Integer> number_rank_per_page = null;

		// キャッシュクリア
		number_rank_list_cache = null;
		seek_resume_position(lowest_number_rank);
		myLogger.fine("フォロー開始");
		while (0 < lowest_number_rank) {
			try {
				// ページ単位各人のランキング数認識
				number_rank_per_page = recognition_numbers_in_a_page();
				if (Objects.isNull(number_rank_per_page)) {
					mySS.take_screen("missed swipe");
					fit_seperator_line_to_bottom_border();
					seek_resume_position(lowest_number_rank);
					continue;
				}
				// ページ単位のハート送信を実施する
				if (Objects.nonNull(BS5Regions.hearts.exists(BS5Patterns.heart_full_093, WAIT_TIMEOUT))) {
					Spliterator<Match> spliterator = Spliterators
						.spliteratorUnknownSize(BS5Regions.hearts.findAll(BS5Patterns.heart_full_093), 0);
					Stream<Match> stream = StreamSupport.stream(spliterator, false);
					Comparator<Match> comparator = Comparator.comparing(Match::getY).reversed();
					List<Match> sorted_hearts = stream.sorted(comparator).collect(Collectors.toList());
					for (Match heart : sorted_hearts) {
						int offset_heart_y = heart.y - (BS5Regions.rank_list.y - 2);
						int num_index = (int) (offset_heart_y / C_HEIGHT_OF_ONE_BLOCK_INT);
						if (num_index < 0) {
							mySS.take("heart_pos_-1");
							num_index = 0;
						}
						if (3 < num_index) {
							mySS.take("heart_pos_+1");
							num_index = 3;
						}
						Integer number_rank = number_rank_per_page.get(num_index);
						myLogger.fine(String.format("heart(%1$d,%2$d), %3$d位", heart.x - BS5Regions.tsum.x,
							heart.y - BS5Regions.tsum.y, number_rank));
						// ♥を贈る
						BS5Status.update_send_datetime(number_rank, null, HeartSendState.HEART_TAP);
						LocalDateTime result = give_a_heart(heart, number_rank);
						if (Objects.nonNull(result)) {
							// 最初に♥を送信した日時を更新
							if (Objects.isNull(BS5Status.get_first_send_datetime())) {
								BS5Status.set_first_send_datetime(result);
								// ファイル日時の設定
								myDateTime.set_FileDateTime(BS5Status.get_first_send_datetime());
							}
							myLogger.info(String.format("♥送信モレをキャッチ、%1$d位", number_rank));
							// ♥を送信した日時を更新
							BS5Status.update_send_datetime(number_rank, result, HeartSendState.COLOURFUL_TSUM);
							// 最終送信日時を記憶
							BS5Status.set_last_send_datetime(result);
							// [おためし] start
							my.sleep(3000);
							// [おためし] end
						} else {
							// ランキング表示に戻れているかチェック
							recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);
							break;
						}
					}
				}
				// ランキングNo.1位まで来たらループを抜ける
				if (number_rank_per_page.get(0).equals(1)) {
					break;
				}
				// 4人分進めて、レジューム位置シーク
				lowest_number_rank -= 4;
				seek_resume_position(lowest_number_rank);
			} catch (Exception e) {
				// 例外発生時
				// スタックトレース出力
				e.printStackTrace();
				continue;
			}
		}
		myLogger.fine("フォロー終了");
	}

	/**
	 * 自身のランキングを表示.
	 */
	public static int to_my_ranking_0() {
		// 週間ランキングを表示
		recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);
		// 自身のランキングを表示
		if (Objects.isNull(BS5Regions.tsum.exists(BS5Patterns.my_play_history_088, WAIT_TIMEOUT))) {
			if (Objects.nonNull(BS5Regions.play.exists(BS5Patterns.play_090, WAIT_TIMEOUT))) {
				my.single_tap_by_Location(BS5Regions.play.getTarget());
				my.sleep(1400);
			}
			if (Objects.nonNull(BS5Regions.turn_back.exists(BS5Patterns.turn_back_088, WAIT_TIMEOUT))) {
				my.single_tap_by_Location(BS5Regions.turn_back.getTarget());
				my.sleep(1400);
			}
		}
		// 自身の位置取得
		Region reg_my_rank = null;
		int number_of_my_rank = -1;
		while (number_of_my_rank <= 0) {
			if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.my_play_history_088, WAIT_TIMEOUT))) {
				reg_my_rank = BS5Regions.tsum.getLastMatch();
			} else {
				// 失敗した
				recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);
				to_my_ranking();
				continue;
			}
			List<Integer> number_rank_list = recognition_numbers_in_a_page();
			if (Objects.isNull(number_rank_list)) {
				// 失敗した
				recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);
				to_my_ranking();
				continue;
			}
			int offset_my_rank_y = reg_my_rank.y - (BS5Regions.tsum.y + 266);
			int num_index = (int) (offset_my_rank_y / 90);
			if (num_index < 0) {
				mySS.take("my_rank_pos_-1");
				num_index = 0;
			}
			if (3 < num_index) {
				mySS.take("my_rank_pos_+1");
				num_index = 3;
			}
			number_of_my_rank = number_rank_list.get(num_index).intValue();
		}
		myLogger.info(String.format("自身(%1$d,%2$d), %3$d位", reg_my_rank.x - BS5Regions.tsum.x,
			reg_my_rank.y - BS5Regions.tsum.y, number_of_my_rank));
		return number_of_my_rank;
	}

	/**
	 * 数値化.
	 * 
	 * @param reg                      リージョン
	 * @param numbers_array_on_ranking 数字画像(2次元配列)
	 * @param interval                 間隔
	 * @return
	 */
	public static int recognition_number(Region reg, Pattern numbers_array[][], int interval) {
		long start_datetime = System.nanoTime();
		// それぞれの数字画像を検索し、
		// X座標とインデックスをペアにしてリストへ格納
		Map<Integer, Integer> numbers = new HashMap<Integer, Integer>();
		final int index[] = new int[] { 0 };
		for (Pattern[] patterns : numbers_array) {
			// long start_datetime_findBestList = System.nanoTime();
			Match match = reg.findBestList(Arrays.asList((Object[]) patterns));
			// long elapsed_time_findBestList = System.nanoTime() -
			// start_datetime_findBestList;
			// my.println(String.format("findBestList(%1$d) : elapsed time = %2$d (msec)",
			// index[0], elapsed_time_findBestList / 1000000));
			if (Objects.nonNull(match)) {
				// my.println("match=" + match.toString());
				// long start_datetime_findAll = System.nanoTime();
				Pattern pattern = patterns[match.getIndex()];
				try {
					reg.findAll(pattern).forEachRemaining(r -> {
						numbers.put(Integer.valueOf((r.getX() - reg.getX()) / interval), index[0]);
						// my.println("index=" + Integer.toString(index[0]));
					});
				} catch (FindFailed e) {
					e.printStackTrace();
					my.println("findAll failed. : index=" + index[0]);
				}
				// long elapsed_time_findAll = System.nanoTime() - start_datetime_findAll;
				// my.println(String.format("findAll(%1$d,%2$d) : elapsed time = %3$d (msec)",
				// index[0], match.getIndex(), elapsed_time_findAll / 1000000));
			}
			index[0]++;
		}
		// ヒットしない場合は -1
		if (numbers.isEmpty()) {
			return -1;
		}
		// my.println("numbers=" + numbers.toString());
		// X座標でソート
		numbers.entrySet().stream().sorted(java.util.Map.Entry.comparingByKey());
		// 順番にインデックスを取り出し数値化
		int result = numbers.values().stream().reduce(0, (accum, value) -> accum * 10 + value);
		// 処理時間計測
		long elapsed_time = System.nanoTime() - start_datetime;
		my.println(
			String.format("recognition number of rank one  : elapsed time = %1$d (msec)", elapsed_time / 1000000));
		// 数値を返す
		return result;
	}

	/**
	 * [with Stalker] メインループ.
	 */
	public void run_0() {
		// ローカル変数定義
		Set<Integer> hearts_unsent = new LinkedHashSet<Integer>();
		LocalDateTime next_datetime_with_tsum_play = LocalDateTime.MAX;
		LocalDateTime next_datetime = LocalDateTime.MAX;

		// ハイスコア非表示時間(ms)を復元
		Long valuLong = myIO.<Long> read(Paths.get(PATH_LONG_CLOSE_HIGHSCORE), Long.class);
		if (Objects.nonNull(valuLong)) {
			min_close_highscore_ms = valuLong.longValue();
			myLogger.info("ハイスコア非表示時間(ms)を復元");
		}

		// 次回実行日時を復元
		next_datetime = myDateTime.get_datetime(BS5Instance.get_datetime_next());
		if (Objects.isNull(next_datetime)) {
			// 復元に失敗したら、即実行
			next_datetime = LocalDateTime.now();
		} else {
			myLogger.info("次回実行日時を復元");
		}
		BS5Status.set_next_datetime(next_datetime);
		myLogger.info(String.format("next datetime = %1$s",
			myDateTime.formatter.format(BS5Status.get_next_datetime())));
		// ファイル日時の設定
		myDateTime.set_FileDateTime(BS5Status.get_next_datetime());
		// 最終送信日時を復元
		BS5Status.set_last_time_last_send_datetime(
			myDateTime.get_datetime(BS5Instance.get_datetime_last_time_last_send()));
		if (Objects.nonNull(BS5Status.get_last_time_last_send_datetime())) {
			myLogger.info("最終送信日時を復元");
			myLogger.info(String.format("last sent datetime = %1$s",
				myDateTime.formatter.format(BS5Status.get_last_time_last_send_datetime())));
		}
		// 全メンバー情報ストア日時を復元
		BS5Status.store_members_list_datetime = myDateTime.get_datetime(BS5Instance.get_datetime_store_members_list());
		if (Objects.nonNull(BS5Status.store_members_list_datetime)) {
			myLogger.info("全メンバー情報ストア日時を復元");
			myLogger.info(String.format("store members list datetime = %1$s",
				myDateTime.formatter.format(BS5Status.store_members_list_datetime)));
		}
		// 全メンバー情報を復元
		GillsActivity.load();
		myLogger.info("全メンバー情報を復元");
		// 生存通知
		myDateTime.set_datetime(LocalDateTime.now(), DATETIME_ALIVE);

		// // 余興を楽しませてもらおう
		// next_datetime_with_tsum_play =
		// BS5Status.get_next_datetime().plusMinutes(C_RESTART_REQUIRED_MINUTES);
		// if (next_datetime_with_tsum_play.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
		// next_datetime_with_tsum_play =
		// next_datetime_with_tsum_play.plusMinutes(C_OFFSET_MINUTES_OF_MON);
		// }
		// if (next_datetime_with_tsum_play.isAfter(LocalDateTime.now())) {
		// // プレミアムボックス・セレクトボックス・ピックアップガチャ
		// GachaActivity.roll_all_gacha();
		// }
		// while (next_datetime_with_tsum_play.isAfter(LocalDateTime.now())) {
		// for (int i = 0; i < 1; i++) {
		// my.mouse_move(new Location(1, 1));
		// my.sleep(1000);
		// }

		// myLogger.fine(String.format("play datetime = %1$s",
		// myDateTime.formatter.format(next_datetime_with_tsum_play)));
		// myLogger.fine(String.format("next datetime = %1$s",
		// myDateTime.formatter.format(BS5Status.get_next_datetime())));
		// if (Objects.nonNull(BS5Status.get_last_time_last_send_datetime())) {
		// myLogger.fine(String.format("last sent datetime = %1$s",
		// myDateTime.formatter.format(BS5Status.get_last_time_last_send_datetime())));
		// }
		// // 生存通知
		// myDateTime.set_datetime(LocalDateTime.now(), DATETIME_ALIVE);

		// if (next_datetime_with_tsum_play.isAfter(LocalDateTime.now())) {
		// // オートプレイ
		// AutoPlayActivity.play_tsum_tsum();
		// }
		// }
		// // 余興は終わりだ

		// // ハート送信前処理
		// myLogger.info_members("♣︎♣︎♣︎ START ♣︎♣︎♣︎");
		// pre_process();
		// hearts_unsent = BS5Status.get_hearts_unsent();
		// myLogger.flush();

		// // 開始待ち
		// {
		// boolean last_set_datetime = false;
		// while (BS5Status.get_next_datetime().isAfter(LocalDateTime.now())) {
		// my.sleep(1);
		// // 生存通知
		// if (LocalDateTime.now().getSecond() == 0) {
		// if (!last_set_datetime) {
		// myDateTime.set_datetime(LocalDateTime.now(), DATETIME_ALIVE);
		// last_set_datetime = true;
		// }
		// } else {
		// last_set_datetime = false;
		// }
		// }
		// }

		// メインループ
		while (true) {
			// 余興を楽しませてもらおう
			next_datetime_with_tsum_play = BS5Status.get_next_datetime().plusMinutes(OFFSET_MINUTES_RESTART);
			if (next_datetime_with_tsum_play.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
				next_datetime_with_tsum_play = next_datetime_with_tsum_play.plusMinutes(OFFSET_MINUTES_FOR_MONDAY);
			}
			if (next_datetime_with_tsum_play.isAfter(LocalDateTime.now())) {
				// プレミアムボックス・セレクトボックス・ピックアップガチャ
				GachaActivity.roll_all_gacha();
			}
			if (next_datetime_with_tsum_play.isAfter(LocalDateTime.now())) {
				// オートプレイ
				AutoPlayActivity.play_tsum_tsum();
			}
			while (next_datetime_with_tsum_play.isAfter(LocalDateTime.now())) {
				for (int i = 0; i < 10; i++) {
					my.mouse_move(new Location(1, 1));
					my.sleep(1000);
				}

				myLogger.fine(String.format("play datetime = %1$s",
					myDateTime.formatter.format(next_datetime_with_tsum_play)));
				myLogger.fine(String.format("next datetime = %1$s",
					myDateTime.formatter.format(BS5Status.get_next_datetime())));
				myLogger.fine(String.format("last sent datetime = %1$s",
					myDateTime.formatter.format(BS5Status.get_last_time_last_send_datetime())));
				// 生存通知
				myDateTime.set_datetime(LocalDateTime.now(), DATETIME_ALIVE);

				if (next_datetime_with_tsum_play.isAfter(LocalDateTime.now())) {
					// オートプレイ
					AutoPlayActivity.play_tsum_tsum();
				}
			}
			// 余興は終わりだ

			// ファイル日時の設定
			myDateTime.set_FileDateTime(BS5Status.get_next_datetime());

			// ハート送信前処理
			myLogger.info_members("♣︎♣︎♣︎ START ♣︎♣︎♣︎");
			pre_process();
			hearts_unsent = BS5Status.get_hearts_unsent();
			myLogger.flush();

			// 開始待ち
			{
				boolean last_set_datetime = false;
				while (BS5Status.get_next_datetime().isAfter(LocalDateTime.now())) {
					my.sleep(1);
					// 生存通知
					if (LocalDateTime.now().getSecond() == 0) {
						if (!last_set_datetime) {
							myDateTime.set_datetime(LocalDateTime.now(), DATETIME_ALIVE);
							last_set_datetime = true;
						}
					} else {
						last_set_datetime = false;
					}
				}
			}

			// エミュフォーカスセット
			BS5App.focus();
			// 生存通知
			myDateTime.set_datetime(LocalDateTime.now(), DATETIME_ALIVE);
			// 実行日時セット
			LocalDateTime started_datetime = LocalDateTime.now();
			// 仮の次回実行日時セット
			BS5Status.set_next_datetime(started_datetime
				.plusHours(LAUNCH_INTERVAL_HOURS)
				.plusMinutes(LAUNCH_INTERVAL_MINUTES)
				.plusSeconds(LAUNCH_INTERVAL_SECONDS));
			// 初期化
			BS5Status.set_last_time_first_send_datetime(BS5Status.get_first_send_datetime());
			BS5Status.set_first_send_datetime(null);
			count_hearts = 0;
			BS5Status.set_extended_delay(false);

			myLogger.info("♥♥♥ START ♥♥♥");
			myLogger.flush();
			// ハート送信ループ
			int retry = 0;
			LocalDateTime limit_datetime_for_all = LocalDateTime.now().plusSeconds(LIMIT_SECONDS_EXISTS_HEART);
			if (Objects.nonNull(BS5Status.get_last_time_last_send_datetime())) {
				limit_datetime_for_all = BS5Status.get_last_time_last_send_datetime()
					.plusSeconds(LIMIT_SECONDS_POPS_UP_HEART);
				if (limit_datetime_for_all.isBefore(LocalDateTime.now())) {
					// タイムアウト時刻が不正の場合、現在時刻から補正する
					LocalDateTime.now().plusSeconds(LIMIT_SECONDS_EXISTS_HEART);
				}
			}
			while (limit_datetime_for_all.isAfter(LocalDateTime.now())) {
				try {
					recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);

					// ハート送信開始
					give_all_hearts(hearts_unsent);
					// 最終送信日時を記憶
					if (Objects.nonNull(BS5Status.get_last_send_datetime())) {
						BS5Status.set_last_time_last_send_datetime(BS5Status.get_last_send_datetime());
					}
				} catch (RetryOverException e) {
					// リトライオーバー発生時
					// スタックトレース出力
					e.printStackTrace();
					mySS.take("RETRY_OVER_heart_send");
					// ツム再起動
					stop_and_create(LIMIT_SECONDS_RESTART);
					// ループを抜けず、以降リトライ
				} catch (Exception e) {
					// その他例外発生時
					// スタックトレース出力
					e.printStackTrace();
					mySS.take("EXCEPTION_heart_send");
					// ツム再起動
					stop_and_create(LIMIT_SECONDS_RESTART);
					// ループを抜けず、以降リトライ
				}
				if (hearts_unsent.size() == 0) {
					// ♥を送り終わったら終了
					break;
				}
				////////// 以降がループを抜けずリトライ時の処理
				// 自身の位置取得
				int new_number_of_my_rank = to_my_ranking();
				BS5Status.set_number_of_my_rank(new_number_of_my_rank);
				// レジューム位置シーク
				int number_of_next_rank = BS5Status.get_number_of_members();
				Iterator<Integer> iterator_integer = hearts_unsent.iterator();
				if (iterator_integer.hasNext()) {
					number_of_next_rank = iterator_integer.next().intValue();
				}
				myLogger.info(String.format("再開位置 %1$d位", number_of_next_rank));
				try {
					seek_resume_position(number_of_next_rank);
				} catch (RetryOverException e) {
					e.printStackTrace();
				}
				myLogger.fine(String.format("main リトライ:%1$d/%2$d, heartsUnsent = %3$s",
					retry++, RETRY_SEND_HEARTS_ALL, Arrays.toString(hearts_unsent.toArray())));
			}
			// 全メンバー情報を保存
			GillsActivity.store();
			// 全メンバー情報ストア日時を設定
			myDateTime.set_datetime(LocalDateTime.now(), BS5Instance.get_datetime_store_members_list());
			// first_send_datetime が未設定だったら started_datetime を使う
			if (Objects.isNull(BS5Status.get_first_send_datetime())) {
				BS5Status.set_first_send_datetime(started_datetime);
			}
			myLogger.info("♥♥♥ FINISH ♥♥♥");
			myLogger.flush();
			Duration elapsed_time = Duration.between(LocalDateTime.now(), BS5Status.get_first_send_datetime()).abs();
			myLogger.info(String.format("elapsed time = %02d:%02d.%03d",
				elapsed_time.getSeconds() / 60,
				elapsed_time.getSeconds() % 60,
				elapsed_time.toMillis() % 1000));
			// next_datetime を最初に♥送信した時刻を基に再設定
			myLogger.info(String.format("first sent datetime = %1$s",
				myDateTime.formatter.format(BS5Status.get_first_send_datetime())));
			BS5Status.set_next_datetime(BS5Status.get_first_send_datetime()
				.plusHours(LAUNCH_INTERVAL_HOURS)
				.plusMinutes(LAUNCH_INTERVAL_MINUTES)
				.plusSeconds(LAUNCH_INTERVAL_SECONDS));
			myDateTime.set_datetime(BS5Status.get_next_datetime(), BS5Instance.get_datetime_next());
			myDateTime.set_datetime(BS5Status.get_next_datetime(), DATETIME_NEXT);
			myLogger.info(String.format("next datetime = %1$s",
				myDateTime.formatter.format(BS5Status.get_next_datetime())));
			// 最終送信日時を設定
			myDateTime.set_datetime(BS5Status.get_last_time_last_send_datetime(),
				BS5Instance.get_datetime_last_time_last_send());
			myLogger.info(String.format("last sent datetime = %1$s",
				myDateTime.formatter.format(BS5Status.get_last_time_last_send_datetime())));
			// 多い日も安心
			try {
				recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);
				hearts_unsent = GillsActivity.get_hearts_send_queue();
				follow_up_missing_hearts(hearts_unsent);
				recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);
				// 最終送信日時を記憶
				if (Objects.nonNull(BS5Status.get_last_send_datetime())) {
					BS5Status.set_last_time_last_send_datetime(BS5Status.get_last_send_datetime());
				}
			} catch (Exception e) {
				// 例外発生時
				// スタックトレース出力
				e.printStackTrace();
				mySS.take("follow_up_E");
				// ツム再起動
				stop_and_create(LIMIT_SECONDS_RESTART);
			}
			// 全メンバー情報を保存
			GillsActivity.store();
			// 全メンバー情報ストア日時を設定
			myDateTime.set_datetime(LocalDateTime.now(), BS5Instance.get_datetime_store_members_list());
			myLogger.fine(String.format("next datetime = %1$s",
				myDateTime.formatter.format(BS5Status.get_next_datetime())));
			// 最終送信日時を再設定
			myDateTime.set_datetime(BS5Status.get_last_time_last_send_datetime(),
				BS5Instance.get_datetime_last_time_last_send());
			myLogger.info(String.format("last sent datetime = %1$s",
				myDateTime.formatter.format(BS5Status.get_last_time_last_send_datetime())));

			// デバッグ用
			myLogger.info_members("♣︎♣︎♣︎ FINISH ♣︎♣︎♣︎");
			myLogger.info_members(String.format("members_list"));
			GillsActivity.members_list.values().stream()
				.sorted(GillsActivity.comparator)
				.forEach(mm -> {
					myLogger.info_members(String.format("%1$d位, %2$s, 同分回数=%3$d, 同秒回数=%4$d",
						mm.rankNumber,
						myDateTime.formatter.format(mm.lastSendDatetime),
						mm.timesOfSameMin,
						mm.timesOfSameSec));
				});
			// 整理整頓
			GillsActivity.cleaning();

			myLogger.fine("全ての♥送信を終了しました");
			myLogger.flush();

			// システム再起動スケジュールチェック
			Integer hour = BS5Status.get_first_send_datetime().getHour();
			if (Arrays.asList(SYSTEM_REBOOT_SCHEDULE_HOUR).contains(hour)) {
				bs_exit(60);
				my.reboot_system();
			}

			// ハート送信後処理
			post_process();

			for (int i = 0; i < 20; i++) {
				my.sleep(1000);
			}

			// // 余興を楽しませてもらおう
			// next_datetime_with_tsum_play =
			// BS5Status.get_next_datetime().plusMinutes(C_RESTART_REQUIRED_MINUTES);
			// if (next_datetime_with_tsum_play.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
			// next_datetime_with_tsum_play =
			// next_datetime_with_tsum_play.plusMinutes(C_OFFSET_MINUTES_OF_MON);
			// }
			// if (next_datetime_with_tsum_play.isAfter(LocalDateTime.now())) {
			// // プレミアムボックス・セレクトボックス・ピックアップガチャ
			// GachaActivity.roll_all_gacha();
			// }
			// if (next_datetime_with_tsum_play.isAfter(LocalDateTime.now())) {
			// // オートプレイ
			// AutoPlayActivity.play_tsum_tsum();
			// }
			// while (next_datetime_with_tsum_play.isAfter(LocalDateTime.now())) {
			// for (int i = 0; i < 10; i++) {
			// my.mouse_move(new Location(1, 1));
			// my.sleep(1000);
			// }

			// myLogger.fine(String.format("play datetime = %1$s",
			// myDateTime.formatter.format(next_datetime_with_tsum_play)));
			// myLogger.fine(String.format("next datetime = %1$s",
			// myDateTime.formatter.format(BS5Status.get_next_datetime())));
			// myLogger.fine(String.format("last sent datetime = %1$s",
			// myDateTime.formatter.format(BS5Status.get_last_time_last_send_datetime())));
			// // 生存通知
			// myDateTime.set_datetime(LocalDateTime.now(), DATETIME_ALIVE);

			// if (next_datetime_with_tsum_play.isAfter(LocalDateTime.now())) {
			// // オートプレイ
			// AutoPlayActivity.play_tsum_tsum();
			// }
			// }
			// // 余興は終わりだ

			// // ファイル日時の設定
			// myDateTime.set_FileDateTime(BS5Status.get_next_datetime());

			// // ハート送信前処理
			// myLogger.info_members("♣︎♣︎♣︎ START ♣︎♣︎♣︎");
			// pre_process();
			// hearts_unsent = BS5Status.get_hearts_unsent();
			// myLogger.flush();

			// // 開始待ち
			// {
			// boolean last_set_datetime = false;
			// while (BS5Status.get_next_datetime().isAfter(LocalDateTime.now())) {
			// my.sleep(1);
			// // 生存通知
			// if (LocalDateTime.now().getSecond() == 0) {
			// if (!last_set_datetime) {
			// myDateTime.set_datetime(LocalDateTime.now(), DATETIME_ALIVE);
			// last_set_datetime = true;
			// }
			// } else {
			// last_set_datetime = false;
			// }
			// }
			// }
		}
	}
}
