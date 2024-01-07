/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application.bs5;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.sikuli.script.FindFailed;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;
import org.sikuli.script.ScreenImage;

import club.u_1.tsum.application.ConfigureModel;
import club.u_1.tsum.application.my;
import club.u_1.tsum.application.myDateTime;
import club.u_1.tsum.application.myLogger;
import club.u_1.tsum.application.mySS;

public class TsumClock extends Thread {

	// ----- 設定値定義 -----
	public static double WAIT_TIMEOUT = 0.001;
	public static double WAIT_TIMEOUT_FOR_CLOCK = 0.000001;

	public static int MONITORING_INTERVAL_MILLIS = 10;
	public static long MONITORING_INTERVAL_NANOS = (long) MONITORING_INTERVAL_MILLIS * 1000000L;
	public static long LIMIT_SECONDS_GET_TSUM_CLOCK = 300;
	public static long THRESHOLD_NANOS_JUDGMENT_UPDATE = 3L * 1000000000L;
	public static long THRESHOLD_MILLIS_JUDGMENT_LEAD = 500;
	public static long THRESHOLD_MILLIS_JUDGMENT_ABS = 200;

	public static final String KEY_CLOCK = "clock";
	public static final String KEY_OFFSET = "offset";

	// ----- 静的フィールド -----
	public static final TsumClock instance = new TsumClock();

	static final public Pattern remain_084 = new Pattern("/bs5/remain.png").similar(0.84f);
	static final public Pattern closed_reception_090 = new Pattern("/bs5/closed_reception.png").similar(0.90f);

	/**
	 * 数字画像配列 ツム時計用.
	 * <P>
	 * インデックス＝数値
	 */
	static final public Pattern numbers_array_on_clock[] = {
		new Pattern("/bs5/re_0.png").similar(0.82f),
		new Pattern("/bs5/re_1.png").similar(0.80f),
		new Pattern("/bs5/re_2.png").similar(0.83f),
		new Pattern("/bs5/re_3.png").similar(0.83f),
		new Pattern("/bs5/re_4.png").similar(0.83f),
		new Pattern("/bs5/re_5.png").similar(0.83f),
		new Pattern("/bs5/re_6.png").similar(0.83f),
		new Pattern("/bs5/re_7.png").similar(0.83f),
		new Pattern("/bs5/re_8.png").similar(0.82f),
		new Pattern("/bs5/re_9.png").similar(0.83f),
		new Pattern("/bs5/re_colon.png").similar(0.83f),
		new Pattern("/bs5/re_day.png").similar(0.83f),
	};

	// ----- 変数定義 -----
	/** オフセットナノ秒. */
	private static long offset_nanos = 0;

	/**
	 * get オフセットナノ秒.
	 * @return オフセットナノ秒
	 */
	public static long get_offset_nanos() {
		return offset_nanos;
	}

	/**
	 * set オフセットナノ秒.
	 * @param offset_nanos オフセットナノ秒
	 */
	public static void set_offset_nanos(long offset_nanos) {
		TsumClock.offset_nanos = offset_nanos;
	}

	/**
	 * スレッドスタート.
	 */
	public static void thread_start() {
		instance.start();
	}

