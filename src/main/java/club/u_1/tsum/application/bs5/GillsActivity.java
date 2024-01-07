/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application.bs5;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.sikuli.script.Finder;
import org.sikuli.script.Image;
import org.sikuli.script.Location;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;
import org.sikuli.script.ScreenImage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import club.u_1.tsum.application.ConfigureModel;
import club.u_1.tsum.application.MemberModel;
import club.u_1.tsum.application.MemberModelInfo;
import club.u_1.tsum.application.RetryOverException;
import club.u_1.tsum.application.my;
import club.u_1.tsum.application.myDateTime;
import club.u_1.tsum.application.myLogger;
import club.u_1.tsum.application.mySS;

/**
 * ランキング変動を追っかける.
 * <p>
 * ニックネーム：ギルス
 */
public class GillsActivity extends BS5Activity {

	// ----- 定数定義 -----
	public static double WAIT_TIMEOUT = 0.001;

	public static String PATH_MM_UPDATE_SAVE = "";
	public static String FILENAME_MEMBERS_LIST = "members_list.bin";
	public static String FILENAME_MEMBERS_LIST_BAK = "members_list.bak";
	public static String FILENAME_MEMBERS_LIST_JSON = "members_list.json";
	public static String FILENAME_MEMBERS_LIST_BAK_JSON = "members_list.bak.json";

	// ----- 静的フィールド -----
	public static Region reg_name = null;
	public static Region reg_icon = null;
	public static Region reg_high_score = null;
	public static Region reg_high_score_popup = null;
	public static LocalDateTime store_members_list_datetime = null;

	public static final Pattern hana_high_score_093;

	public static Map<Integer, MemberModel> members_list = new LinkedHashMap<Integer, MemberModel>();
	private static Map<Integer, MemberModel> old_members_list = null;
	public static LinkedHashSet<Integer> priority_members_list = new LinkedHashSet<Integer>();

	// Comparator
	public static Comparator<MemberModel> comparator = Comparator
		.comparing(MemberModel::getLastSendDatetime)
		.thenComparing(Comparator.comparing(MemberModel::getRankNumber).reversed());

	private static final DateTimeFormatter dirname_formatter = DateTimeFormatter.ofPattern("yyyyMMddHH");

	public static final ObjectMapper mapper = new ObjectMapper();

	static {
		hana_high_score_093 = new Pattern("/bs5/hana_high_score.png").similar(0.93f);
		JavaTimeModule jtm = new JavaTimeModule();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.registerModule(jtm);
	}

	/**
	 * ツムアプリRegionを元に各Regionセット.
	 * <P>
	 * *Regions.set_region()からコールされるように。
	 */
	public static void set_region() {
		if (Objects.nonNull(BS5Regions.tsum)) {
			reg_high_score_popup = new Region(BS5Regions.tsum.x + 25, BS5Regions.tsum.y + 328, 485, 325);
			reg_name = new Region(reg_high_score_popup.x + 154, reg_high_score_popup.y + 26, 177, 24);
			reg_high_score = new Region(reg_high_score_popup.x + 0, reg_high_score_popup.y + 268, 485, 48);
			reg_icon = new Region(reg_high_score_popup.x + 110, reg_high_score_popup.y + 6, 36, 36);
		}
	}

	/**
	 * ♥送信日時候補を更新.
	 * @param number_rank ランキングNo.
	 * @param send_datetime ♥送信日時
	 */
	public static void update_send_datetime_candidate(Integer number_rank, LocalDateTime send_datetime_on_system) {
		if (members_list.size() > 0) {
			MemberModel mm = members_list.get(number_rank);
			if (Objects.nonNull(mm)) {
				mm.lastSendDatetimeCandidate = send_datetime_on_system;
				myLogger.fine(String.format("%1$d位, %2$s",
					mm.rankNumber,
					myDateTime.formatter.format(mm.lastSendDatetimeCandidate)));
			}
		}
	}

