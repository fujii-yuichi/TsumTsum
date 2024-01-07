/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.sikuli.script.App;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;

/**
 * スクリーンショット.
 */
public class mySS {

	// ----- 設定値定義 -----
	public static String PATH_SS_SAVE = "";
	public static String DIR_FREEZED_EXISTS = ".freezed";
	public static String DIR_TSUM_CLOCK_SAVE = ".clock";

	// ----- 定数定義 -----
	private static final DateTimeFormatter filename_formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
	private static final DateTimeFormatter dirname_formatter = DateTimeFormatter.ofPattern("yyyyMMddHH");

	// ----- 静的フィールド -----
	private static LocalDateTime dir_datetime = LocalDateTime.now();

	/**
	 * ファイル日時設定.
	 * @param datetime
	 */
	public static void set_FileDateTime(LocalDateTime datetime) {
		dir_datetime = datetime;
	}

	public static void take(String suffix) {
		take(suffix, dir_datetime);
	}

	public static ScreenImage take(String suffix, LocalDateTime datetime) {
		// キャプチャ
		Region win = App.focusedWindow();
		return take(win, suffix, datetime);
	}

	public static ScreenImage take(Region reg, String suffix, LocalDateTime datetime) {
		// ファイル日時が無ければ補正
		if (Objects.isNull(datetime)) {
			datetime = LocalDateTime.now();
		}
		// 保存先パス生成
		StringBuilder sb = new StringBuilder(200);
		sb.append(filename_formatter.format(LocalDateTime.now()));
		sb.append('_');
		sb.append(suffix);
		sb.append(".png");
		String filename = sb.toString();
		String dirname = dirname_formatter.format(datetime);
		Path dirpath = Paths.get(PATH_SS_SAVE, dirname);
		// キャプチャ
		return take(reg, dirpath, filename);
	}

	public static ScreenImage take(Region reg, Path filepath) {
		Path dirpath = filepath.getParent();
		// キャプチャ
		return take(reg, dirpath, filepath.getFileName().toString());
	}

	public static ScreenImage take(Region reg, String dirpaths, String filename) {
		Path dirpath = Paths.get(dirpaths);
		return take(reg, dirpath, filename);
	}

	public static ScreenImage take(Region reg, Path dirpath, String filename) {
		// 保存先生存確認・生成
		try {
			if (!Files.exists(dirpath)) {
				Files.createDirectories(dirpath);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// キャプチャ
		ScreenImage img = ((Screen) reg.getScreen()).cmdCapture(reg, dirpath.toString(), filename);
		return img;
	}

	public static ScreenImage take_for_tsum_clock(Region reg, String filename) {
		Path dirpath = Paths.get(PATH_SS_SAVE, DIR_TSUM_CLOCK_SAVE);
		return take(reg, dirpath, filename);
	}

	public static ScreenImage take_for_freezed_exists(Region reg, String filename) {
		Path dirpath = Paths.get(PATH_SS_SAVE, DIR_FREEZED_EXISTS);
		return take(reg, dirpath, filename);
	}

	public static void take_screen() {
		String filename = String.format("%s.png", filename_formatter.format(LocalDateTime.now()));
		take(Screen.getPrimaryScreen(), mySS.PATH_SS_SAVE + "ED/", filename);
	}

	public static void take_screen(String suffix) {
		take_screen(suffix, dir_datetime);
	}

	public static ScreenImage take_screen(String suffix, LocalDateTime datetime) {
		// キャプチャ
		Region srceen = Screen.getPrimaryScreen();
		return take(srceen, suffix, datetime);

	}

	/**
	 * [test]スクリーンショット撮影.
	 * @param reg リージョン
	 * @return スクリーンショット
	 */
	public static ScreenImage take_test(Region reg) {
		Path filepath = Paths.get(PATH_SS_SAVE, "test.png");
		return mySS.take(reg, filepath);
	}

	public static void config(ConfigureModel config) {
		PATH_SS_SAVE = config.PATH_SS_SAVE;
		DIR_FREEZED_EXISTS = config.DIR_FREEZED_EXISTS;
		DIR_TSUM_CLOCK_SAVE = config.DIR_TSUM_CLOCK_SAVE;

		// 設定値に応じてディレクトリ生成
		try {
			Files.createDirectories(Paths.get(PATH_SS_SAVE));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
