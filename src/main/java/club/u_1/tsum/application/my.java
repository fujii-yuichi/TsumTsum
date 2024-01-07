/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application;

import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.sikuli.script.App;
import org.sikuli.script.Location;
import org.sikuli.script.Region;
import org.sikuli.script.RunTime;

public class my {
	// ----- 定数定義 -----
	static final int WAIT_AFTER_RELEASE = 30;

	static final int DELAY_TAP = 10;
	static final int DELAY_LONG_TAP = 1000;

	static final int DELAY_SWIPE_MOVE_ATTACK = 2;
	static final int DELAY_SWIPE_MOVE_DECAY = 1;
	static final int DELAY_SWIPE_DURING_MOVE = 18;
	static final int DELAY_SWIPE_BEFORE_RELEASE = 32;
	static final int DELAY_SWIPE_DURING_MOVE_FOR_PLAY = 40;
	static final int DIVISION = 4;

	// ----- 静的フィールド -----
	public static Robot r = null;
	public static boolean is_shutdown = false;
	public static boolean is_windows = false;
	public static String location;

	/**
	 * static初期化子.
	 */
	static {
		is_shutdown = false;
		try {
			r = new Robot();
			println("SikuliVersion is " + RunTime.get().getVersion());
		} catch (Exception e) {
			e.printStackTrace();
		}
		is_windows = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
		URL url = my.class.getProtectionDomain().getCodeSource().getLocation();
		location = new File(url.getPath()).getAbsolutePath();
	}

	/**
	 * exit.
	 * @param status 終了コード
	 */
	public static void exit(int status) {
		is_shutdown = true;
		waitForIdle(0);
		System.exit(status);
	}

	/**
	 * sleep.
	 * @param millis スリープ時間(ミリ秒)
	 */
	public static void sleep(int millis) {
		if (0 < millis) {
			r.delay(millis);
		}
	}

	/**
	 * waitForIdle with sleep.
	 * @param millis スリープ時間(ミリ秒)
	 */
	public static void waitForIdle(int millis) {
		sleep(millis);
		r.waitForIdle();
	}

	/**
	 * 日時付きSystem.out.println.
	 * @param msg 出力メッセージ
	 */
	public static void println(String msg) {
		LocalDateTime ldt = LocalDateTime.now();
		System.out.println(myDateTime.formatter.format(ldt) + ' ' + msg);
		// myLogger.fine(msg);
	}

	/**
	 * 次の正時を求める
	 * @param datetime 対象日時
	 * @return
	 */
	public static LocalDateTime next_hour_datetime(LocalDateTime datetime) {
		LocalDateTime next = datetime.plusHours(1).truncatedTo(ChronoUnit.HOURS);
		return next;
	}

	private static void take_screen_for_extended_delay() {
		mySS.take_screen();
	}