	/**
	 * ♥送信日時を更新.
	 * @param number_rank ランキングNo.
	 * @param send_datetime ♥送信日時
	 */
	public static void update_send_datetime(Integer number_rank, LocalDateTime send_datetime_on_system) {
		LocalDateTime send_datetime_on_tsum = TsumClock.tsum_date_time(send_datetime_on_system);
		if (members_list.size() > 0) {
			MemberModel mm = members_list.get(number_rank);
			if (Objects.nonNull(mm)) {
				if (Objects.nonNull(mm.lastSendDatetime)) {
					LocalDateTime last_send_datetime_on_tsum = mm.lastSendDatetime.minus(mm.offsetOfNanos, ChronoUnit.NANOS);
					StringBuilder sb = new StringBuilder();
					sb.append("[DEBUG] ");
					sb.append(myDateTime.formatter.format(send_datetime_on_tsum));
					// 前回送信日時がある場合、
					// 今回送信日時と比較して同分回数、同秒回数の更新
					boolean needs_reset = false;
					if (send_datetime_on_tsum.isAfter(last_send_datetime_on_tsum)) {
						long between = ChronoUnit.MINUTES.between(last_send_datetime_on_tsum, send_datetime_on_tsum);
						sb.append(", between=").append(between);
						if (between > 60) {
							// 60分超過の場合、分替わり
							needs_reset = true;
						} else if (between < 59) {
							// 59分未満の場合、連続した更新の可能性のため無視
						} else {
							if (last_send_datetime_on_tsum.getMinute() == send_datetime_on_tsum.getMinute()) {
								sb.append(", min=").append(send_datetime_on_tsum.getMinute());
								// 同分回数インクリメント
								mm.timesOfSameMin++;
								// 秒が同じ、
								// もしくは、
								// 予想される今回送信日時より実際の今回送信日時の方が前
								LocalDateTime ldtExpectation = last_send_datetime_on_tsum.plus(1, ChronoUnit.HOURS);
								if ((last_send_datetime_on_tsum.getSecond() == send_datetime_on_tsum.getSecond()) ||
									(ldtExpectation.isBefore(send_datetime_on_tsum))) {
									sb.append(", sec=").append(send_datetime_on_tsum.getSecond());
									// 同秒回数インクリメント
									mm.timesOfSameSec++;
								} else {
									mm.timesOfSameSec = 0;
								}
							} else {
								needs_reset = true;
							}
						}
					}
					sb.append(", needs_reset=").append(needs_reset);
					if (needs_reset) {
						mm.timesOfSameMin = 0;
						mm.timesOfSameSec = 0;
					}
					myLogger.fine(sb.toString());
				}
				mm.lastSendDatetime = send_datetime_on_system;
				mm.lastSendDatetimeCandidate = null;
				mm.offsetOfNanos = TsumClock.get_offset_nanos();
				myLogger.fine(String.format("%1$d位, %2$s, %3$d, %4$d",
					mm.rankNumber,
					myDateTime.formatter.format(mm.lastSendDatetime),
					mm.timesOfSameMin,
					mm.timesOfSameSec));
			}
		}
	}

	/**
	 * ♥送信日時を取得.
	 * @param number_rank ランキングNo.
	 * @return ♥送信日時
	 * 	<BR>	≠null:正常
	 * 	<BR> 	＝null:異常
	 */
	public static LocalDateTime get_send_datetime(Integer number_rank) {
		if (members_list.size() > 0) {
			MemberModel mm = members_list.get(number_rank);
			if (Objects.nonNull(mm)) {
				return mm.lastSendDatetime;
			}
		}
		return null;
	}

	/**
	 * オフセットナノ秒.を取得.
	 * @param number_rank ランキングNo.
	 * @return オフセットナノ秒.
	 * 	<BR>	≠null:正常
	 * 	<BR> 	＝null:異常
	 */
	public static Long get_offset_nanos(Integer number_rank) {
		if (members_list.size() > 0) {
			MemberModel mm = members_list.get(number_rank);
			if (Objects.nonNull(mm)) {
				return mm.offsetOfNanos;
			}
		}
		return null;
	}

	/**
	 * ♥送信キューを取得.
	 * @return ♥送信キュー
	 */
	public static Set<Integer> get_hearts_send_queue() {
		final LinkedHashSet<Integer> result = new LinkedHashSet<Integer>(priority_members_list);
		final LinkedHashSet<Integer> sorted_list = members_list.values().stream()
			.sorted(GillsActivity.comparator)
			.map(mm -> {
				return Integer.valueOf(mm.rankNumber);
			})
			.collect(Collectors.toCollection(LinkedHashSet::new));
		sorted_list.removeAll(result);
		result.addAll(sorted_list);
		return result;
	}

