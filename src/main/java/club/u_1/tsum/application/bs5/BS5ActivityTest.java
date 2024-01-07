/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application.bs5;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;

import club.u_1.tsum.application.RetryOverException;
import club.u_1.tsum.application.my;
import club.u_1.tsum.application.myDateTime;
import club.u_1.tsum.application.myLogger;
import club.u_1.tsum.application.mySS;

public class BS5ActivityTest extends BS5Activity {

	/**
	 * 実行中のメソッド名を取得します。
	 * @return メソッド名
	 */
	public static String getMethodName() {
		return Thread.currentThread().getStackTrace()[2].getMethodName();
	}

	/**
	 * 連続n人分スワイプ上下振りテスト.
	 * <P>
	 * 判断基準：元のランキングページに戻れたか
	 * @param times 回数
	 */
	public static void test_swipe_up_down_n(int times, double blocks) {
		// BS5Activity.recovery_to_ranking(120);
		// BS5Activity.fit_seperator_line_to_bottom_border();
		final List<Integer> number_rank_list = BS5Activity.recognition_numbers_in_a_page();
		if (Objects.isNull(number_rank_list)) {
			my.println(getMethodName());
			my.println("test fail.");
			return;
		}
		my.println(number_rank_list.toString());
		int max_number_rank = number_rank_list.get(3);
		long start_datetime = System.nanoTime();
		for (int n = 0; n < times; n++) {
			my.println(String.format("Number of trials : %1$d", n + 1));
			if (8 < max_number_rank) {
				double min_blocks = max_number_rank - blocks;
				if (min_blocks > 4) {
					BS5Activity.swipe_n(-blocks);
					BS5Activity.swipe_n(blocks);
				} else {
					min_blocks = max_number_rank - 4;
					BS5Activity.swipe_n(-min_blocks);
					BS5Activity.swipe_n(min_blocks);
				}
			} else {
				BS5Activity.swipe_n(blocks);
				BS5Activity.swipe_n(-blocks);
			}
			try {
				// キャッシュクリア
				BS5Activity.number_rank_list_cache = null;
				// ページ移動が発生していないかチェック
				BS5Activity.seek_resume_position_with_check_row4(max_number_rank, true);
			} catch (Exception e) {
				// ランキング表示に戻れているかチェック
				BS5Activity.recovery_to_ranking(BS5Activity.LIMIT_SECONDS_RECOVERY_TO_RANKING);
			}
		}
		long elapsed_time = (System.nanoTime() - start_datetime) / 1000000;

		my.println("test case name : " + getMethodName());
		my.println("elapsed time : " + elapsed_time + " msec");
		my.println("elapsed time per one time : " + (elapsed_time / times) + " msec");
	}

	/**
	 * 連続n人分スワイプ上テスト.
	 * <P>
	 * 判断基準：元のランキングページに戻れたか
	 * @param times 回数
	 */
	public static void test_swipe_up_n(int times, double blocks) {
		BS5Activity.recovery_to_ranking(120);
		BS5Activity.fit_seperator_line_to_top_border();
		final List<Integer> number_rank_list = BS5Activity.recognition_numbers_in_a_page();
		if (Objects.isNull(number_rank_list)) {
			my.println(getMethodName());
			my.println("test fail.");
			return;
		}
		my.println(number_rank_list.toString());
		long start_datetime = System.currentTimeMillis();
		for (int n = 0; n < times; n++) {
			my.println(String.format("Number of trials : %1$d", n + 1));
			BS5Activity.swipe_n(blocks);
		}
		long elapsed_time = System.currentTimeMillis() - start_datetime;

		my.println("test case name : " + getMethodName());
		my.println("elapsed time : " + elapsed_time + " msec");
		my.println("elapsed time per one time : " + (elapsed_time / times) + " msec");
	}