	/**
	 * 自前シングルタップ by Location.
	 * @param loc
	 */
	public static void single_tap_by_Location(Location loc) {
		try {
			long start_time = System.nanoTime();
			r.waitForIdle();
			r.mouseMove(loc.x, loc.y);
			r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			sleep(DELAY_TAP);
			r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			waitForIdle(WAIT_AFTER_RELEASE);
			long elapsed_time = (System.nanoTime() - start_time) / 1000000;
			// println("SingleTap on " + loc.toString() + " (" + elapsed_time + " msec)");
			if (elapsed_time > 2000) {
				myLogger.warn("★ [single_tap] extended delay (" + elapsed_time + " msec)");
				take_screen_for_extended_delay();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 自前ロングタップ by Location.
	 * @param loc
	 */
	public static void long_tap_by_Location(Location loc) {
		try {
			long start_time = System.nanoTime();
			r.waitForIdle();
			r.mouseMove(loc.x, loc.y);
			r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			sleep(DELAY_LONG_TAP);
			r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			waitForIdle(WAIT_AFTER_RELEASE);
			long elapsed_time = (System.nanoTime() - start_time) / 1000000;
			println("LongTap on " + loc.toString() + " (" + elapsed_time + " msec)");
			if (elapsed_time > 2000) {
				// TODO:set_extended_delay
				// myStatus.set_extended_delay(true);
				myLogger.warn("★ [long_tap] extended delay (" + elapsed_time + " msec)");
				take_screen_for_extended_delay();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 自前スワイプ with 中間点 by Location.
	 * @param start_loc
	 * @param middle_loc
	 * @param end_loc
	 */
	public static void swipe_with_middle_by_Location(
		Location start_loc,
		Location middle_loc,
		Location end_loc) {
		try {
			long start_time = System.nanoTime();
			r.waitForIdle();
			r.mouseMove(start_loc.x, start_loc.y);
			r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			sleep(DELAY_TAP);
			r.mouseMove(middle_loc.x, middle_loc.y);
			sleep(DELAY_SWIPE_DURING_MOVE);
			r.mouseMove(end_loc.x, end_loc.y);
			sleep(DELAY_SWIPE_DURING_MOVE);
			r.mouseMove(middle_loc.x, middle_loc.y);
			sleep(DELAY_SWIPE_BEFORE_RELEASE);
			r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			waitForIdle(WAIT_AFTER_RELEASE);
			long elapsed_time = (System.nanoTime() - start_time) / 1000000;
			// println("Swipe on " + start_loc.toString() + " to " + end_loc.toString() + " (" + elapsed_time + " msec)");
			if (elapsed_time > 2000) {
				// TODO:set_extended_delay
				// myStatus.set_extended_delay(true);
				myLogger.warn("★ [swipe] extended delay (" + elapsed_time + " msec)");
				take_screen_for_extended_delay();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 自前スワイプ with 中間点 by Location To 細かく移動.
	 * @param start_loc
	 * @param middle_loc
	 * @param end_loc
	 */
	public static void swipe_with_middle_by_Location_to_dividely(
		Location start_loc,
		Location middle_loc,
		Location end_loc) {
		try {
			long start_time = System.nanoTime();
			int y_divided = (int) ((middle_loc.y - start_loc.y) / DIVISION);
			r.waitForIdle();
			r.mouseMove(start_loc.x, start_loc.y);
			r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			final int y_moving[] = new int[1];
			IntStream.range(1, DIVISION).forEachOrdered(i -> {
				y_moving[0] += y_divided;
				r.mouseMove(start_loc.x, start_loc.y + y_moving[0]);
				sleep(DELAY_SWIPE_MOVE_ATTACK + (i * DELAY_SWIPE_MOVE_DECAY));
			});
			r.mouseMove(middle_loc.x, middle_loc.y);
			sleep(DELAY_SWIPE_DURING_MOVE);
			r.mouseMove(end_loc.x, end_loc.y);
			sleep(DELAY_SWIPE_DURING_MOVE);
			r.mouseMove(middle_loc.x, middle_loc.y);
			sleep(DELAY_SWIPE_BEFORE_RELEASE);
			r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			waitForIdle(WAIT_AFTER_RELEASE);
			long elapsed_time = (System.nanoTime() - start_time) / 1000000;
			// println("Swipe on " + start_loc.toString() + " to " + end_loc.toString() + " (" + elapsed_time + " msec)");
			if (elapsed_time > 2000) {
				// TODO:set_extended_delay
				// myStatus.set_extended_delay(true);
				myLogger.warn("★ [swipe] extended delay (" + elapsed_time + " msec)");
				take_screen_for_extended_delay();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 自前スワイプ with 中間点 by Location.
	 * @param start_loc
	 * @param middle_loc
	 * @param end_loc
	 */
	public static void swipe_by_Locations_for_play_tsum(
		List<Location> locs) {
		if (locs.size() == 0) {
			return;
		}
		try {
			long start_time = System.nanoTime();
			Location start_loc = locs.get(0);
			r.waitForIdle();
			r.mouseMove(start_loc.x, start_loc.y);
			r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			for (Location location : locs) {
				sleep(DELAY_SWIPE_DURING_MOVE_FOR_PLAY);
				r.mouseMove(location.x, location.y);
			}
			sleep(DELAY_SWIPE_BEFORE_RELEASE);
			r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			waitForIdle(WAIT_AFTER_RELEASE);
			long elapsed_time = (System.nanoTime() - start_time) / 1000000;
			println(String.format("Swipe %1$s", Arrays.toString(locs.toArray())));
			// myLogger.fine(String.format("Swipe %1$s", Arrays.toString(locs.toArray())));
			if (elapsed_time > 5000) {
				// TODO:set_extended_delay
				// myStatus.set_extended_delay(true);
				myLogger.warn("★ [swipe] extended delay (" + elapsed_time + " msec)");
				take_screen_for_extended_delay();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void mouse_move(Location loc) {
		try {
			long start_time = System.nanoTime();
			Location start_loc = new Location(MouseInfo.getPointerInfo().getLocation());
			Location end_loc = start_loc.offset(loc);
			r.waitForIdle();
			r.mouseMove(end_loc.x, end_loc.y);
			sleep(DELAY_SWIPE_BEFORE_RELEASE);
			r.mouseMove(start_loc.x, start_loc.y);
			waitForIdle(WAIT_AFTER_RELEASE);
			long elapsed_time = (System.nanoTime() - start_time) / 1000000;
			// println("mouse move on " + start_loc.toString() + " to " + end_loc.toString() + " (" + elapsed_time + " msec)");
			if (elapsed_time > 2000) {
				// TODO:set_extended_delay
				// myStatus.set_extended_delay(true);
				myLogger.warn("★ [mouse move] extended delay (" + elapsed_time + " msec)");
				take_screen_for_extended_delay();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ウィンドウ移動.
	 * @param application アプリケーション名
	 * @param x X座標
	 * @param y Y座標
	 */
	public static void move_window(String application, int x, int y) {
		App target = new App(application);
		Region reg = null;

		if (target.isRunning(1)) {
			// ウィンドウをフォーカス
			target.focus();
			my.sleep(10);
			reg = App.focusedWindow();
			// ウィンドウハンドラ座標
			int window_handler_x = reg.getTopLeft().x + 11;
			int window_handler_y = reg.getTopLeft().y + 11;
			// ウィンドウ移動座標
			int moved_x = x + 11;
			int moved_y = y + 11;
			// ウィンドウドラッグ開始
			try {
				long start_time = System.nanoTime();
				r.waitForIdle();
				r.mouseMove(window_handler_x, window_handler_y);
				r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				sleep(DELAY_LONG_TAP);
				sleep(DELAY_SWIPE_DURING_MOVE);
				r.mouseMove(moved_x, moved_y);
				sleep(DELAY_SWIPE_BEFORE_RELEASE);
				r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
				waitForIdle(WAIT_AFTER_RELEASE);
				long elapsed_time = (System.nanoTime() - start_time) / 1000000;
				println("drag and drop (" + elapsed_time + " msec)");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ウィンドウ移動.
	 * @param application アプリケーション名
	 * @param w 幅
	 * @param h 高さ
	 */
	public static Region resize_window(String application, int w, int h) {
		App target = new App(application);
		Region reg = null;

		if (target.isRunning(2)) {
			int adjustedH = h;
			while (true) {
				// ウィンドウをフォーカス
				target.focus();
				my.sleep(50);
				reg = App.focusedWindow();
				if (reg.h == h) {
					break;
				}
				// 調整
				if (reg.h > h) {
					adjustedH--;
				} else {
					adjustedH++;
				}
				// ウィンドウハンドラ座標
				int window_handler_x = reg.getBottomRight().x - (reg.w / 2);
				int window_handler_y = reg.getBottomRight().y;
				// ウィンドウ移動座標
				int moved_x = reg.getCenter().x;
				int moved_y = reg.getTopLeft().y + adjustedH;
				// ウィンドウドラッグ開始
				try {
					long start_time = System.nanoTime();
					r.waitForIdle();
					r.mouseMove(window_handler_x, window_handler_y);
					r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
					sleep(DELAY_LONG_TAP);
					sleep(DELAY_SWIPE_DURING_MOVE);
					r.mouseMove(moved_x, moved_y);
					sleep(DELAY_SWIPE_BEFORE_RELEASE);
					r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
					waitForIdle(WAIT_AFTER_RELEASE);
					long elapsed_time = (System.nanoTime() - start_time) / 1000000;
					println("resize (" + elapsed_time + " msec)");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		myLogger.fine(String.format("%3$s w=%1$d, h=%2$d", reg.w, reg.h, application));
		return reg;
	}

	/**
	 * new App.
	 * @param application アプリケーション名
	 * @return the App instance
	 */
	public static App new_App(String application) {
		return new App(application);
	}

	/**
	 * is Running.
	 * @param application アプリケーション名
	 * @return running or not running
	 */
	public static boolean is_running_App(String application) {
		return new_App(application).isRunning(1);
	}

	/**
	 * 次回ランキング終了日時を算出する.
	 * @param now 現在日時
	 * @return 次回ランキング終了日時
	 */
	public static LocalDateTime get_counting_date(LocalDateTime now) {
		// 現在時刻よりランキング終了日を算出する (直近の次の月曜日)
		LocalDateTime next_start = now
			.truncatedTo(ChronoUnit.DAYS)
			.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
		return next_start;
	}

	/**
	 * ファイル移動.
	 * @param source 移動元ファイルへのパス
	 * @param target_dir 移動先ディレクトリへのパス
	 * @return 移動先ファイルへのパス
	 */
	public static Path move(Path source, Path target_dir) {
		Path filename = source.getFileName();
		Path target = target_dir.resolve(filename);
		Path result = null;
		try {
			result = Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
			// my.println(String.format("move %1$s to %2$s", source.toString(), target.toString()));
			// my.println(String.format("    result = %1$s", result.toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * ファイルコピー.
	 * @param source コピー元ファイルへのパス
	 * @param target_dir コピー先ディレクトリへのパス
	 * @return コピー先ファイルへのパス
	 */
	public static Path copy(Path source, Path target_dir) {
		Path filename = source.getFileName();
		Path target = target_dir.resolve(filename);
		Path result = null;
		try {
			result = Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
			// my.println(String.format("copy %1$s to %2$s", source.toString(), target.toString()));
			// my.println(String.format("    result = %1$s", result.toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * システムリブート.
	 */
	public static void reboot_system() {
		List<String> out = null;
		String[] command1 = {
			"shutdown",
			"-r",
			"-f",
			"-t",
			"0",
		};
		out = my.systemcall(command1);
	}

	/**
	 * システムコール.
	 * @param command コマンド配列
	 * @return 標準出力
	 */
	public static List<String> systemcall(String[] command) {
		String charset = Charset.defaultCharset().name();
		return systemcall(command, charset);
	}

	/**
	 * システムコール.
	 * <p>
	 * 参考：https://miyakoroom.blogspot.com/2018/09/java-processbuilder_4.html
	 * 参考：https://www.ne.jp/asahi/hishidama/home/tech/java/process.html
	 * @param command コマンド配列
	 * @param charset サポートされている{@link java.nio.charset.Charset </code>charset<code>}名称
	 * @return 標準出力
	 */
	public static List<String> systemcall(String[] command, String charset) {

		// タイムアウト用タイマー : 内部クラス
		class CmdTimer extends TimerTask {

			private boolean is_timeout = false;
			private Process p = null;

			public CmdTimer(Process p) {
				this.p = p;
			}

			public boolean isTimeOut() {
				return this.is_timeout;
			}

			@Override
			public void run() {
				this.is_timeout = true;
				try {
					p.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		Timer timer = new Timer();

		// 標準出力受け取り用の配列。
		List<String> resultStr = new ArrayList<String>();
		// resultStr.add("Command start time [" + LocalDateTime.now().toString() + "]");
		// resultStr.add("========================================");
		// resultStr.add("");

		// ■準備 : タイムアウト間隔の格納(例 : 5000ms = 5s)
		long timeout = 10000;

		// ■準備
		ProcessBuilder pb = new ProcessBuilder(command);
		my.println(String.join(" ", command));
		// 標準出力と標準エラー出力を同一視
		pb.redirectErrorStream(true);

		// ■準備2
		Process process = null;
		// InputStreamReader isr = null;
		// BufferedReader reader = null;
		InputStreamThread it = null;
		// InputStreamThread et = null;
		boolean is_timeout = false;
		int ret = -1;

		try {
			// プロセススタート
			process = pb.start();

			// タイマー設定
			CmdTimer task = new CmdTimer(process);
			timer.schedule((TimerTask) task, timeout);

			// InputStreamのスレッド開始
			it = new InputStreamThread(process.getInputStream(), charset);
			// et = new InputStreamThread(process.getErrorStream());
			it.start();
			// et.start();

			// // 標準出力受取用のストリーム取得
			// isr = new InputStreamReader(process.getInputStream(), "UTF-8");
			// reader = new BufferedReader(isr);

			// 標準出力受取用ループ。
			// ストリーム内で文字列が4Kバイト？を超えるとプロセスが停止するため、定期的にストリームから抜き出しておく。
			// 抜き取り間隔は下のThread.sleepで指定。ここが遅いと、抜き取る前にたくさん貯まるので、値を小さくしたりすること。
			while (true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}

				// サブプロセス終了、もしくはタイム・アウトしている場合、受取用ループはお役御免なので終了する。
				if (!process.isAlive() || task.isTimeOut()) {
					is_timeout = task.isTimeOut();
					if (is_timeout) {
						// 割り込み
						try {
							it.interrupt();
							// et.interrupt();
							myLogger.warn("InputStreamのスレッド割り込み");
							// 1秒終了を待つ
							process.waitFor(1, TimeUnit.SECONDS);
						} catch (Exception e) {
							myLogger.error("InputStreamのスレッド割り込み：例外発生");
							e.printStackTrace();
						}
						// サブプロセスが生存している場合
						if (process.isAlive()) {
							// 強制終了
							try {
								process.destroy();
								myLogger.error("サブプロセス強制終了");
							} catch (Exception e2) {
								myLogger.error("サブプロセス強制終了：例外発生");
								e2.printStackTrace();
							}
						}
					}
					// サブプロセスの終了値取得
					try {
						process.waitFor(1, TimeUnit.SECONDS);
						ret = process.exitValue();
					} catch (IllegalThreadStateException e) {
						e.printStackTrace();
					}
					break;
				}

				// // 標準出力のバッファが貯まるとprocessBuilderが停止するので、抜き出す。
				// if (reader.ready()) {
				// 	String line = reader.readLine();
				// 	resultStr.add(line);
				// 	println(line);
				// }
			}
		} catch (Exception e) {
			// たとえば sh が見つからない時などはここに来る。
			// resultStr.add("コマンド処理中にエラーが発生しました。");
			// resultStr.add(e.getMessage());
			println("IOException:");
			e.printStackTrace();
		} finally {
			// タイムアウトかどうか。
			if (is_timeout) {
				// タイムアウトで終わった場合 : 後続の標準出力は無いので、抜ける。
				myLogger.warn("systemcall timeout");
				resultStr.add("コマンドがタイムアウトしました。");
				resultStr.add("現在のタイムアウト間隔 : " + timeout + "(ms)");
			} else {
				// タイムアウト以外（正常終了）

				// // エラー判定 : リーダーがnullという状況は異常で、上部のcatchを経由してきた状況なのでそのまま返却。
				// if (reader == null) {
				// 	// 返却
				// 	timer.cancel();
				// 	return resultStr;
				// }
				// エラー判定 : リーダーがnullという状況は異常で、上部のcatchを経由してきた状況なのでそのまま返却。
				if (it.getState() == Thread.State.TERMINATED) {
					// 返却
					timer.cancel();
					// resultStr.addAll(it.getStringList());
					// resultStr.addAll(et.getStringList());
					// // return resultStr;
				}
				// if (et.getState() == Thread.State.TERMINATED) {
				// 	// 返却
				// 	timer.cancel();
				// 	resultStr.addAll(it.getStringList());
				// 	resultStr.addAll(et.getStringList());
				// // 	return resultStr;
				// }

				// 標準出力結果を取得。
				// // ここまでループ内で細かく取得しており、最後に1回抜き取る。
				// while (true) {
				// 	try {
				// 		if (reader.ready())
				// 		{
				// 			String line = reader.readLine();
				// 			if ((line == null) || (line.isEmpty())) {
				// 				break;
				// 			} else {
				// 				resultStr.add(line);
				// 				println(line);
				// 			}
				// 		}
				// 	} catch (IOException e) {
				// 		println("IOException:");
				// 		e.printStackTrace();
				// 	}
				// }

				// // 結果用文言の付与 : 0なら正常終了、それ以外は異常終了ってことで。sh次第だけど、いちおう一般的。
				// resultStr.add("");
				// if (process.exitValue() == 0) {
				// 	// 0 : 正常終了
				// 	resultStr.add("コマンドが正常終了しました。");
				// } else {
				// 	// !0 : 異常終了
				// 	resultStr.add("コマンドが異常終了しました。");
				// }
				println(String.format("ret = %1$d", ret));
			}

			// // 各種close
			// try {
			// 	reader.close();
			// } catch (IOException e) {
			// }
			// try {
			// 	isr.close();
			// } catch (IOException e) {
			// }

			//InputStreamのスレッド終了待ち
			try {
				it.join();
				// et.join();
				my.println("it.join();");
				resultStr.addAll(it.getStringList());
				// resultStr.addAll(et.getStringList());
				resultStr.add(String.format("ret = %1$d", ret));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// try {
		// 	//InputStreamのスレッド終了待ち
		// 	it.join();
		// 	// et.join();
		// } catch (Exception e) {
		// 	e.printStackTrace();
		// }

		// 返却
		timer.cancel();
		return resultStr;
	}

	/**
	 * [未使用] システムコール.
	 * @param command
	 */
	public static ArrayList<String> systemcall_0(String[] command) {
		// // ランタイムオブジェクトを取得する
		// Runtime runtime = Runtime.getRuntime();
		// try {
		// 	// 指定したコマンドを実行する
		// 	return runtime.exec(command);
		// } catch (IOException e) {
		// 	e.printStackTrace();
		// }
		// return null;
		ArrayList<String> out = new ArrayList<>();
		BufferedReader br = null;
		// 起動するコマンド、引数でProcessBuilderを作る。
		ProcessBuilder pb = new ProcessBuilder(command);
		// 実行するプロセスの標準エラー出力を標準出力に混ぜる。(標準エラー出力を標準入力から入力できるようになる)
		pb.redirectErrorStream(true);
		try {
			// プロセス起動
			Process process = pb.start();

			// 起動したプロセスの標準出力を取得して表示する。
			//   標準出力やエラー出力が必要なくても読んどかないとバッファがいっぱいになって
			//   プロセスが止まる(一時停止)してしまう場合がある。
			InputStream is = process.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			while (true) {
				String line = br.readLine();
				if (line == null) {
					break;
				}
				out.add(line);
				println(line);
			}
			// プロセスの終了を待つ。
			int ret = process.waitFor();
			// 終了コードを表示
			println("ret = " + ret);
		} catch (IOException ex) {
			println("IOException:");
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			println("InterruptedException:");
			ex.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException ex) {
					println("IOException:");
					ex.printStackTrace();
				}
			}
		}
		return out;
	}
}