	/**
	 * ロード.
	 * @return 結果
	 * 	<BR>	true:正常
	 * 	<BR> 	false:異常
	 */
	public static boolean load() {
		//		if (members_list.size() > 0) {
		//			// メモリ上にロード済みであれば何もしない
		//			return true;
		//		}

		Path target_file = Paths.get(BS5Instance.get_mm_save_path(), FILENAME_MEMBERS_LIST_JSON);

		if (!target_file.toFile().exists()) {
			// ファイルが無い場合は仕方がない、成功
			return true;
		}

		LinkedHashMap<Integer, MemberModel> return_object = new LinkedHashMap<Integer, MemberModel>();
		try (FileInputStream fis = new FileInputStream(target_file.toFile())) {
			my.println(String.format("members_list ロード開始, target = %1$s", target_file.toString()));

			MemberModelInfo info = mapper.readValue(fis, MemberModelInfo.class);
			if (info instanceof MemberModelInfo) {
				return_object = info.members_list;
				my.println(String.format("members_list ロード成功, count = %1$d", return_object.size()));
			}
			members_list = return_object;
			return true;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return false;
	}

	/**
	 * ストア.
	 * @return 結果
	 * 	<BR>	true:正常
	 * 	<BR> 	false:異常
	 */
	public static boolean store() {
		Path target_file = Paths.get(BS5Instance.get_mm_save_path(), FILENAME_MEMBERS_LIST_JSON);

		// バックアップ
		if (Files.exists(target_file)) {
			Path backup_file = Paths.get(BS5Instance.get_mm_save_path(), FILENAME_MEMBERS_LIST_BAK_JSON);
			try {
				Files.copy(target_file, backup_file, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// 前回送信日時候補を前回送信日時に確定させる
		members_list.values().stream()
			.forEach(mm -> {
				if (Objects.nonNull(mm.lastSendDatetimeCandidate)) {
					myLogger.warn(String.format("前回送信日時候補有り, %1$d位, %2$s, %3$s",
						mm.rankNumber,
						myDateTime.formatter.format(mm.lastSendDatetime),
						myDateTime.formatter.format(mm.lastSendDatetimeCandidate)));
					mm.lastSendDatetime = mm.lastSendDatetimeCandidate;
				}
			});

		// ファイル出力
		try (FileOutputStream fos = new FileOutputStream(target_file.toFile())) {
			my.println(String.format("members_list ストア開始, target = %1$s", target_file.toString()));
			MemberModelInfo info = new MemberModelInfo();
			info.members_list = (LinkedHashMap<Integer, MemberModel>) members_list;
			mapper.writeValue(fos, info);
			my.println(String.format("members_list ストア成功"));
			return true;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return false;
	}

	/**
	 * 生成 for ランキングリセット時.
	 */
	public static void create_for_ranking_reset() {
		try {
			// findAny用リスト
			final LinkedList<MemberModel> members_list_values = new LinkedList<MemberModel>();
			final LinkedList<Object> list_high_score_popup = new LinkedList<Object>();
			final LinkedList<Object> list_name = new LinkedList<Object>();
			final LinkedList<Object> list_high_score = new LinkedList<Object>();
			final LinkedList<Object> list_icon = new LinkedList<Object>();

			if (members_list.size() > 0) {
				// 2回目以降→マップ更新
				// 現在のマップを退避
				old_members_list = members_list;
				// 名前→ハイスコア→プロフ画像の順に検索し、一致する情報の送信日時をコピーする。
				members_list_values.addAll(members_list.values());

				// findAny用リスト作成
				for (MemberModel m2 : members_list_values) {
					String m2_high_score_popup_path = Paths.get(BS5Instance.get_mm_save_path(), m2.highScorePopup).toString();
					Image high_score_popup_img = Image.create(m2_high_score_popup_path);
					Image name_img = high_score_popup_img.getSub(154, 26, 177, 24);
					Image high_score_img = high_score_popup_img.getSub(0, 268, 485, 48);
					Image icon_img = high_score_popup_img.getSub(110, 6, 36, 36);

					// // デバッグ用 start
					// {
					// 	String source = name_img.asFile();
					// 	File sourcFile = new File(source);
					// 	Path sourcePath = sourcFile.toPath();
					// 	Path targetPath = Paths.get(mySS.C_PATH_SS_SAVE, "name_img" + sourcFile.getName());
					// 	Files.copy(sourcePath, targetPath);
					// }
					// {
					// 	String source = high_score_img.asFile();
					// 	File sourcFile = new File(source);
					// 	Path sourcePath = sourcFile.toPath();
					// 	Path targetPath = Paths.get(mySS.C_PATH_SS_SAVE, "high_score_img" + sourcFile.getName());
					// 	Files.copy(sourcePath, targetPath);
					// }
					// {
					// 	String source = icon_img.asFile();
					// 	File sourcFile = new File(source);
					// 	Path sourcePath = sourcFile.toPath();
					// 	Path targetPath = Paths.get(mySS.C_PATH_SS_SAVE, "icon_img" + sourcFile.getName());
					// 	Files.copy(sourcePath, targetPath);
					// }
					// // デバッグ用 end

					list_high_score_popup.add(new Pattern(high_score_popup_img).similar(0.93f));
					list_name.add(new Pattern(name_img).similar(0.93f));
					list_high_score.add(new Pattern(high_score_img).similar(0.93f));
					list_icon.add(new Pattern(icon_img).similar(0.93f));
				}
			}

			// 取得済み情報セット
			int number_of_my_rank = BS5Status.get_number_of_my_rank();
			int number_of_members = BS5Status.get_number_of_members();

			long start_time = System.nanoTime();
			long elapsed_time = start_time;
			// キャッシュクリア
			number_rank_list_cache = null;
			// ランキング最下位へスワイプ
			seek_resume_position(number_of_members);
			// ハート未送信集合
			List<Integer> members = IntStream.rangeClosed(1, number_of_members).boxed().collect(Collectors.toList());
			HashSet<Integer> hearts_unsent = new HashSet<Integer>(members);
			hearts_unsent.remove(number_of_my_rank);
			BS5Status.set_hearts_unsent(hearts_unsent);

			priority_members_list = new LinkedHashSet<Integer>();

			List<Integer> number_rank_list = null;
			LinkedHashMap<Integer, MemberModel> now_list = new LinkedHashMap<Integer, MemberModel>();
			while (true) {
				try {
					// レジューム位置シーク
					final int lowest_number_rank = Collections.max(hearts_unsent);
					seek_resume_position(lowest_number_rank, true);
					// ページ単位のハート送信対象辞書(dict)
					List<Map<Integer, Integer>> dict = generate_dict_per_page(hearts_unsent);
					Map<Integer, Integer> target_per_page = dict.get(0);
					Map<Integer, Integer> number_rank_per_page = dict.get(1);
					number_rank_list = new ArrayList<Integer>();
					for (int i = 0; i < 4; i++) {
						number_rank_list.add(number_rank_per_page.get(Integer.valueOf(i)));
					}
					Map<Integer, Integer> target_per_page_for_loop = new HashMap<>(target_per_page);
					myLogger.fine(
						String.format("Collections.max(hearts_unsent)=%3$d, target_per_page_for_loop:key=%1$s, values=%2$s",
							Arrays.toString(target_per_page_for_loop.keySet().toArray()),
							Arrays.toString(target_per_page_for_loop.values().toArray()),
							Collections.max(hearts_unsent)));
					// ページ単位のハート送信対象があればメンバー情報取得を実施する
					if (target_per_page_for_loop.size() > 0) {
						target_per_page_for_loop.entrySet().stream()
							.sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
							.forEach(s -> {
								Integer number_rank = s.getKey();
								Integer row = s.getValue();
								// ランキングNo.と行数が一致しているかチェック
								List<Integer> local_number_rank_list = recognition_numbers_in_a_page();
								int local_list_index = (row + 3) % 4;
								if (local_number_rank_list.get(local_list_index).intValue() != number_rank.intValue()) {
									myLogger.warn_members(
										String.format("ランキングNoが一致しない:%1$d位(%2$d)",
											number_rank, local_list_index));
									mySS.take_screen(String.format("%1$d位ランキングNoが一致しない", number_rank));
									myLogger.info(
										String.format("number_rank=%1$d, row=%2$d, local_number_rank_list=%3$s",
											number_rank,
											row,
											Arrays.toString(local_number_rank_list.toArray())));
									// レジューム位置シーク
									try {
										seek_resume_position(lowest_number_rank, true);
									} catch (RetryOverException e) {
										e.printStackTrace();
									}
								}
								my.println(String.format("%1$d位", number_rank));

								// ハイスコア表示
								show_high_score(row);

								MemberModel mm = null;
								ScreenImage high_score_popup_img = take(reg_high_score_popup);

								boolean matched = false;
								int index = -1;
								LocalDateTime m1_last_send_datetime = LocalDateTime.MAX;
								int m1_times_of_same_min = 0;
								int m1_times_of_same_sec = 0;
								long m1_offset_of_nanos = 0;
								{
									List<Match> matchs_name = reg_name.findAnyList((List<Object>) list_name);
									if (matchs_name.size() > 0) {
										//  名前一致
										for (int i = 0; i < matchs_name.size(); i++) {
											Match match = matchs_name.get(i);
											if (!matched) {
												index = match.getIndex();
											}
											my.println(String.format("matchs_name[%1$d]=%2$d", i, index));
											matched = true;
										}
									}
								}
								{
									List<Match> matchs_high_score = reg_high_score.findAnyList((List<Object>) list_high_score);
									if (matchs_high_score.size() > 0) {
										//  ハイスコア一致
										for (int i = 0; i < matchs_high_score.size(); i++) {
											Match match = matchs_high_score.get(i);
											if (!matched) {
												index = match.getIndex();
											}
											my.println(String.format("matchs_high_score[%1$d]=%2$d", i, index));
											matched = true;
										}
									} else if (matched) {
										// 既に一致していてハイスコアが一致しない場合、
										myLogger.info_members(String.format("ハイスコア更新, %1$s",
											Paths.get(high_score_popup_img.getFile()).getFileName().toString()));
										my.copy(Paths.get(high_score_popup_img.getFile()),
											Paths.get(PATH_MM_UPDATE_SAVE));
									}
								}
								if (!matched) {
									List<Match> matchs_icon = reg_icon.findAnyList((List<Object>) list_icon);
									if (matchs_icon.size() > 0) {
										// プロフ画像一致
										for (int i = 0; i < matchs_icon.size(); i++) {
											Match match = matchs_icon.get(i);
											if (!matched) {
												index = match.getIndex();
											}
											my.println(String.format("matchs_icon[%1$d]=%2$d", i, index));
											matched = true;
										}
									}
								}
								// 一致していれば、送信日時、同分回数、同秒回数をコピーする
								if (matched) {
									MemberModel m2 = members_list_values.get(index);
									m1_last_send_datetime = m2.lastSendDatetime;
									m1_times_of_same_min = m2.timesOfSameMin;
									m1_times_of_same_sec = m2.timesOfSameSec;
									m1_offset_of_nanos = m2.offsetOfNanos;

									members_list_values.remove(index);
									list_high_score_popup.remove(index);
									list_icon.remove(index);
									list_name.remove(index);
									list_high_score.remove(index);

									// 優先的に送信する人
									if (Objects.nonNull(reg_high_score.exists(hana_high_score_093, 0.1d))) {
										// ハナちゃんと一致
										myLogger.info(String.format("ハナちゃんを見つけた！, %1$d位", number_rank));
										priority_members_list.add(number_rank);
									}
								} else if (m1_last_send_datetime.isEqual(LocalDateTime.MAX)) {
									myLogger.info_members(String.format("送信日時なし, %1$d位", number_rank));
								}

								// メンバー情報生成
								mm = MemberModel.of(
									number_rank,
									m1_last_send_datetime,
									high_score_popup_img.getFile().replace(BS5Instance.get_mm_save_path(), ""),
									m1_times_of_same_min,
									m1_times_of_same_sec,
									m1_offset_of_nanos);

								// ハイスコア非表示
								hide_high_score();

								if (Objects.nonNull(mm)) {
									now_list.put(Integer.valueOf(number_rank), mm);
									// 全体のハート送信対象から抜く
									try {
										if (!hearts_unsent.remove(number_rank)) {
											myLogger.warn_members(String.format("二重取得の疑い, %1$d位", number_rank));
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
									// ページ単位のハート送信対象から抜く
									try {
										if (Objects.isNull(target_per_page.remove(number_rank))) {
											mySS.take("ページ移動か二重取得の疑い");
											myLogger.warn_members(
												String.format("ページ移動か二重取得の疑い, %1$d位", number_rank));
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
								// ランキング表示に戻れているかチェック
								recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);
							});
					}
					// 生存通知
					myDateTime.set_datetime(LocalDateTime.now(), DATETIME_ALIVE);
					// ハート未送信がなくなったらループを抜ける
					if (hearts_unsent.size() <= 0) {
						break;
					}
					myLogger.info_members(
						String.format("seek_resume_position:Collections.max(hearts_unsent)=%1$d位",
							Collections.max(hearts_unsent)));
					// レジューム位置シーク
					seek_resume_position(Collections.max(hearts_unsent));
				} catch (RetryOverException | RuntimeException e) {
					// スタックトレース出力
					myLogger.error(e);
					mySS.take_screen("Exception_create_for_ranking_reset");
					// ツム再起動
					stop_and_create(LIMIT_SECONDS_RESTART);
				} catch (Exception e) {
					myLogger.error(e);
				}
			}
			members_list = now_list;

			// デバッグ用
			members_list.values().stream()
				.sorted(GillsActivity.comparator)
				.forEach(mm -> {
					myLogger.info_members(String.format("%1$d位, %2$s, 同分回数=%3$d, 同秒回数=%4$d",
						mm.rankNumber,
						myDateTime.formatter.format(mm.lastSendDatetime),
						mm.timesOfSameMin,
						mm.timesOfSameSec));
				});

			elapsed_time = (System.nanoTime() - start_time) / 1000000;
			myLogger.info_members(String.format("全メンバー情報生成, %1$d(ms)", elapsed_time));
		} catch (Exception e) {
			myLogger.error(e);
		}
	}

	/**
	 * 生成.
	 */
	public static void create() {
		try {
			// 取得済み情報セット
			int number_of_my_rank = BS5Status.get_number_of_my_rank();
			int number_of_members = BS5Status.get_number_of_members();

			long start_time = System.nanoTime();
			long elapsed_time = start_time;
			// キャッシュクリア
			number_rank_list_cache = null;
			// ランキング最下位へスワイプ
			seek_resume_position(number_of_members);
			// ハート未送信集合
			List<Integer> members = IntStream.rangeClosed(1, number_of_members).boxed().collect(Collectors.toList());
			HashSet<Integer> hearts_unsent = new HashSet<Integer>(members);
			hearts_unsent.remove(number_of_my_rank);
			BS5Status.set_hearts_unsent(hearts_unsent);

			priority_members_list = new LinkedHashSet<Integer>();

			Map<Integer, MemberModel> now_list = create_members_info(hearts_unsent);

			if (members_list.size() > 0) {
				// 2回目以降→マップ更新
				// 現在のマップを退避
				old_members_list = members_list;
				// 名前→ハイスコア→プロフ画像の順に検索し、一致する情報の送信日時をコピーする。
				Collection<MemberModel> members_list_values = new LinkedHashSet<MemberModel>(members_list.values());
				now_list.entrySet().stream()
					.forEach(e1 -> {
						MemberModel m1 = e1.getValue();
						try {
							String m1_high_score_popup_path = Paths.get(BS5Instance.get_mm_save_path(), m1.highScorePopup).toString();
							final Image image_from_file = Image.create(m1_high_score_popup_path);
							final Finder high_score_popup_src = new Finder(image_from_file);

							for (MemberModel m2 : members_list_values) {
								String m2_high_score_popup_path = Paths.get(BS5Instance.get_mm_save_path(), m2.highScorePopup).toString();
								Image high_score_popup_img = Image.create(m2_high_score_popup_path);
								Image name_img = high_score_popup_img.getSub(154, 26, 177, 24);
								Image high_score_img = high_score_popup_img.getSub(0, 268, 485, 48);
								Image icon_img = high_score_popup_img.getSub(110, 6, 36, 36);

								boolean matched = false;
								{
									Pattern name_des = new Pattern(name_img).similar(0.97f);
									high_score_popup_src.find(name_des);
									if (high_score_popup_src.hasNext()) {
										//  名前一致
										matched = true;
										high_score_popup_src.next();
									}
								}
								{
									Pattern high_score_des = new Pattern(high_score_img).similar(0.95f);
									high_score_popup_src.find(high_score_des);
									if (high_score_popup_src.hasNext()) {
										//  ハイスコア一致
										matched = true;
										high_score_popup_src.next();
									} else if (matched) {
										// 既に一致していてハイスコアが一致しない場合、
										Path update_high_score_path = Paths.get(BS5Instance.get_mm_save_path(), m1.highScorePopup);
										myLogger.info_members(String.format("ハイスコア更新, %1$s",
											update_high_score_path.getFileName().toString()));
										my.copy(update_high_score_path, Paths.get(PATH_MM_UPDATE_SAVE));
									}
								}
								if (!matched) {
									Pattern icon_des = new Pattern(icon_img).similar(0.97f);
									high_score_popup_src.find(icon_des);
									if (high_score_popup_src.hasNext()) {
										// プロフ画像一致
										matched = true;
										high_score_popup_src.next();
									}
								}
								// 一致していれば、送信日時をコピーする
								if (matched) {
									if (!m1.lastSendDatetime.isEqual(LocalDateTime.MAX)) {
										// 重複あり
										myLogger.info(String.format("重複あり %1$d位→%2$d位, %3$s",
											m1.rankNumber,
											m2.rankNumber,
											myDateTime.formatter.format(m1.lastSendDatetime)));
									}
									m1.lastSendDatetime = m2.lastSendDatetime;
									m1.timesOfSameMin = m2.timesOfSameMin;
									m1.timesOfSameSec = m2.timesOfSameSec;
									members_list_values.remove(m2);

									// 優先的に送信する人
									high_score_popup_src.find(hana_high_score_093);
									if (high_score_popup_src.hasNext()) {
										// ハナちゃんと一致
										myLogger.info(String.format("ハナちゃんを見つけた！, %1$d位", m1.rankNumber));
										priority_members_list.add(m1.rankNumber);
										high_score_popup_src.next();
									}

									break;
								}
							}
							if (m1.lastSendDatetime.isEqual(LocalDateTime.MAX)) {
								myLogger.info_members(String.format("送信日時なし, %1$d位", m1.rankNumber));
							}
						} catch (Exception e) {
							myLogger.error(e);
						}
					});

			} else {
				// 初回
				// 無処理
				;
			}
			members_list = now_list;

			// デバッグ用
			members_list.values().stream()
				.sorted(GillsActivity.comparator)
				.forEach(mm -> {
					myLogger.info_members(String.format("%1$d位, %2$s, 同分回数=%3$d, 同秒回数=%4$d",
						mm.rankNumber,
						myDateTime.formatter.format(mm.lastSendDatetime),
						mm.timesOfSameMin,
						mm.timesOfSameSec));
				});

			elapsed_time = (System.nanoTime() - start_time) / 1000000;
			myLogger.info_members(String.format("全メンバー情報生成, %1$d(ms)", elapsed_time));
		} catch (Exception e) {
			myLogger.error(e);
		}
	}

	/**
	 * メンバー情報作成.
	 * <P>
	 * 現在のランキングに対してメンバー情報のマップを作成する。
	 * @param hearts_unsent
	 * @return メンバー情報マップ
	 */
	public static Map<Integer, MemberModel> create_members_info(HashSet<Integer> hearts_unsent) {
		List<Integer> number_rank_list = null;
		LinkedHashMap<Integer, MemberModel> list = new LinkedHashMap<Integer, MemberModel>();
		while (true) {
			try {
				// レジューム位置シーク
				final int lowest_number_rank = Collections.max(hearts_unsent);
				seek_resume_position(lowest_number_rank, true);
				// ページ単位のハート送信対象辞書(dict)
				List<Map<Integer, Integer>> dict = generate_dict_per_page(hearts_unsent);
				Map<Integer, Integer> target_per_page = dict.get(0);
				Map<Integer, Integer> number_rank_per_page = dict.get(1);
				number_rank_list = new ArrayList<Integer>();
				for (int i = 0; i < 4; i++) {
					number_rank_list.add(number_rank_per_page.get(Integer.valueOf(i)));
				}
				Map<Integer, Integer> target_per_page_for_loop = new HashMap<>(target_per_page);
				myLogger.fine(
					String.format("Collections.max(hearts_unsent)=%3$d, target_per_page_for_loop:key=%1$s, values=%2$s",
						Arrays.toString(target_per_page_for_loop.keySet().toArray()),
						Arrays.toString(target_per_page_for_loop.values().toArray()),
						Collections.max(hearts_unsent)));
				// ページ単位のハート送信対象があればメンバー情報取得を実施する
				if (target_per_page_for_loop.size() > 0) {
					target_per_page_for_loop.entrySet().stream()
						.sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
						.forEach(s -> {
							Integer number_rank = s.getKey();
							Integer row = s.getValue();
							// ランキングNo.と行数が一致しているかチェック
							List<Integer> local_number_rank_list = recognition_numbers_in_a_page();
							int local_list_index = (row + 3) % 4;
							if (local_number_rank_list.get(local_list_index).intValue() != number_rank.intValue()) {
								myLogger.info(
									String.format("ランキングNoが一致しない:%1$d位(%2$d)",
										number_rank, local_list_index));
								mySS.take_screen(String.format("%1$d位ランキングNoが一致しない", number_rank));
								myLogger.info(
									String.format("number_rank=%1$d, row=%2$d, local_number_rank_list=%3$s",
										number_rank,
										row,
										Arrays.toString(local_number_rank_list.toArray())));
								// レジューム位置シーク
								try {
									seek_resume_position(lowest_number_rank, true);
								} catch (RetryOverException e) {
									e.printStackTrace();
								}
							}
							my.println(String.format("%1$d位", number_rank));

							// ハイスコア表示
							show_high_score(row);

							// メンバー情報セット
							MemberModel mm = make_a_member_info(number_rank);

							// ハイスコア非表示
							hide_high_score();

							if (Objects.nonNull(mm)) {
								list.put(Integer.valueOf(number_rank), mm);
								// 全体のハート送信対象から抜く
								try {
									if (!hearts_unsent.remove(number_rank)) {
										myLogger.warn_members(String.format("二重取得の疑い, %1$d位", number_rank));
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
								// ページ単位のハート送信対象から抜く
								try {
									if (Objects.isNull(target_per_page.remove(number_rank))) {
										mySS.take("ページ移動か二重取得の疑い");
										myLogger.warn_members(String.format("ページ移動か二重取得の疑い, %1$d位", number_rank));
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							// ランキング表示に戻れているかチェック
							recovery_to_ranking(LIMIT_SECONDS_RECOVERY_TO_RANKING);
						});
				} else {
					// フリーズ検出のためのハイスコア表示
					show_high_score(1);
				}
				// 生存通知
				myDateTime.set_datetime(LocalDateTime.now(), DATETIME_ALIVE);
				// ハート未送信がなくなったらループを抜ける
				if (hearts_unsent.size() <= 0) {
					break;
				}
				// レジューム位置シーク
				seek_resume_position(Collections.max(hearts_unsent));
			} catch (RetryOverException | RuntimeException e) {
				// スタックトレース出力
				myLogger.error(e);
				mySS.take_screen("Exception_create_members_info");
				// ツム再起動
				stop_and_create(LIMIT_SECONDS_RESTART);
			} catch (Exception e) {
				myLogger.error(e);
			}
		}

		return list;
	}

	/**
	 * ハイスコア表示.
	 * @param row ページ内タップ対象段
	 * @return メンバー情報
	 */
	public static void show_high_score(int row) {
		Location l = BS5Regions.rank_list.getTopLeft().offset(OFFSET_X_FROM_LEADING_EDGE_OF_RANK_LIST, ROW_TO_OFFSET[row]);
		my.println(String.format("ランキングをタップ Y=%1$d", ROW_TO_OFFSET[row]));
		for (int i = 0; i < RETRY_TAP_HIGH_SCORE; i++) {
			if (Objects.isNull(BS5Regions.title.exists(BS5Patterns.title_weekly_ranking_094, 0))) {
				break;
			}
			my.single_tap_by_Location(l);
		}
		// これまでのハイスコアが表示されているか判定
		boolean breaked = false;
		for (int i = 0; i < RETRY_EXISTS_HIGH_SCORE; i++) {
			if (Objects.nonNull(BS5Regions.high_score_so_far.exists(BS5Patterns.high_score_so_far_090, WAIT_TIMEOUT))) {
				my.println("ハイスコア表示");
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
			mySS.take("High_score_is_not_displayed");
			throw new RuntimeException(
				String.format("ハイスコア表示されていない？ Y=%1$d",
					ROW_TO_OFFSET[row]));
		}
		my.sleep(300);
	}

	/**
	 * ハイスコア非表示.
	 */
	public static void hide_high_score() {
		my.println("とじるをタップ");
		LocalDateTime single_tap_limit_datetime = LocalDateTime.now().plus(TAP_INTERVAL_MILLIS, ChronoUnit.MILLIS);
		Location l = BS5Regions.close_lower.getCenter();
		for (int i = 0; i < RETRY_TAP_UPPER_CLOSE; i++) {
			if (LocalDateTime.now().isAfter(single_tap_limit_datetime)) {
				break;
			}
			my.single_tap_by_Location(l);
			my.sleep(40);
		}
		// これまでのハイスコアが非表示されているか判定
		boolean breaked = false;
		for (int i = 0; i < RETRY_DISAPPEAR_HIGH_SCORE; i++) {
			if (Objects.nonNull(BS5Regions.title.exists(BS5Patterns.title_weekly_ranking_094, WAIT_TIMEOUT))) {
				my.println("ハイスコア非表示");
				breaked = true;
				break;
			}
			my.sleep(100);
		}
		if (!breaked) {
			mySS.take("High_score_is_displayed");
			throw new RuntimeException("ハイスコア非表示されていない？");
		}
		my.sleep(300);
	}

	/**
	 * メンバー情報生成.
	 * @return メンバー情報
	 */
	public static MemberModel make_a_member_info(int number_rank) {
		ScreenImage high_score_popup_img = take(reg_high_score_popup);
		MemberModel result = MemberModel.of(
			number_rank,
			LocalDateTime.MAX,
			high_score_popup_img.getFile().replace(BS5Instance.get_mm_save_path(), ""));

		return result;
	}

	/**
	 * スクリーンショット撮影.
	 * @param reg リージョン
	 * @return スクリーンショット
	 */
	public static ScreenImage take(Region reg) {
		Path filepath = Paths.get(BS5Instance.get_mm_save_path(), String.format("%d.png", new Date().getTime()));
		return mySS.take(reg, filepath);
	}

	/**
	 * お掃除.
	 * @param list
	 */
	public static void cleaning() {
		if (Objects.isNull(old_members_list)) {
			return;
		}

		if (old_members_list.size() > 0) {
			LocalDateTime dir_datetime = BS5Status.get_first_send_datetime();
			// 保存先ディレクトリ日時が無ければ補正
			if (Objects.isNull(dir_datetime)) {
				dir_datetime = LocalDateTime.now();
			}
			String dirname = dirname_formatter.format(dir_datetime);
			Path dirpath = Paths.get(BS5Instance.get_mm_save_path(), dirname);
			// 保存先生存確認・生成
			try {
				if (!Files.exists(dirpath)) {
					Files.createDirectories(dirpath);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			old_members_list.entrySet().stream()
				.forEach(m -> {
					MemberModel mm = m.getValue();

					Path high_score_popup = Paths.get(BS5Instance.get_mm_save_path(), mm.highScorePopup);
					if (Files.exists(high_score_popup)) {
						my.move(high_score_popup, dirpath);
					}
				});
			// バックアップ
			Path target_file = Paths.get(BS5Instance.get_mm_save_path(), FILENAME_MEMBERS_LIST);
			if (Files.exists(target_file)) {
				Path backup_file = Paths.get(dirpath.toString(), FILENAME_MEMBERS_LIST);
				try {
					Files.copy(target_file, backup_file, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		old_members_list = null;
	}

	public static void config(ConfigureModel config) {
		WAIT_TIMEOUT = config.WAIT_TIMEOUT;

		PATH_MM_UPDATE_SAVE = config.PATH_MM_UPDATE_SAVE;
		FILENAME_MEMBERS_LIST = config.FILENAME_MEMBERS_LIST;
		FILENAME_MEMBERS_LIST_BAK = config.FILENAME_MEMBERS_LIST_BAK;
		FILENAME_MEMBERS_LIST_JSON = config.FILENAME_MEMBERS_LIST_JSON;
		FILENAME_MEMBERS_LIST_BAK_JSON = config.FILENAME_MEMBERS_LIST_BAK_JSON;
	}

	///// 以下、テスト用メソッド /////

	public static void test() {
		// reg_high_score.highlight(1);
		// if (Objects.nonNull(reg_high_score.exists(hana_high_score_093))) {
		// 	reg_high_score.getLastMatch().highlight(2);
		// }
		// my.exit(0);

		load();

		// デバッグ用
		members_list.values().stream()
			.sorted(GillsActivity.comparator)
			.forEach(mm -> {
				myLogger.info_members(String.format("%1$d位, %2$s, 同分回数=%3$d, 同秒回数=%4$d",
					mm.rankNumber,
					myDateTime.formatter.format(mm.lastSendDatetime),
					mm.timesOfSameMin,
					mm.timesOfSameSec));
			});
		// store();
		// my.exit(0);

		// BS5Status.set_last_time_last_send_datetime(myDateTime.get_datetime("last_time_last_send_datetime"));
		// members_list.values().stream()
		// 	.sorted(GillsActivity.comparator)
		// 	.forEach(mm -> {
		// 		update_send_datetime(mm.rankNumber, BS5Status.get_last_time_last_send_datetime());
		// 	});

		// store();
		// my.exit(0);

		// 自身の位置取得
		BS5Status.set_number_of_my_rank(to_my_ranking());
		recovery_to_ranking(120);
		// ランキング最下位へスワイプ
		swipe_down_to_bottom();
		fit_seperator_line_to_bottom_border();
		// ランキングメンバー数取得
		int number_of_members = -1;
		while (number_of_members <= 0) {
			List<Integer> number_rank_list = recognition_numbers_in_a_page();
			if (Objects.isNull(number_rank_list)) {
				// 失敗した
				recovery_to_ranking(30);
				swipe_down_to_bottom();
				continue;
			}
			number_of_members = number_rank_list.get(2).intValue();
		}
		BS5Status.set_number_of_members(number_of_members);
		my.println(String.format("メンバー数%1$d人", number_of_members));

		// create();
		create_for_ranking_reset();

		// GillsActivity.store();

		Set<Integer> hearts_unsent = new LinkedHashSet<Integer>();
		Iterator<Integer> iterator_integer = null;

		// ハート未送信集合
		BS5Status.set_hearts_unsent(GillsActivity.get_hearts_send_queue());
		hearts_unsent = BS5Status.get_hearts_unsent();

		// レジューム位置シーク
		iterator_integer = hearts_unsent.iterator();
		try {
			seek_resume_position(iterator_integer.next());
		} catch (RetryOverException e) {
			e.printStackTrace();
		}

		// // BS2のmembers_listを読み込み、ランキングNo.から♥送信日時を更新する
		// LinkedHashMap<Integer, MemberModel> bs2_members_list = new LinkedHashMap<Integer, MemberModel>();
		// FileInputStream fis = null;
		// ObjectInputStream ois = null;
		// Path target_file = Paths.get("/Users/yuichi/Tsum/MM/bs2/", C_FILENAME_MEMBERS_LIST);

		// if (!target_file.toFile().exists()) {
		// 	// ファイルが無い場合は仕方がない、終了
		// 	return;
		// }

		// try {
		// 	my.println(String.format("members_list ロード開始, target = %1$s", target_file.toString()));
		// 	fis = new FileInputStream(target_file.toFile());
		// 	ois = new ObjectInputStream(fis);
		// 	Object o = ois.readObject();
		// 	if (o instanceof LinkedHashMap) {
		// 		bs2_members_list = (LinkedHashMap<Integer, MemberModel>) o;
		// 		my.println(String.format("members_list ロード成功, count = %1$d", bs2_members_list.size()));
		// 	}
		// } catch (IOException ioe) {
		// 	ioe.printStackTrace();
		// } catch (ClassNotFoundException cnfe) {
		// 	cnfe.printStackTrace();
		// } finally {
		// 	try {
		// 		ois.close();
		// 	} catch (Exception e) {
		// 		e.printStackTrace();
		// 	}
		// }

		// bs2_members_list.values().stream()
		// 	.sorted(GillsActivity.comparator)
		// 	.forEach(mm -> {
		// 		update_send_datetime(mm.rankNumber, mm.lastSendDatetime);
		// 	});

		// // デバッグ用
		// members_list.values().stream()
		// 	.sorted(GillsActivity.comparator)
		// 	.forEach(mm -> {
		// 		myLogger.info_members(String.format("%1$d位, %2$s",
		// 			mm.rankNumber,
		// 			myDateTime.formatter.format(mm.lastSendDatetime)));
		// 	});

		// GillsActivity.store();
	}
}
