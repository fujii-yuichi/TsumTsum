/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application.bs5;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import club.u_1.tsum.application.ConfigureModel;
import club.u_1.tsum.application.my;
import club.u_1.tsum.application.myDateTime;
import club.u_1.tsum.application.myLogger;

public class ADBWrapper {

	public static String PATH_ADB = "";
	public static int RETRY_SYSTEMCALL = 3;
	public static int RETRY_GET_DATETIME = 2;

	public static void config(ConfigureModel config) {
		PATH_ADB = config.PATH_ADB;
		RETRY_SYSTEMCALL = config.RETRY_SYSTEMCALL;
		RETRY_GET_DATETIME = config.RETRY_GET_DATETIME;
	}

	/**
	 * systemcall wrapper for ADB commands.
	 * 
	 * @param commands command list
	 * @param contains result is contains
	 * @param condition true or false
	 */
	public static List<String> systemcallWrapper(String[] commands, String contains, boolean condition) {
		final String host = BS5Instance.get_adb_host();

		List<String> result = null;

		boolean needRestartServer = false;
		boolean needReconnect = false;
		boolean isOK = false;

		boolean breaked = false;
		for (int i = 0; i < RETRY_SYSTEMCALL; i++) {
			isOK = false;
			result = my.systemcall(commands);
			if (Objects.nonNull(result)) {
				for (String line : result) {
					System.out.println(line);
					if (line.contains("doesn't match this client")) {
						needRestartServer = true;
					}
					if (line.contains("error: device '" + host + "' not found")) {
						System.out.println("[debug]contains needReconnect = true");
						needReconnect = true;
					} else if ((Objects.nonNull(contains))
						&& (line.contains(contains) == condition)) {
						breaked = true;
						break;
					} else if (line.contains("ret = 0")) {
						isOK = true;
					}
				}
				if (needRestartServer) {
					// restart_server_adb();
					// my.sleep(1000);
					needRestartServer = false;
				}
				if (needReconnect) {
					System.out.println("[debug]exec Reconnect");
					String[] command2 = {
						PATH_ADB,
						"connect",
						host,
					};
					List<String> out2 = my.systemcall(command2);
					my.sleep(1000);
					needReconnect = false;
				}
			} else {
				break;
			}
			if (breaked) {
				break;
			} else if (isOK) {
				break;
			}
			my.sleep(1000);
		}

		return result;
	}

	/**
	 * ADB kill-server → start-server.
	 * <p>
	 * Nox起動後に行うこと。
	 */
	public static boolean restart_server_adb() {
		String[] command1 = {
			PATH_ADB,
			"kill-server",
		};
		List<String> out1 = my.systemcall(command1);
		String[] command2 = {
			PATH_ADB,
			"start-server",
		};
		List<String> out2 = my.systemcall(command2);
		return true;
	}

	/**
	 * ADB接続.
	 * <p>
	 * ADB操作前に必ず行うこと。
	 */
	public static boolean connect_adb() {
		final String host = BS5Instance.get_adb_host();

		// 接続済みか確認する
		String[] command1 = {
			PATH_ADB,
			"devices"
		};
		List<String> out1 = systemcallWrapper(command1, host, true);
		for (String line : out1) {
			if (line.contains(host)) {
				my.println("Already connected.");
				return true;
			}
		}
		// 未接続なら接続する
		String[] command2 = {
			PATH_ADB,
			"connect",
			host,
		};
		List<String> out2 = systemcallWrapper(command2, "unable to connect", true);
		for (String line : out2) {
			if (line.contains("unable to connect")) {
				return false;
			}
		}
		return true;
	}

	/**
	 * TsumTsum開始.
	 * <p>
	 * 事前にADB接続を確立しておくこと。
	 */
	public static void start_TsumTsum() {
		String[] command2 = {
			PATH_ADB,
			"-s",
			BS5Instance.get_adb_host(),
			"shell",
			"am",
			"start",
			"-n",
			"com.linecorp.LGTMTM/.TsumTsum",
		};
		List<String> out2 = systemcallWrapper(command2, "Starting: Intent { cmp=com.linecorp.LGTMTM/.TsumTsum }", true);
	}

	/**
	 * TsumTsum停止.
	 * <p>
	 * 事前にADB接続を確立しておくこと。
	 */
	public static void stop_TsumTsum() {
		String[] command = {
			PATH_ADB,
			"-s",
			BS5Instance.get_adb_host(),
			"shell",
			"am",
			"force-stop",
			"com.linecorp.LGTMTM",
		};
		List<String> out = systemcallWrapper(command, "ret = 1", true);
	}

