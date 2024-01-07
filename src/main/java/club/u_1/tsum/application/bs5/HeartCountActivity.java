/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application.bs5;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.sikuli.script.FindFailed;
import org.sikuli.script.Location;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;
import org.sikuli.script.ScreenImage;

import club.u_1.tsum.application.ConfigureModel;
import club.u_1.tsum.application.my;
import club.u_1.tsum.application.mySS;

public class HeartCountActivity {

	// ----- 設定値定義 -----
	public static double WAIT_TIMEOUT = 0.001;
	public static String PATH_HC_UPDATE_SAVE = "";
	public static String PATH_HC_SAVE = "";

	// ----- 静的フィールド -----
	// Regions
	public static Region reg_list = null;
	public static Region reg_hearts = null;
	public static Region reg_name = null;
	public static Region reg_icon = null;

	// Patterns
	public static final Pattern hc_separator_line_085 = new Pattern("/bs5/hc_separator_line.png").similar(0.85f);
	public static final Pattern hc_confirmation_090 = new Pattern("/bs5/hc_confirmation.png").similar(0.90f);
	public static final Pattern hc_heart_full_090 = new Pattern("/bs5/hc_heart_full.png").similar(0.90f);

	/**
	 * 数字画像配列 メールボックス用.
	 * <P>
	 * インデックス＝数値
	 */
	static final public Pattern numbers_array_on_mb[] = {
		new Pattern("/bs5/hc_0.png").similar(0.82f),
		new Pattern("/bs5/hc_1.png").similar(0.80f),
		new Pattern("/bs5/hc_2.png").similar(0.82f),
		new Pattern("/bs5/hc_3.png").similar(0.83f),
		new Pattern("/bs5/hc_4.png").similar(0.82f),
		new Pattern("/bs5/hc_5.png").similar(0.82f),
		new Pattern("/bs5/hc_6.png").similar(0.82f),
		new Pattern("/bs5/hc_7.png").similar(0.82f),
		new Pattern("/bs5/hc_8.png").similar(0.82f),
		new Pattern("/bs5/hc_9.png").similar(0.82f),
		new Pattern("/bs5/hc_colon.png").similar(0.82f),
		new Pattern("/bs5/hc_slash.png").similar(0.82f),
	};

	/**
	 * ツムアプリRegionを元に各Regionセット.
	 * <P>
	 * NoxRegions.set_region()からコールされるように。
	 */
	public static void set_region() {
		if (Objects.nonNull(BS5Regions.tsum)) {
			reg_list = new Region(BS5Regions.tsum.x + 26, BS5Regions.tsum.y + 288, 482, 420);
			reg_hearts = new Region(reg_list.x + 105, reg_list.y + 0, 32, reg_list.h);
			reg_name = new Region(BS5Regions.tsum.x + 166, BS5Regions.tsum.y + 303, 208, 24);
			reg_icon = new Region(BS5Regions.tsum.x + 77, BS5Regions.tsum.y + 311, 52, 56);
		}
	}

	static Region get_reg_member(Match heart) {
		return new Region(reg_list.x + 40, heart.y - 52, 402, 83);
	}

	static Region get_reg_date(Region reg_member) {
		return new Region(reg_member.getBottomRight().x - 84, reg_member.getBottomRight().y - 16, 38, 16);
	}

	static Region get_reg_time(Region reg_member) {
		return new Region(reg_member.getBottomRight().x - 46, reg_member.getBottomRight().y - 16, 38, 16);
	}

	static void open_mailbox() {
		// メールボックスの表示を試みる
		for (int i = 0; i < BS5Activity.LIMIT_SECONDS_TAKE_ALL_HEART; i++) {
			if (Objects.nonNull(BS5Regions.retry.exists(BS5Patterns.retry_090, WAIT_TIMEOUT))) {
				my.println("リトライをタップ");
				my.single_tap_by_Location(BS5Regions.retry.getTarget());
				my.sleep(1000);
			}
			// カラフルツム表示
			else if (Objects.nonNull(BS5Regions.colorful.exists(BS5Patterns.colorful_tsum_088, WAIT_TIMEOUT))) {
				my.println("カラフルツム表示→タイトルをタップ");
				my.single_tap_by_Location(BS5Regions.mailbox.getTarget());
				my.sleep(1000);
			}
			// OK
			else if (Objects.nonNull(BS5Regions.ok_upper.exists(BS5Patterns.ok_090, WAIT_TIMEOUT))) {
				my.println("OKをタップ");
				my.single_tap_by_Location(BS5Regions.ok_upper.getTarget());
				my.sleep(1000);
			}
			// メールボックスアイコン
			else if (Objects.isNull(BS5Regions.mailbox.exists(BS5Patterns.title_mailbox_088, WAIT_TIMEOUT))) {
				my.println("メールボックスアイコンをタップ");
				my.single_tap_by_Location(BS5Regions.mail_icon.getTarget());
				my.sleep(800);
			} else {
				break;
			}

			if (Objects.isNull(BS5Regions.mailbox.exists(BS5Patterns.title_mailbox_088, WAIT_TIMEOUT))) {
				BS5Activity.recovery_to_ranking(60);
			}
		}
	}

