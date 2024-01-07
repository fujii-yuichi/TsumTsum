/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * myDateTime.
 */
public class myDateTime {

	// ----- 設定値定義 -----
	public static String PATH_DATETIME = "";

	// ----- 定数定義 -----
	public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");

	/**
	 * ファイル日時設定.
	 * @param datetime
	 */
	public static void set_FileDateTime(LocalDateTime datetime) {
		myLogger.set_FileHandelr(datetime);
		mySS.set_FileDateTime(datetime);
	}

	/**
	 * ファイル出力.
	 * @param datetime 対象日時
	 * @param filename ファイル名
	 */
	public static void set_datetime(LocalDateTime datetime, String filename) {
		Path fooPath = Paths.get(PATH_DATETIME, filename);
		try (BufferedWriter writer = Files.newBufferedWriter(fooPath)) {
			writer.append(formatter.format(datetime));
			writer.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ファイル入力.
	 * @param filename
	 * @return 対象日時
	 */
	public static LocalDateTime get_datetime(String filename) {
		Path fooPath = Paths.get(PATH_DATETIME, filename);
		LocalDateTime result = LocalDateTime.now();
		try (BufferedReader reader = Files.newBufferedReader(fooPath)) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				result = LocalDateTime.parse(line, formatter);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void config(ConfigureModel config) {
		PATH_DATETIME = config.PATH_DATETIME;

		// 設定値に応じてディレクトリ生成
		try {
			Files.createDirectories(Paths.get(PATH_DATETIME));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