	/**
	 * Settings開始.
	 * <p>
	 * 事前にADB接続を確立しておくこと。
	 */
	public static void start_Settings() {
		String[] command2 = {
			PATH_ADB,
			"-s",
			BS5Instance.get_adb_host(),
			"shell",
			"am",
			"start",
			"-a",
			"'android.settings.SETTINGS'",
		};
		List<String> out2 = systemcallWrapper(command2, "Starting: Intent { act=android.settings.SETTINGS }", true);
	}

	/**
	 * Settings停止.
	 * <p>
	 * 事前にADB接続を確立しておくこと。
	 */
	public static void stop_Settings() {
		String[] command = {
			PATH_ADB,
			"-s",
			BS5Instance.get_adb_host(),
			"shell",
			"am",
			"force-stop",
			"com.android.settings",
		};
		List<String> out = systemcallWrapper(command, null, true);
	}

	/**
	 * Google Chrome開始.
	 * <p>
	 * 事前にADB接続を確立しておくこと。
	 */
	public static void start_GoogleChrome() {
		String[] command2 = {
			PATH_ADB,
			"-s",
			BS5Instance.get_adb_host(),
			"shell",
			"am",
			"start",
			"-n",
			"com.android.chrome/com.google.android.apps.chrome.Main",
		};
		List<String> out2 = systemcallWrapper(command2, "Starting: Intent { cmp=com.android.chrome/com.google.android.apps.chrome.Main }", true);
	}

	/**
	 * Google Chrome停止.
	 * <p>
	 * 事前にADB接続を確立しておくこと。
	 */
	public static void stop_GoogleChrome() {
		String[] command = {
			PATH_ADB,
			"-s",
			BS5Instance.get_adb_host(),
			"shell",
			"am",
			"force-stop",
			"com.android.chrome",
		};
		List<String> out = systemcallWrapper(command, "ret = 1", true);
	}

	/**
	 * キーイベント送信：ホームキー.
	 * <p>
	 * 事前にADB接続を確立しておくこと。
	 */
	public static void sendkey_home() {
		String[] command = {
			PATH_ADB,
			"-s",
			BS5Instance.get_adb_host(),
			"shell",
			"input",
			"keyevent",
			"3",
		};
		List<String> out = systemcallWrapper(command, null, true);
	}

	/**
	 * キーイベント送信：AppSwitcherキー.
	 * <p>
	 * 事前にADB接続を確立しておくこと。
	 */
	public static void sendkey_switcher() {
		String[] command = {
			PATH_ADB,
			"-s",
			BS5Instance.get_adb_host(),
			"shell",
			"input",
			"keyevent",
			"187",
		};
		List<String> out = systemcallWrapper(command, null, true);
	}

	public static LocalDateTime get_datetime() {
		for (int i = 0; i < RETRY_GET_DATETIME; i++) {
			try {
				// final String[] command1 = {
				// 	C_PATH_ADB,
				// 	"-s",
				// 	C_HOST,
				// 	"shell",
				// 	"su",
				// 	"-c",
				// 	"date",
				// };
				// List<String> out1 = systemcallWrapper(command1, null, true);
				String[] command2 = {
					PATH_ADB,
					"-s",
					BS5Instance.get_adb_host(),
					"shell",
					"echo",
					"$(date '+%Y/%m/%d %T')${EPOCHREALTIME:10:4}",
				};
				List<String> out2 = systemcallWrapper(command2, null, true);

				LocalDateTime ldt = LocalDateTime.parse(out2.get(0), myDateTime.formatter);
				return ldt;
			} catch (Exception e) {
				// スタックトレース出力
				e.printStackTrace();
				// 待ち
				my.sleep(1000);
			}
		}
		myLogger.warn("adb shell date failed");
		return null;
	}

	public static void set_date(LocalDateTime datetime) {
		LocalDateTime ldt = datetime;

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddHHmmyyyy.ss");
		String[] command = {
			PATH_ADB,
			"-s",
			BS5Instance.get_adb_host(),
			"shell",
			"su",
			"-c",
			String.format("'date %s'", formatter.format(ldt)),
		};
		List<String> out = systemcallWrapper(command, null, true);
	}

}