	/**
	 * 区切り線を上端に合わせる.
	 */
	public static void fit_seperator_line_to_top_border() {
		try {
			// リスト区切りを検出
			Spliterator<Match> spliterator = Spliterators.spliteratorUnknownSize(
				reg_list.findAll(hc_separator_line_085), 0);
			Stream<Match> stream = StreamSupport.stream(spliterator, false);
			Comparator<Match> comparator = Comparator.comparing(Match::getY);
			List<Match> sorted_lines = stream.sorted(comparator).collect(Collectors.toList());
			// 位置決め
			Location baseLocation = new Location(reg_list.x + 88, reg_list.y);
			Location startLocation = new Location(baseLocation.x, sorted_lines.get(0).getTarget().y);
			Location middleLocation = baseLocation.offset(0, 0);
			Location endLocation = baseLocation.offset(40, 0);
			// スワイプ実施
			my.swipe_with_middle_by_Location(startLocation, middleLocation, endLocation);
		} catch (FindFailed e) {
			e.printStackTrace();
		}
	}

	/**
	 * タイムスタンプ 文字列化.
	 * @param reg リージョン
	 * @param numbers_array_on_clock 数字画像(1次元配列)
	 * @param interval 間隔
	 * @return 文字列
	 */
	public static String recognition_tsum_timestamp(Region reg, Pattern numbers_array[], int interval) {
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
						// my.println("x=" + Integer.toString(x));
						// my.println("index=" + Integer.toString(index[0]));
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
					c = "/";
				} else {
					c = m.toString();
				}
				// my.println(String.format("c=%1s", c));
				return c;
			})
			.collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
			.toString();
		// my.println(String.format("result=%1s", result));
		// 処理時間計測
		long elapsed_time = System.currentTimeMillis() - start_datetime;
		// my.println(String.format("recognition number of tsum clock  : elapsed time = %1$d (msec)", elapsed_time));
		// 文字列を返す
		return result;
	}

	/**
	 * スクリーンショット撮影.
	 * @param reg リージョン
	 * @return スクリーンショット
	 */
	public static ScreenImage take(Region reg) {
		Path filepath = Paths.get(PATH_HC_SAVE, String.format("%d.png", new Date().getTime()));
		return mySS.take(reg, filepath);
	}

	public static void config(ConfigureModel config) {
		WAIT_TIMEOUT = config.WAIT_TIMEOUT;
		PATH_HC_UPDATE_SAVE = config.PATH_HC_UPDATE_SAVE;
		PATH_HC_SAVE = config.PATH_HC_SAVE;
	}

	public static void test() {
		// fit_seperator_line_to_top_border();
		// my.sleep(500);
		// mySS.take("test");

		open_mailbox();

		// TextRecognizer.reset();
		// TextRecognizer.getInstance();
		try {
			// reg_hearts.highlight(1);
			while (Objects.nonNull(reg_hearts.exists(hc_heart_full_090, WAIT_TIMEOUT))) {
				Spliterator<Match> spliterator = Spliterators.spliteratorUnknownSize(reg_hearts.findAll(hc_heart_full_090), 0);
				Stream<Match> stream = StreamSupport.stream(spliterator, false);
				Comparator<Match> comparator = Comparator.comparing(Match::getY);
				List<Match> sorted_hearts = stream.sorted(comparator).collect(Collectors.toList());
				Match heart = sorted_hearts.get(0);
				// for (Match heart : sorted_hearts) {
				// 	int offset_heart_y = heart.y - (reg_list.y - 2);
				Region reg_member = get_reg_member(heart);
				// reg_member.highlight(1);
				Region reg_date = get_reg_date(reg_member);
				Region reg_time = get_reg_time(reg_member);
				reg_date.highlight(2);
				reg_time.highlight(2);
				String date = recognition_tsum_timestamp(reg_date, numbers_array_on_mb, 5);
				String time = recognition_tsum_timestamp(reg_time, numbers_array_on_mb, 5);
				my.println("◆◆◆" + date + " " + time);
				my.println("◆◆◆" + reg_date.text() + " " + reg_time.text());
				// }
				// fit_seperator_line_to_top_border();
				ScreenImage member_img = take(reg_member);
				if (Objects.nonNull(reg_member.exists(hc_confirmation_090, WAIT_TIMEOUT))) {
					my.println("確認をタップ");
					my.single_tap_by_Location(reg_member.getLastMatch().getTarget());
					my.sleep(800);
					break;
				}
				open_mailbox();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