	/**
	 * 連続n人分スワイプ下テスト.
	 * <P>
	 * 判断基準：元のランキングページに戻れたか
	 * @param times 回数
	 */
	public static void test_swipe_down_n(int times, double blocks) {
		BS5Activity.recovery_to_ranking(120);
		BS5Activity.fit_seperator_line_to_top_border();
		final List<Integer> number_rank_list = BS5Activity.recognition_numbers_in_a_page();
		if (Objects.isNull(number_rank_list)) {
			my.println(getMethodName());
			my.println("test fail.");
			return;
		}
		my.println(number_rank_list.toString());
		long start_datetime = System.currentTimeMillis();
		for (int n = 0; n < times; n++) {
			my.println(String.format("Number of trials : %1$d", n + 1));
			BS5Activity.swipe_n(-blocks);
		}
		long elapsed_time = System.currentTimeMillis() - start_datetime;

		my.println("test case name : " + getMethodName());
		my.println("elapsed time : " + elapsed_time + " msec");
		my.println("elapsed time per one time : " + (elapsed_time / times) + " msec");
	}

	/**
	 * 連続n人分スワイプ下テスト.
	 * <P>
	 * 判断基準：なし(目視で確認)
	 * @param times 回数
	 */
	public static void test_swipe_down_n_from_bottom(int times, double blocks) {
		BS5Activity.recovery_to_ranking(120);
		BS5Activity.swipe_down_to_bottom();
		BS5Activity.fit_seperator_line_to_bottom_border();
		final List<Integer> number_rank_list = BS5Activity.recognition_numbers_in_a_page();
		if (Objects.isNull(number_rank_list)) {
			my.println(getMethodName());
			my.println("test fail.");
			return;
		}
		my.println(number_rank_list.toString());
		long start_datetime = System.currentTimeMillis();
		for (int n = 0; n < times; n++) {
			my.println(String.format("Number of trials : %1$d", n + 1));
			BS5Activity.swipe_n(-blocks);
		}
		long elapsed_time = System.currentTimeMillis() - start_datetime;

		my.println("test case name : " + getMethodName());
		my.println("elapsed time : " + elapsed_time + " msec");
		my.println("elapsed time per one time : " + (elapsed_time / times) + " msec");
	}

	/**
	 * 連続区切り線を下端に合わせるテスト.
	 * <P>
	 * 判断基準：ランキング画面に戻れたか
	 * @param times 回数
	 */
	public static void test_fit_seperator_line_to_bottom_border(int times) {
		final int fails[] = new int[] { 0 };
		BS5Activity.recovery_to_ranking(120);
		BS5Activity.swipe_down_to_bottom();
		final List<Integer> number_rank_list = BS5Activity.recognition_numbers_in_a_page();
		if (Objects.isNull(number_rank_list)) {
			my.println(getMethodName());
			my.println("test fail.");
			return;
		}
		long start_datetime = System.currentTimeMillis();
		final Integer[] number_rank_bottom = new Integer[] { Integer.valueOf(number_rank_list.get(2)) };
		for (int n = 0; n < times; n++) {
			my.println(String.format("Number of trials : %1$d", n + 1));
			BS5Activity.fit_seperator_line_to_bottom_border();
			// ランキングNoを1つ進めたかチェック
			List<Integer> temporay = BS5Activity.recognition_numbers_in_a_page();
			boolean breaked = false;
			if (Objects.nonNull(temporay)) {
				if (temporay.get(3).equals(number_rank_bottom[0])) {
					number_rank_bottom[0] = temporay.get(2);
					breaked = true;
				}
			}
			if (!breaked) {
				fails[0]++;
				my.println("fails : " + fails[0]);
				my.sleep(500);
				BS5Activity.recovery_to_ranking(30);
				// チェック用ランキングNoセットしなおす
				if (Objects.nonNull(temporay)) {
					number_rank_bottom[0] = temporay.get(2);
				}
			}
		}
		long elapsed_time = System.currentTimeMillis() - start_datetime;

		my.println("test case name : " + getMethodName());
		my.println("total fails : " + fails[0]);
		my.println("elapsed time : " + elapsed_time + " msec");
		my.println("elapsed time per one time : " + (elapsed_time / times) + " msec");
	}