	/**
	 * メイン.
	 */
	@Override
	public void run() {
		while (!my.is_shutdown) {
			if (Objects.isNull(BS5Regions.tsum)) {
				// 準備できるまで待つ
				my.sleep(1000);
				continue;
			}
			try {
				// ランキング画面に戻れたかチェック
				if (Objects.nonNull(BS5Regions.title.exists(BS5Patterns.title_weekly_ranking_094, WAIT_TIMEOUT))) {
					// 左上の1ドットの色を取得し、0.5より上かを判定する
					Color color_TopLeft = BS5Regions.title.getLastMatch().getTopLeft().getColor();
					int[] rgb = new int[] {
						color_TopLeft.getRed(),
						color_TopLeft.getGreen(),
						color_TopLeft.getBlue(),
					};
					float[] hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], null);
					boolean is_bright = (hsb[2] > 0.5f);

					if (is_bright) {
						// ランキング画面に戻れたら
						// ツム時計取得
						Map<String, Object> result_tsum_clock = TsumClock.get_tsum_clock(LIMIT_SECONDS_GET_TSUM_CLOCK);
						myLogger.info(String.format("TsumClock,%1$s", myDateTime.formatter.format(TsumClock.now())));
						{
							long offset_nanos = TsumClock.get_offset_nanos();
							myLogger.info(String.format("offset, %1$d(ms)", offset_nanos / 1000000));
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			my.sleep(1000);
		}
	}

	/**
	 * 現在ツム時計取得.
	 * @return 現在ツム時計
	 */
	public static LocalDateTime now() {
		LocalDateTime tsum_clock = LocalDateTime.now();
		tsum_clock = tsum_clock.minus(TsumClock.offset_nanos, ChronoUnit.NANOS);
		return tsum_clock;
	}

	/**
	 * ツム時計からシステム時計取得.
	 * @return システム時計
	 */
	public static LocalDateTime system_date_time(LocalDateTime tsum_clock) {
		LocalDateTime date_time = tsum_clock.plus(TsumClock.offset_nanos, ChronoUnit.NANOS);
		return date_time;
	}

	/**
	 * システム時計からツム時計取得.
	 * @return ツム時計
	 */
	public static LocalDateTime tsum_date_time(LocalDateTime system_clock) {
		LocalDateTime date_time = system_clock.minus(TsumClock.offset_nanos, ChronoUnit.NANOS);
		return date_time;
	}

	/**
	 * ツム時計取得.
	 * @param limit_seconds
	 * @return
	 */
	public static Map<String, Object> get_tsum_clock(long limit_seconds) {
		HashMap<String, Object> result = new HashMap<>();

		if (Objects.isNull(BS5Regions.tsum)) {
			return result;
		}

		// タイムアウト時刻設定
		LocalDateTime limit_datetime = LocalDateTime.now().plusSeconds(limit_seconds);

		// Region絞り込み　
		Region reg = BS5Regions.tsum_clock;
		if (Objects.nonNull(reg.exists(remain_084, WAIT_TIMEOUT))) {
			Match m = reg.getLastMatch();
			reg = new Region(m.x + m.w - 2, m.y - 2, 72, 22);
			// reg.highlight(2);
		} else {
			my.println("絞り込み失敗");
		}

		// タイムアウトを待ちながらランキング画面への復帰を試みる
		boolean breaked = false;
		while (limit_datetime.isAfter(LocalDateTime.now())) {
			// スクリーンキャプチャ
			ScreenImage baseImage = mySS.take_for_tsum_clock(reg, "temp");
			Pattern basePattern = new Pattern(baseImage).exact();
			for (int i = 0; i < (60 * 1000) / MONITORING_INTERVAL_MILLIS; i++) {
				//  比較
				if (Objects.isNull(reg.exists(basePattern, WAIT_TIMEOUT))) {
					if (Objects.nonNull(reg.exists(closed_reception_090, WAIT_TIMEOUT))) {
						// 受付終了ならcontinue
						continue;
					}
					try {
						// 異なれば、分替わり
						// long offset_seconds = ChronoUnit.SECONDS.between(
						// 	LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES), LocalDateTime.now());
						// long offset_nanos = LocalDateTime.now().getLong(ChronoField.NANO_OF_SECOND);
						// long offset_ = (offset_seconds * 1000000000) + offset_nanos - INTERVAL_NANOS_MONITORING;
						// result.put(KEY_OFFSET, Long.valueOf(offset_));
						// my.println("offset=" + offset_);
						// LocalDateTime tsum_clock_ = LocalDateTime.now().plusNanos(offset_);
						// result.put(KEY_CLOCK, tsum_clock_);
						// my.println("tsum_clock=" + myDateTime.formatter.format(tsum_clock_));
						// my.println("now=" + myDateTime.formatter.format(LocalDateTime.now()));
						LocalDateTime tsum_clock = calculate_tsum_clock(reg);
						result.put(KEY_CLOCK, tsum_clock);
						my.println("tsum_clock=" + myDateTime.formatter.format(tsum_clock));
						my.println("now=" + myDateTime.formatter.format(LocalDateTime.now()));
						long offset = ChronoUnit.NANOS.between(tsum_clock, LocalDateTime.now());
						result.put(KEY_OFFSET, Long.valueOf(offset));
						my.println("offset=" + offset);
						// BS5Activity.to_home_and_return();
						breaked = true;
						break;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				my.sleep(MONITORING_INTERVAL_MILLIS);
			}
			if (breaked) {
				long offset = Math.abs(((Long) result.get(KEY_OFFSET)).longValue());
				if (offset < THRESHOLD_NANOS_JUDGMENT_UPDATE) {
					// オフセット更新
					TsumClock.offset_nanos = offset;
					break;
				}
			} else {
				// // ランキング更新
				// if (Objects.nonNull(BS5Regions.play.exists(BS5Patterns.play_090, WAIT_EXISTS))) {
				// 	// プレイ
				// 	my.single_tap_by_Location(BS5Regions.play.getTarget());
				// 	my.sleep(1400);
				// }
				// if (Objects.nonNull(BS5Regions.turn_back.exists(BS5Patterns.turn_back_088, WAIT_EXISTS))) {
				// 	// もどる
				// 	my.single_tap_by_Location(BS5Regions.turn_back.getTarget());
				// 	my.sleep(1400);
				// }
				// BS5Activity.to_home_and_return();
			}
		}

		return result;
	}

	/**
	 * ツム時計取得
	 * @param reg
	 * @return
	 */
	public static LocalDateTime calculate_tsum_clock(Region reg) {
		LocalDateTime result = null;

		// 残り日時を文字列化
		// String remain = "受付終了";
		String remain = recognition_tsum_clock(reg, numbers_array_on_clock, 5);

		// 残り日時の文字列を日と時刻に分割→ツム時計を求める
		String[] remains = remain.split("日|:");
		my.println(Arrays.toString(remains));
		if (remains.length >= 3) {
			int days = Integer.parseInt(remains[0]);
			int hours = Integer.parseInt(remains[1]);
			int minutes = Integer.parseInt(remains[2]);

			result = my.get_counting_date(LocalDateTime.now())
				.minusDays(days)
				.minusHours(hours)
				.minusMinutes(minutes + 1)
				.plusNanos(MONITORING_INTERVAL_NANOS);
		}

		return result;
	}

	/**
	 * ツム時計 文字列化.
	 * @param reg リージョン
	 * @param numbers_array_on_clock 数字画像(1次元配列)
	 * @param interval 間隔
	 * @return 文字列
	 */
	public static String recognition_tsum_clock(Region reg, Pattern numbers_array[], int interval) {
		long start_datetime = System.currentTimeMillis();
		// それぞれの数字画像を検索し、
		// X座標とインデックスをペアにしてリストへ格納
		Map<Integer, Integer> numbers = new HashMap<Integer, Integer>();
		final int index[] = new int[] { 0 };
		for (Pattern pattern : numbers_array) {
			if (Objects.nonNull(reg.exists(pattern, WAIT_TIMEOUT))) {
				long start_datetime_findAll = System.currentTimeMillis();
				try {
					reg.findAll(pattern).forEachRemaining(r -> {
						int x = (r.getX() - reg.getX());
						my.println("x=" + Integer.toString(x));
						my.println("index=" + Integer.toString(index[0]));
						numbers.put(Integer.valueOf(x / interval), index[0]);
					});
				} catch (FindFailed e) {
					// e.printStackTrace();
					// my.println("findAll failed. : index=" + index[0]);
				}
				long elapsed_time_findAll = System.currentTimeMillis() - start_datetime_findAll;
				// my.println(String.format("findAll(%1$d) : elapsed time = %2$d (msec)", index[0], elapsed_time_findAll));
			}
			index[0]++;
		}
		// ヒットしない場合は Empty
		if (numbers.isEmpty()) {
			return "";
		}
		// my.println("numbers=" + numbers.toString());

		// X座標でソート
		numbers.entrySet().stream().sorted(java.util.Map.Entry.comparingByKey());
		// 順番にインデックスを取り出し文字列化
		String result = numbers.values()
			.stream()
			.map(m -> {
				String c = "";
				if (m == 10) {
					c = ":";
				} else if (m == 11) {
					c = "日";
				} else {
					c = m.toString();
				}
				my.println(String.format("c=%1s", c));
				return c;
			})
			.collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
			.toString();
		my.println(String.format("result=%1s", result));
		// 処理時間計測
		long elapsed_time = System.currentTimeMillis() - start_datetime;
		// my.println(String.format("recognition number of tsum clock  : elapsed time = %1$d (msec)", elapsed_time));

		// 文字列を返す
		return result;
	}

	/**
	 * エミュの日時をn秒進める.
	 * <p>
	 * 負の値を指定することも可
	 * @param n ミリ秒数
	 */
	public static boolean set_date_add_n_milliseconds(long n) {
		try {
			LocalDateTime remote = ADBWrapper.get_datetime();
			if (Objects.isNull(remote)) {
				return false;
			}
			myLogger.fine(String.format("get date : %s", myDateTime.formatter.format(remote)));

			long ms = remote.getLong(ChronoField.MILLI_OF_SECOND);
			LocalDateTime des = remote.plusSeconds(2).truncatedTo(ChronoUnit.SECONDS);
			long n_corrected = n;
			// if (0 > n_corrected) {
			// 	n_corrected -= 200;
			// } else if (0 < n_corrected) {
			// 	n_corrected += 200;
			// }
			my.sleep((int) (1000 - ms + n_corrected));
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

	public static Long exist_deviation() {
		// Region絞り込み
		Region reg = BS5Regions.tsum_clock;
		if (Objects.nonNull(reg.exists(remain_084, WAIT_TIMEOUT))) {
			Match m = reg.getLastMatch();
			reg = new Region(m.x + m.w + 61, m.y + 2, 8, 15);
			// reg.highlight(2);
		}
		// 見つからない
		else {
			return null;
		}

		// スクリーンキャプチャ
		ScreenImage baseImage = mySS.take_for_tsum_clock(reg, "temp");
		Pattern basePattern = new Pattern(baseImage).exact();

		long millis = 0;

		// タイムアウト時刻設定
		LocalDateTime limit_datetime = LocalDateTime.now().plusSeconds(60);

		// タイムアウトを待ちながら変化を監視する
		while (limit_datetime.isAfter(LocalDateTime.now())) {
			//  比較
			if (Objects.isNull(reg.exists(basePattern, WAIT_TIMEOUT_FOR_CLOCK))) {
				// 異なれば、分替わり
				LocalDateTime nowDateTime = LocalDateTime.now();
				millis = nowDateTime.getLong(ChronoField.MILLI_OF_SECOND);
				String deviation = "";
				if (nowDateTime.getSecond() >= 1) {
					// 1以上→遅れ
					deviation = "遅れ";
					millis *= -1;
				} else {
					// 1未満→進み
					millis = 1000 - millis;
					deviation = "進み";
				}
				myLogger.info(String.format("TsumClock deviation : %1$s, %3$s : %2$d", myDateTime.formatter.format(nowDateTime), millis, deviation));
				break;
			}
		}

		// 生存通知
		myDateTime.set_datetime(LocalDateTime.now(), BS5Activity.DATETIME_ALIVE);

		return Long.valueOf(millis);
	}

	public static Long lottery_tsum_clock(long limit_seconds) {
		if (Objects.isNull(BS5Regions.tsum)) {
			return null;
		}

		// タイムアウト時刻設定
		LocalDateTime limit_datetime = LocalDateTime.now().plusSeconds(limit_seconds);

		return lottery_tsum_clock(limit_datetime);
	}

	/**
	 * ツムくじを引く.
	 * 
	 * @param limit_datetime リミット
	 * @return
	 */
	public static Long lottery_tsum_clock(LocalDateTime limit_datetime) {
		if (Objects.isNull(BS5Regions.tsum)) {
			return null;
		}

		long limit_seconds_remain;

		// オフセット初期化
		TsumClock.set_offset_nanos(0);

		// タイムアウトを待ちながらツムくじを引く
		boolean breaked = false;
		Long deviation = Long.valueOf(0);
		while (limit_datetime.isAfter(LocalDateTime.now())) {
			deviation = exist_deviation();
			if (Objects.nonNull(deviation)) {
				if (deviation.longValue() > THRESHOLD_MILLIS_JUDGMENT_LEAD) {
					// 進みならループを抜ける
					breaked = true;
				} else {
					limit_seconds_remain = ChronoUnit.SECONDS.between(LocalDateTime.now(), limit_datetime);
					if (limit_seconds_remain > BS5Activity.LIMIT_SECONDS_RESTART) {
						boolean need_restart = false;
						// long lead_ms = THRESHOLD_MILLIS_JUDGMENT_ABS;
						if (Math.abs(deviation.longValue()) < THRESHOLD_MILLIS_JUDGMENT_ABS) {
							// 絶対値で閾値に満たない
							// →BS再起動して調整
							need_restart = true;
						} else if (deviation.longValue() < 0) {
							// 遅れ
							if ((deviation.longValue() + 1000) <= THRESHOLD_MILLIS_JUDGMENT_ABS) {
								// 反転させて閾値に満たない
								// →BS再起動して調整
								need_restart = true;
							}
						}
						if (need_restart) {
							// BS再起動
							BS5Activity.bs_restart(BS5Activity.LIMIT_SECONDS_RESTART);
							// ツム再起動＆Regionセット
							Region reg_tsum = BS5Activity.stop_and_create(BS5Activity.LIMIT_SECONDS_START);
							BS5Regions.set_region(reg_tsum);
						} else {
							// ADB経由ホームキーイベント送信
							ADBWrapper.sendkey_home();
							my.sleep(1000);
							// 時計をnミリ秒進める
							set_date_add_n_milliseconds(150);
							// ミリ秒が650ms～800msになったらツム画面に戻す
							while (true) {
								LocalDateTime ldt = ADBWrapper.get_datetime();
								long ms = ldt.getLong(ChronoField.MILLI_OF_SECOND);
								if (650 < ms && ms < 800) {
									// ADB経由ツムツム起動
									ADBWrapper.start_TsumTsum();
									my.sleep(2000);
									break;
								}
								my.sleep(10);
							}
							my.println(String.format(
								"TsumClock limit datetime = %1$s",
								myDateTime.formatter.format(limit_datetime)));
						}
					} else {
						// タイムアウト確定
						break;
					}
				}
			}
			// 残り時間が表示されていなければ
			else {
				// ランキング表示に戻れているかチェック
				BS5Activity.recovery_to_ranking(BS5Activity.LIMIT_SECONDS_RECOVERY_TO_RANKING);
			}

			if (breaked) {
				break;
			}
		}
		// ※タイムアウトしてもオフセットは更新する
		long offset = deviation.longValue() * 1000000L;
		// オフセット更新
		set_offset_nanos(offset);

		return get_offset_nanos();
	}

	public static void config(ConfigureModel config) {
		WAIT_TIMEOUT = config.WAIT_TIMEOUT;
		WAIT_TIMEOUT_FOR_CLOCK = config.WAIT_TIMEOUT_FOR_CLOCK;

		MONITORING_INTERVAL_MILLIS = config.MONITORING_INTERVAL_MILLIS;
		MONITORING_INTERVAL_NANOS = config.MONITORING_INTERVAL_NANOS;
		LIMIT_SECONDS_GET_TSUM_CLOCK = config.LIMIT_SECONDS_GET_TSUM_CLOCK;
		THRESHOLD_NANOS_JUDGMENT_UPDATE = config.THRESHOLD_SECONDS_JUDGMENT_UPDATE * 1000000000L;
		THRESHOLD_MILLIS_JUDGMENT_LEAD = config.THRESHOLD_MILLIS_JUDGMENT_LEAD;
		THRESHOLD_MILLIS_JUDGMENT_ABS = config.THRESHOLD_MILLIS_JUDGMENT_ABS;
	}

	///// 以下、未使用メソッド /////

	public static final void deviation_test(long limit_seconds) {
		if (Objects.isNull(BS5Regions.tsum)) {
			return;
		}

		long limit_seconds_remain;

		// タイムアウト時刻設定
		LocalDateTime limit_datetime = LocalDateTime.now().plusSeconds(limit_seconds);

		// オフセット初期化
		TsumClock.set_offset_nanos(0);

		// タイムアウトを待ちながらツムくじを引く
		boolean breaked = false;
		while (limit_datetime.isAfter(LocalDateTime.now())) {
			Long deviation = exist_deviation();
			if (Objects.nonNull(deviation)) {
				if (deviation.longValue() > 0) {
					// 進みならループを抜ける
					breaked = true;
				} else {
					limit_seconds_remain = ChronoUnit.SECONDS.between(LocalDateTime.now(), limit_datetime);
					if (limit_seconds_remain > BS5Activity.LIMIT_SECONDS_START) {
						// ツムアプリ再起動→ツム時計同期
						BS5Activity.stop_and_create(BS5Activity.LIMIT_SECONDS_START);
					}
					limit_seconds_remain = ChronoUnit.SECONDS.between(LocalDateTime.now(), limit_datetime);
					if (limit_seconds_remain > BS5Activity.LIMIT_SECONDS_START) {
						// ランキング表示に戻れているかチェック
						BS5Activity.recovery_to_ranking(BS5Activity.LIMIT_SECONDS_RECOVERY_TO_RANKING);
					}
				}
			}
			if (breaked) {
				long offset = deviation.longValue() * 1000000L;
				if (offset < THRESHOLD_NANOS_JUDGMENT_UPDATE) {
					// オフセット更新
					TsumClock.set_offset_nanos(offset);
					break;
				}
			}
		}

		return;
	}
}
