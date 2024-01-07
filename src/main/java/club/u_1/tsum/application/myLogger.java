/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class myLogger {

	/**
	 * LogFormatter.
	 */
	static private class LogFormatter extends Formatter {

		private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");

		private static final Map<Level, String> levelMsgMap = Collections.unmodifiableMap(
			new HashMap<Level, String>() {
				{
					put(Level.SEVERE, "SEVERE");
					put(Level.WARNING, "WARN");
					put(Level.INFO, "INFO");
					put(Level.CONFIG, "CONF");
					put(Level.FINE, "FINE");
					put(Level.FINER, "FINE");
					put(Level.FINEST, "FINE");
				}
			});

		private AtomicInteger nameColumnWidth = new AtomicInteger(16);

		public static void applyToRoot() {
			applyToRoot(new ConsoleHandler());
		}

		public static void applyToRoot(Handler handler) {
			handler.setLevel(Level.FINE);
			handler.setFormatter(new LogFormatter());
			Logger root = Logger.getLogger("");
			root.setUseParentHandlers(false);
			for (Handler h : root.getHandlers()) {
				if (h instanceof ConsoleHandler)
					root.removeHandler(h);
			}
			root.addHandler(handler);
		}

		@Override
		public String format(LogRecord record) {

			StringBuilder sb = new StringBuilder(200);

			Instant instant = Instant.ofEpochMilli(record.getMillis());
			LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
			sb.append(formatter.format(ldt));
			sb.append(",");

			sb.append("[");
			sb.append(levelMsgMap.get(record.getLevel()));
			sb.append("],");

			// String category;
			// if (record.getSourceClassName() != null) {
			// 	category = record.getSourceClassName();
			// 	if (record.getSourceMethodName() != null) {
			// 		category += " " + record.getSourceMethodName();
			// 	}
			// } else {
			// 	category = record.getLoggerName();
			// }
			// int width = nameColumnWidth.intValue();
			// category = adjustLength(category, width);
			// sb.append("[");
			// sb.append(category);
			// sb.append("] ");

			// if (category.length() > width) {
			// 	// grow in length.
			// 	nameColumnWidth.compareAndSet(width, category.length());
			// }

			sb.append(formatMessage(record));

			sb.append(System.lineSeparator());
			if (record.getThrown() != null) {
				try {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					record.getThrown().printStackTrace(pw);
					pw.close();
					sb.append(sw.toString());
				} catch (Exception ex) {}
			}

			return sb.toString();
		}

		static String adjustLength(String packageName, int aimLength) {

			int overflowWidth = packageName.length() - aimLength;

			String[] fragment = packageName.split(Pattern.quote("."));
			for (int i = 0; i < fragment.length - 1; i++) {
				if (fragment[i].length() > 1 && overflowWidth > 0) {

					int cutting = (fragment[i].length() - 1) - overflowWidth;
					cutting = (cutting < 0) ? (fragment[i].length() - 1) : overflowWidth;

					fragment[i] = fragment[i].substring(0, fragment[i].length() - cutting);
					overflowWidth -= cutting;
				}
			}

			String result = String.join(".", fragment);
			while (result.length() < aimLength) {
				result += " ";
			}

			return result;
		}
	}

	// ----- 設定値定義 -----
	static String PATH_HEARTS = "";
	static String PATH_FINE = "";
	static String PATH_MEMBERS = "";
	static String FILENAME_HEARTS = "hearts.";
	static String FILENAME_FINE = "fine.";
	static String FILENAME_MEMBERS = "members.";
	static String EXT = ".txt";

	//----- 静的フィールド -----
	static final myLogger instance = new myLogger();
	private static final DateTimeFormatter datetime_formatter = DateTimeFormatter.ofPattern("yyyyMMddHH");

	//----- インスタンスフィールド
	Logger logger_hearts = null;
	FileHandler heartsHandler = null;
	FileHandler fineHandler = null;
	Logger logger_members = null;
	FileHandler membersHandler = null;
	LocalDateTime file_datetime = LocalDateTime.now();

	/**
	 * コンストラクタ.
	 */
	private myLogger() {
		logger_hearts = Logger.getLogger("nox.hearts");
		logger_members = Logger.getLogger("nox.members");
		LogFormatter.applyToRoot();
	}

	/**
	 * ファイルハンドラリメイク.
	 */
	private void remake() {
		// 古いファイルハンドラを削除しておく
		Handler old_heartsHandler = heartsHandler;
		if (Objects.nonNull(old_heartsHandler)) {
			logger_hearts.removeHandler(old_heartsHandler);
			old_heartsHandler.flush();
			old_heartsHandler.close();
		}
		Handler old_fineHandler = fineHandler;
		if (Objects.nonNull(old_fineHandler)) {
			logger_hearts.removeHandler(old_fineHandler);
			old_fineHandler.flush();
			old_fineHandler.close();
		}
		Handler old_membersHandler = membersHandler;
		if (Objects.nonNull(old_membersHandler)) {
			logger_members.removeHandler(old_membersHandler);
			old_membersHandler.flush();
			old_membersHandler.close();
		}
		// 新しいファイル名でファイルハンドラを生成する
		// heartsログファイル名
		StringBuilder hearts_sb = new StringBuilder(200);
		hearts_sb.append(FILENAME_HEARTS);
		hearts_sb.append(datetime_formatter.format(file_datetime));
		hearts_sb.append(EXT);
		String hearts_filepath = new File(PATH_HEARTS, hearts_sb.toString()).getPath();
		// fineログファイル名
		StringBuilder fine_sb = new StringBuilder(200);
		fine_sb.append(FILENAME_FINE);
		fine_sb.append(datetime_formatter.format(file_datetime));
		fine_sb.append(EXT);
		String fine_filepath = new File(PATH_FINE, fine_sb.toString()).getPath();
		// membersログファイル名
		StringBuilder members_sb = new StringBuilder(200);
		members_sb.append(FILENAME_MEMBERS);
		members_sb.append(datetime_formatter.format(file_datetime));
		members_sb.append(EXT);
		String members_filepath = new File(PATH_MEMBERS, members_sb.toString()).getPath();
		try {
			Files.createDirectories(Paths.get(PATH_HEARTS));
			heartsHandler = new FileHandler(hearts_filepath, true);
			logger_hearts.addHandler(heartsHandler);
			heartsHandler.setLevel(Level.INFO);
			heartsHandler.setFormatter(new LogFormatter());

			Files.createDirectories(Paths.get(PATH_FINE));
			fineHandler = new FileHandler(fine_filepath, true);
			logger_hearts.addHandler(fineHandler);
			fineHandler.setLevel(Level.FINE);
			fineHandler.setFormatter(new LogFormatter());

			logger_hearts.setLevel(Level.FINE);

			Files.createDirectories(Paths.get(PATH_MEMBERS));
			membersHandler = new FileHandler(members_filepath, true);
			logger_members.addHandler(membersHandler);
			membersHandler.setLevel(Level.INFO);
			membersHandler.setFormatter(new LogFormatter());

			logger_members.setLevel(Level.FINE);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ファイルハンドラ設定.
	 * @param datetime
	 */
	public static void set_FileHandelr(LocalDateTime datetime) {
		instance.file_datetime = datetime;
		// ファイルハンドラリメイク
		instance.remake();
	}

	/**
	 * close.
	 */
	public static void close() {
		Handler old_heartsHandler = instance.heartsHandler;
		if (Objects.nonNull(old_heartsHandler)) {
			instance.logger_hearts.removeHandler(old_heartsHandler);
			old_heartsHandler.flush();
			old_heartsHandler.close();
		}
		Handler old_fineHandler = instance.fineHandler;
		if (Objects.nonNull(old_fineHandler)) {
			instance.logger_hearts.removeHandler(old_fineHandler);
			old_fineHandler.flush();
			old_fineHandler.close();
		}
		Handler old_membersHandler = instance.membersHandler;
		if (Objects.nonNull(old_membersHandler)) {
			instance.logger_members.removeHandler(old_membersHandler);
			old_membersHandler.flush();
			old_membersHandler.close();
		}
	}

	/**
	 * flush.
	 */
	public static void flush() {
		// ファイルハンドラリメイク
		instance.remake();
	}

	/**
	 * ハートファイルハンドラリセット
	 */
	private void reset_HeartsHandler() {
		// 古いファイルハンドラを削除しておく
		Handler old_heartsHandler = heartsHandler;
		if (Objects.nonNull(old_heartsHandler)) {
			logger_hearts.removeHandler(old_heartsHandler);
			old_heartsHandler.close();
		}
		// 新しいファイル名でファイルハンドラを生成する
		// heartsログファイル名
		StringBuilder hearts_sb = new StringBuilder(200);
		hearts_sb.append(FILENAME_HEARTS);
		hearts_sb.append(datetime_formatter.format(file_datetime));
		hearts_sb.append(EXT);
		String hearts_filepath = new File(PATH_HEARTS, hearts_sb.toString()).getPath();
		try {
			Files.createDirectories(Paths.get(PATH_HEARTS));
			heartsHandler = new FileHandler(hearts_filepath, true);
			logger_hearts.addHandler(heartsHandler);
			heartsHandler.setLevel(Level.INFO);
			heartsHandler.setFormatter(new LogFormatter());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static public void fine(String msg) {
		instance.logger_hearts.fine(msg);
	}

	static public void info(String msg) {
		instance.logger_hearts.info(msg);
		instance.reset_HeartsHandler();
	}

	static public void warn(String msg) {
		instance.logger_hearts.warning(msg);
		instance.reset_HeartsHandler();
	}

	static public void error(String msg) {
		instance.logger_hearts.severe(msg);
		instance.reset_HeartsHandler();
	}

	static public void error(Exception exp) {
		try (
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw)) {
			exp.printStackTrace(pw);
			String msg = sw.toString();
			instance.logger_hearts.severe(msg);
			instance.reset_HeartsHandler();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static public void info_members(String msg) {
		instance.logger_members.info(msg);
	}

	static public void warn_members(String msg) {
		instance.logger_members.warning(msg);
	}

	static public void error_members(String msg) {
		instance.logger_members.severe(msg);
	}

	static public void info_all(String msg) {
		info(msg);
		info_members(msg);
	}

	public static void config(ConfigureModel config) {
		PATH_HEARTS = config.PATH_HEARTS;
		PATH_FINE = config.PATH_FINE;
		PATH_MEMBERS = config.PATH_MEMBERS;
		FILENAME_HEARTS = config.FILENAME_HEARTS;
		FILENAME_FINE = config.FILENAME_FINE;
		FILENAME_MEMBERS = config.FILENAME_MEMBERS;
		EXT = config.EXT;
	}
}