	public static void test_findAll_separator_line() {
		long start_datetime = System.currentTimeMillis();

		try {
			// リスト区切りを検出
			Spliterator<Match> spliterator = Spliterators.spliteratorUnknownSize(
				BS5Regions.rank_list.findAll(BS5Patterns.separator_line_087.targetOffset(-31, 1)), 0);
			Stream<Match> stream = StreamSupport.stream(spliterator, false);
			Comparator<Match> comparator = Comparator.comparing(Match::getY);
			List<Match> sorted_lines = stream.sorted(comparator).collect(Collectors.toList());
			sorted_lines.stream().forEach(r -> {
				r.highlight(1);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		long elapsed_time = System.currentTimeMillis() - start_datetime;

		my.println("test case name : " + getMethodName());
		my.println("elapsed time : " + elapsed_time + " msec");
	}

	public static void test_findAll_pattern(Region reg, String imgpath, float sim) {
		long start_datetime = System.currentTimeMillis();

		try {
			// リスト区切りを検出
			Spliterator<Match> spliterator = Spliterators.spliteratorUnknownSize(
				reg.findAll(new Pattern(imgpath).similar(sim)), 0);
			Stream<Match> stream = StreamSupport.stream(spliterator, false);
			Comparator<Match> comparator = Comparator.comparing(Match::getY);
			List<Match> sorted_patterns = stream.sorted(comparator).collect(Collectors.toList());
			sorted_patterns.stream().forEach(r -> {
				r.highlight(1);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		long elapsed_time = System.currentTimeMillis() - start_datetime;

		my.println("test case name : " + getMethodName());
		my.println("elapsed time : " + elapsed_time + " msec");
	}

	/**
	 * 連続ページ内の各人のランキング数認識テスト.
	 * <P>
	 * 判断基準：元のランキングページに戻れたか
	 * @param times 回数
	 */
	public static void test_recognition_numbers_in_a_page(int times) {
		final int fails[] = new int[] { 0 };
		BS5Activity.recovery_to_ranking(120);
		// 自身のランキングを表示
		BS5Activity.to_my_ranking();
		final List<Integer> temporary = BS5Activity.recognition_numbers_in_a_page();
		if (Objects.isNull(temporary)) {
			my.println(getMethodName());
			my.println("test fail.");
			return;
		}
		final List<Long> elapsed_time_list = new ArrayList<Long>();
		for (int n = 0; n < times; n++) {
			my.println(String.format("Number of trials : %1$d", n + 1));
			long start_datetime = System.currentTimeMillis();
			List<Integer> number_rank_list = BS5Activity.recognition_numbers_in_a_page();
			long elapsed_time = System.currentTimeMillis() - start_datetime;
			elapsed_time_list.add(Long.valueOf(elapsed_time));
			if (Objects.nonNull(number_rank_list)) {
				my.println(Arrays.toString(number_rank_list.toArray()));
				// 自身の位置取得
				Region reg_my_rank = null;
				int number_of_my_rank = -1;
				if (Objects.nonNull(BS5Regions.tsum.exists(BS5Patterns.my_play_history_088, 0.001))) {
					reg_my_rank = BS5Regions.tsum.getLastMatch();
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
					my.println(String.format("自身(%1$d,%2$d), %3$d位", reg_my_rank.x - BS5Regions.tsum.x,
						reg_my_rank.y - BS5Regions.tsum.y, number_of_my_rank));
				}
			} else {
				fails[0]++;
				my.println("fails : " + fails[0]);
			}
			if ((n % 2) == 0) {
				BS5Activity.swipe_n(-4.0d);
			} else {
				BS5Activity.swipe_n(4.0d);
			}
		}

		long total_sum = elapsed_time_list.stream().mapToLong(x -> x).sum();
		OptionalDouble average = elapsed_time_list.stream().mapToLong(x -> x).average();
		my.println("test case name : " + getMethodName());
		my.println("total fails : " + fails[0]);
		my.println("elapsed time : " + total_sum + " msec");
		my.println("elapsed time per one time : " + average + " msec");
	}

	/**
	 * 連続ページ内の各人のランキング数認識テスト.
	 * <P>
	 * 判断基準：元のランキングページに戻れたか
	 */
	public static void test_recognition_numbers_in_a_page_bottom_to_up01() {
		final int fails[] = new int[] { 0 };
		BS5Activity.recovery_to_ranking(120);
		BS5Activity.swipe_down_to_bottom();
		BS5Activity.fit_seperator_line_to_bottom_border();
		BS5Activity.fit_seperator_line_to_bottom_border();
		final List<Integer> number_rank_list = BS5Activity.recognition_numbers_in_a_page();
		if (Objects.isNull(number_rank_list)) {
			my.println(getMethodName());
			my.println("test fail.");
			return;
		}
		int times = number_rank_list.get(3).intValue();
		my.println(String.format("ranking bottom No.%1$d", times));
		long start_datetime = System.currentTimeMillis();
		final Integer[] number_rank_bottom = new Integer[] { Integer.valueOf(number_rank_list.get(2)) };
		for (int n = 0; n < times; n++) {
			my.println(String.format("Number of trials : %1$d", n + 1));
			BS5Activity.fit_seperator_line_to_bottom_border();
			// ランキングNoを1つ進めたかチェック
			List<Integer> temporay = BS5Activity.recognition_numbers_in_a_page();
			boolean breaked = false;
			if (Objects.nonNull(temporay)) {
				my.println(String.format("Page top No.=%1$d", temporay.get(0)));
				if ((temporay.get(0).equals(1))) {
					break;
				} else if (temporay.get(3).equals(number_rank_bottom[0])) {
					number_rank_bottom[0] = temporay.get(2);
					breaked = true;
				}
			}
			if (!breaked) {
				fails[0]++;
				my.println("fails : " + fails[0]);
				my.sleep(1000);
				mySS.take("no recognition numbers");
				BS5Activity.recovery_to_ranking(30);
				// チェック用ランキングNoセットしなおす
				BS5Activity.fit_seperator_line_to_top_border();
				temporay = BS5Activity.recognition_numbers_in_a_page();
				if (Objects.nonNull(temporay)) {
					number_rank_bottom[0] = temporay.get(2);
					n--;
				}
			}
		}
		long elapsed_time = System.currentTimeMillis() - start_datetime;

		my.println("test case name : " + getMethodName());
		my.println("total fails : " + fails[0]);
		my.println("elapsed time : " + elapsed_time + " msec");
	}

	/**
	 * 連続ページ内の各人のランキング数認識テスト.
	 * <P>
	 * 判断基準：ランキング数が1人分進めたか
	 */
	public static void test_recognition_numbers_in_a_page_bottom_to_up02() {
		final int fails[] = new int[] { 0 };
		BS5Activity.recovery_to_ranking(120);
		BS5Activity.swipe_down_to_bottom();
		BS5Activity.fit_seperator_line_to_bottom_border();
		BS5Activity.fit_seperator_line_to_bottom_border();
		final List<Integer> number_rank_list = BS5Activity.recognition_numbers_in_a_page();
		if (Objects.isNull(number_rank_list)) {
			my.println(getMethodName());
			my.println("test fail.");
			return;
		}
		int times = number_rank_list.get(3).intValue();
		my.println(String.format("ranking bottom No.%1$d", times));
		mySS.take("bottom");
		long start_datetime = System.currentTimeMillis();
		final Integer[] number_rank_bottom = new Integer[] { Integer.valueOf(number_rank_list.get(2)) };
		for (int n = 0; n <= times; n++) {
			my.println(String.format("Number of trials : %1$d", n + 1));
			BS5Activity.swipe_n(-1.0d);
			// ランキングNoを1つ進めたかチェック
			List<Integer> temporay = BS5Activity.recognition_numbers_in_a_page();
			boolean breaked = false;
			if (Objects.nonNull(temporay)) {
				my.println(String.format("Page top No.=%1$d", temporay.get(0)));
				if ((temporay.get(0).equals(1))) {
					break;
				} else if (temporay.get(3).equals(number_rank_bottom[0])) {
					number_rank_bottom[0] = temporay.get(2);
					breaked = true;
				}
			}
			if (!breaked) {
				fails[0]++;
				my.println("fails : " + fails[0]);
				my.sleep(1000);
				mySS.take("no recognition numbers");
				BS5Activity.recovery_to_ranking(30);
				// チェック用ランキングNoセットしなおす
				BS5Activity.fit_seperator_line_to_top_border();
				temporay = BS5Activity.recognition_numbers_in_a_page();
				if (Objects.nonNull(temporay)) {
					number_rank_bottom[0] = temporay.get(2);
				}
			}
		}
		long elapsed_time = System.currentTimeMillis() - start_datetime;

		my.println("test case name : " + getMethodName());
		my.println("total fails : " + fails[0]);
		my.println("elapsed time : " + elapsed_time + " msec");
	}

	/**
	 * ♡を受け取るテスト.
	 * <P>
	 * 判断基準：ランキング画面に戻れたか
	 * @param times 回数
	 */
	public static void test_take_all_hearts(int times) {
		final int fails[] = new int[] { 0 };
		BS5Activity.recovery_to_ranking(120);
		long start_datetime = System.currentTimeMillis();
		for (int n = 0; n <= times; n++) {
			my.println(String.format("Number of trials : %1$d", n + 1));
			BS5Activity.take_all_hearts(30);
			// ランキング画面に戻れたかチェック
			if (Objects.isNull(BS5Regions.title.exists(BS5Patterns.title_weekly_ranking_094, 0.001d))) {
				fails[0]++;
				my.println("fails : " + fails[0]);
			}
		}
		long elapsed_time = System.currentTimeMillis() - start_datetime;

		my.println("test case name : " + getMethodName());
		my.println("total fails : " + fails[0]);
		my.println("elapsed time : " + elapsed_time + " msec");
		my.println("elapsed time per one time : " + (elapsed_time / times) + " msec");
	}

	/**
	 * ♡を送るテスト.
	 * <P>
	 * 判断基準：ランキング画面に戻れたか
	 * @param times 回数
	 */
	public static void test_give_a_heart() {
		final int fails[] = new int[] { 0 };
		BS5Activity.recovery_to_ranking(120);
		if (Objects.isNull(BS5Regions.hearts.exists(BS5Patterns.heart_full_093, WAIT_TIMEOUT))) {
			//			// 時計をn秒進める
			//			BS5Activity.set_date_add_n_seconds(3600);
			my.sleep(800);
			BS5Activity.swipe_n(5.0d);
			BS5Activity.swipe_n(-5.0d);
		}
		long start_datetime = System.currentTimeMillis();
		try {
			if (Objects.nonNull(BS5Regions.hearts.exists(BS5Patterns.heart_full_093, WAIT_TIMEOUT))) {
				Spliterator<Match> spliterator = Spliterators.spliteratorUnknownSize(BS5Regions.hearts.findAll(BS5Patterns.heart_full_093), 0);
				Stream<Match> stream = StreamSupport.stream(spliterator, false);
				Comparator<Match> comparator = Comparator.comparing(Match::getY).reversed();
				List<Match> sorted_hearts = stream.sorted(comparator).collect(Collectors.toList());
				for (Match heart : sorted_hearts) {
					int offset_heart_y = heart.y - (BS5Regions.rank_list.y - 2);
					int num_index = (int) (offset_heart_y / BS5Activity.C_HEIGHT_OF_ONE_BLOCK_INT);
					if (num_index < 0) {
						mySS.take("heart_pos_-1");
						num_index = 0;
					}
					if (3 < num_index) {
						mySS.take("heart_pos_+1");
						num_index = 3;
					}
					// ♥を贈る
					LocalDateTime result = BS5Activity.give_a_heart(heart, Integer.valueOf(0));
				}
				// ランキング画面に戻れたかチェック
				if (Objects.isNull(BS5Regions.title.exists(BS5Patterns.title_weekly_ranking_094, 0.001d))) {
					fails[0]++;
					my.println("fails : " + fails[0]);
				}
			}
		} catch (Exception e) {
			// NOP
		}
		long elapsed_time = System.currentTimeMillis() - start_datetime;

		my.println("test case name : " + getMethodName());
		my.println("total fails : " + fails[0]);
		my.println("elapsed time : " + elapsed_time + " msec");
	}

	/**
	 * 連続ハイスコア表示テスト.
	 * <P>
	 * 判断基準：ランキング画面に戻れたか
	 * @param times 回数
	 */
	public static void test_show_high_score(int times) {
		final int fails[] = new int[] { 0 };
		BS5Activity.recovery_to_ranking(120);
		BS5Activity.to_my_ranking();
		// BS5Activity.swipe_n(2.0d);
		long start_datetime = System.currentTimeMillis();
		for (int n = 0; n < times; n++) {
			my.println(String.format("Number of trials : %1$d", n + 1));
			try {
				BS5Activity.show_and_hide_high_score(1);
				// ランキング画面に戻れたかチェック
				if (Objects.isNull(BS5Regions.title.exists(BS5Patterns.title_weekly_ranking_094, 0.001d))) {
					fails[0]++;
					my.println("fails : " + fails[0]);
				}
				// BS5Activity.show_and_hide_high_score(2);
				// // ランキング画面に戻れたかチェック
				// if (Objects.nonNull(BS5Regions.title.exists(BS5Patterns.title_weekly_ranking_094, 1))) {
				// 	// 無処理
				// 	my.println("ランキング画面");
				// } else {
				// 	fails[0]++;
				// 	my.println("fails : " + fails[0]);
				// }
				BS5Activity.show_and_hide_high_score(3);
				// ランキング画面に戻れたかチェック
				if (Objects.isNull(BS5Regions.title.exists(BS5Patterns.title_weekly_ranking_094, 0.001d))) {
					fails[0]++;
					my.println("fails : " + fails[0]);
				}
				BS5Activity.show_and_hide_high_score(0);
				// ランキング画面に戻れたかチェック
				if (Objects.isNull(BS5Regions.title.exists(BS5Patterns.title_weekly_ranking_094, 0.001d))) {
					fails[0]++;
					my.println("fails : " + fails[0]);
				}
			} catch (Exception e) {
				e.printStackTrace();
				fails[0]++;
				my.println("fails : " + fails[0]);
			}
			BS5Activity.recovery_to_ranking(120);
		}
		long elapsed_time = System.currentTimeMillis() - start_datetime;

		my.println("test case name : " + getMethodName());
		my.println("total fails : " + fails[0]);
		my.println("elapsed time : " + elapsed_time + " msec");
		my.println("elapsed time per one time : " + (elapsed_time / times) + " msec");
	}

	/**
	 * ♥送信モレフォローアップテスト.
	 * <P>
	 * 判断基準：ランキング画面に戻れたか
	 * @param times 回数
	 */
	public static void test_follow_up_missing_hearts(int times) {
		final int fails[] = new int[] { 0 };
		BS5Activity.recovery_to_ranking(120);
		BS5Activity.swipe_down_to_bottom();
		final List<Integer> temporary = BS5Activity.recognition_numbers_in_a_page();
		if (Objects.isNull(temporary)) {
			my.println(getMethodName());
			my.println("test fail.");
			return;
		}
		int lowest_number_rank = temporary.get(3);
		long start_datetime = System.currentTimeMillis();
		for (int n = 0; n < times; n++) {
			my.println(String.format("Number of trials : %1$d", n + 1));
			try {
				BS5Activity.follow_up_missing_hearts_0(lowest_number_rank);
			} catch (RetryOverException e) {
				fails[0]++;
				my.println("fails : " + fails[0]);
			}
			BS5Activity.swipe_down_to_bottom();
			// ランキング画面に戻れたかチェック
			if (Objects.isNull(BS5Regions.title.exists(BS5Patterns.title_weekly_ranking_094, 0.001d))) {
				fails[0]++;
				my.println("fails : " + fails[0]);
			}
		}
		long elapsed_time = System.currentTimeMillis() - start_datetime;

		my.println("test case name : " + getMethodName());
		my.println("total fails : " + fails[0]);
		my.println("elapsed time : " + elapsed_time + " msec");
		my.println("elapsed time per one time : " + (elapsed_time / times) + " msec");
	}

	/**
	 * ♥送信モレフォローアップテスト.
	 * <P>
	 * 判断基準：ランキング画面に戻れたか
	 * @param times 回数
	 */
	public static void test_follow_up_missing_hearts_0(int times) {
		final int fails[] = new int[] { 0 };
		BS5Activity.recovery_to_ranking(120);
		// 全メンバー情報を復元
		GillsActivity.load();
		// 自身の位置取得
		int number_of_my_rank = BS5Activity.to_my_ranking();
		// ランキング最下位へスワイプ
		BS5Activity.swipe_down_to_bottom();
		// ランキングメンバー数取得
		int number_of_members = -1;
		while (number_of_members <= 0) {
			List<Integer> number_rank_list = BS5Activity.recognition_numbers_in_a_page();
			if (Objects.isNull(number_rank_list)) {
				// 失敗した
				BS5Activity.recovery_to_ranking(BS5Activity.LIMIT_SECONDS_RECOVERY_TO_RANKING);
				BS5Activity.swipe_down_to_bottom();
				continue;
			}
			number_of_members = number_rank_list.get(2).intValue();
		}
		BS5Status.set_number_of_members(number_of_members);
		myLogger.info_members(String.format("メンバー数, %1$d人", number_of_members));
		// ハート未送信集合
		Set<Integer> hearts_unsent = GillsActivity.get_hearts_send_queue();
		// デバッグ用
		GillsActivity.members_list.values().stream()
			.sorted(GillsActivity.comparator)
			.forEach(mm -> {
				myLogger.info_members(String.format("%1$d位, %2$s",
					mm.rankNumber,
					myDateTime.formatter.format(mm.lastSendDatetime)));
			});

		long start_datetime = System.currentTimeMillis();
		for (int n = 0; n < times; n++) {
			my.println(String.format("Number of trials : %1$d", n + 1));
			try {
				BS5Activity.follow_up_missing_hearts(hearts_unsent);
			} catch (RetryOverException e) {
				fails[0]++;
				my.println("fails : " + fails[0]);
			}
			BS5Activity.swipe_down_to_bottom();
			// ランキング画面に戻れたかチェック
			if (Objects.isNull(BS5Regions.title.exists(BS5Patterns.title_weekly_ranking_094, 0.001d))) {
				fails[0]++;
				my.println("fails : " + fails[0]);
			}
		}
		long elapsed_time = System.currentTimeMillis() - start_datetime;

		my.println("test case name : " + getMethodName());
		my.println("total fails : " + fails[0]);
		my.println("elapsed time : " + elapsed_time + " msec");
		my.println("elapsed time per one time : " + (elapsed_time / times) + " msec");
	}

	/**
	 * ツムツムアプリ終了から起動テスト.
	 * <P>
	 * 判断基準：ランキング画面に戻れたか
	 * @param times 回数
	 */
	public static void test_stop_and_create(int times) {
		final int fails[] = new int[] { 0 };
		long start_datetime = System.currentTimeMillis();
		boolean breaked = false;
		boolean is_bright = false;
		for (int n = 0; n < times; n++) {
			my.println(String.format("Number of trials : %1$d", n + 1));
			BS5Activity.stop_and_create(120);
			// 週間ランキングが表示されているかチェック
			if (Objects.nonNull(BS5Regions.title.exists(BS5Patterns.title_weekly_ranking_094, WAIT_TIMEOUT))) {
				// 左上の1ドットの色を取得し、0.5より上かを判定する
				is_bright = BS5Activity.is_brightness(BS5Regions.title.getLastMatch());

				if (is_bright) {
					breaked = true;
				}
			}
			if (!breaked) {
				fails[0]++;
				my.println("fails : " + fails[0]);
			}
			breaked = false;
		}
		long elapsed_time = System.currentTimeMillis() - start_datetime;

		my.println("test case name : " + getMethodName());
		my.println("total fails : " + fails[0]);
		my.println("elapsed time : " + elapsed_time + " msec");
		my.println("elapsed time per one time : " + (elapsed_time / times) + " msec");
	}

	/**
	 * レジューム位置シークテスト.
	 * <P>
	 * 判断基準：所望のページへ移動できたか
	 * @param target_number_rank ランキングNo.
	 */
	public static void test1_seek_resume_position(int target_number_rank) {
		final int fails[] = new int[] { 0 };
		BS5Activity.recovery_to_ranking(120);
		long start_datetime = System.currentTimeMillis();

		try {
			BS5Activity.seek_resume_position(target_number_rank, true);
			// 所望のページへ移動できたかチェック
			List<Integer> temporay = BS5Activity.recognition_numbers_in_a_page();
			int des_target_number_rank = target_number_rank;
			if (des_target_number_rank < 4) {
				des_target_number_rank = 4;
			}
			if ((Objects.isNull(temporay)) || (!temporay.get(3).equals(des_target_number_rank))) {
				fails[0]++;
				my.println("fails : " + fails[0]);
				my.sleep(500);
				BS5Activity.recovery_to_ranking(30);
			}
		} catch (RetryOverException e) {
			e.printStackTrace();
		}

		long elapsed_time = System.currentTimeMillis() - start_datetime;

		my.println("test case name : " + getMethodName());
		my.println("total fails : " + fails[0]);
		my.println("elapsed time : " + elapsed_time + " msec");
	}

	/**
	 * レジューム位置シークテスト.
	 * <P>
	 * 判断基準：所望のページへ移動できたか
	 * @param target_number_rank ランキングNo.
	 */
	public static void test2_seek_resume_position(int target_number_rank) {
		final int fails[] = new int[] { 0 };
		BS5Activity.recovery_to_ranking(120);
		long start_datetime = System.currentTimeMillis();

		try {
			BS5Activity.seek_resume_position_with_check_row4(target_number_rank, true);
			// 所望のページへ移動できたかチェック
			List<Integer> temporay = BS5Activity.recognition_numbers_in_a_page();
			int des_target_number_rank = target_number_rank;
			if (des_target_number_rank < 4) {
				des_target_number_rank = 4;
			}
			if ((Objects.isNull(temporay)) || (!temporay.get(3).equals(des_target_number_rank))) {
				fails[0]++;
				my.println("fails : " + fails[0]);
				my.sleep(500);
				BS5Activity.recovery_to_ranking(30);
			}
		} catch (RetryOverException e) {
			e.printStackTrace();
		}

		long elapsed_time = System.currentTimeMillis() - start_datetime;

		my.println("test case name : " + getMethodName());
		my.println("total fails : " + fails[0]);
		my.println("elapsed time : " + elapsed_time + " msec");
	}

	/**
	 * エミュ停止＆起動テスト.
	 * <P>
	 * 判断基準：動作中か
	 * @param times 回数
	 */
	public static void test_emu_restart(int times) {
		final int fails[] = new int[] { 0 };
		BS5Activity.recovery_to_ranking(120);
		long start_datetime = System.currentTimeMillis();
		for (int n = 0; n < times; n++) {
			my.println(String.format("Number of trials : %1$d", n + 1));
			BS5Activity.bs_restart(240);
			// 動作中かチェック
			if (!BS5App.is_running(2)) {
				fails[0]++;
				my.println("fails : " + fails[0]);
			}
		}
		long elapsed_time = System.currentTimeMillis() - start_datetime;

		my.println("test case name : " + getMethodName());
		my.println("total fails : " + fails[0]);
		my.println("elapsed time : " + elapsed_time + " msec");
		my.println("elapsed time per one time : " + (elapsed_time / times) + " msec");
	}

}
