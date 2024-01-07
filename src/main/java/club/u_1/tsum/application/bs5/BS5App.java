/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application.bs5;

import java.util.List;
import java.util.Objects;

import org.sikuli.script.App;
import org.sikuli.script.Location;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;

import club.u_1.tsum.application.ConfigureModel;
import club.u_1.tsum.application.my;

public class BS5App {

	// ----- 設定値定義 -----
	public static String APP_PROCESS_NAME = "HD-Player";
	public static String PATH_PS1_SET_WINDOW = "";
	public static String PATH_PS1_GET_WINDOW = "";

	// ----- 静的フィールド -----
	public static App instance = null;

	public static App get() {
		if (Objects.isNull(instance)) {
			instance = new App(BS5Instance.get_emu_name());
		} else {
			if (!BS5Instance.get_emu_name().equals(instance.getWindow())) {
				instance = new App(BS5Instance.get_emu_name());
			}
		}
		return instance;
	}

	/**
	 * 稼動判定.
	 * @param maxTime 最大待機時間
	 * @return 
	 */
	public static boolean is_running(int maxTime) {
		return get().isRunning(maxTime);
	}

	/**
	 * BSオープン.
	 */
	public static void open() {
		// App instance = new App(BS5Instance.get_emu_fullpath());
		// instance.open();
		instance = App.open(BS5Instance.get_emu_fullpath());
	}

	/**
	 * ウィンドウ移動.
	 * @param x X座標
	 * @param y Y座標
	 * @return ウィンドウのリージョン
	 */
	public static Region move_window(int x, int y) {
		my.move_window(BS5Instance.get_emu_name(), x, y);
		my.sleep(10);
		return App.focusedWindow();
	}

	/**
	 * ウィンドウリサイズ.
	 * @param width 幅
	 * @param height 高さ
	 * @return ウィンドウのリージョン
	 */
	public static Region resize_window(int width, int height) {
		return my.resize_window(BS5Instance.get_emu_name(), width, height);
	}

	/**
	 * ウィンドウ移動&リサイズ.
	 * @param x X座標
	 * @param y Y座標
	 * @param width 幅
	 * @param height 高さ
	 * @return ウィンドウのリージョン
	 */
	public static Region move_resize_window(int x, int y, int width, int height) {
		App target = new App(BS5Instance.get_emu_name());
		Region reg = null;

		// コマンドを定義
		String setWindow = "Set-Window "
			+ "-ProcessName " + APP_PROCESS_NAME + " "
			+ "-X " + x + " "
			+ "-Y " + y + " "
			+ "-Width " + width + " "
			+ "-Height " + height + " "
			+ "-Passthr";
		String[] command = {
			"powershell",
			"-ExecutionPolicy",
			"RemoteSigned",
			"-Command",
			". " + PATH_PS1_SET_WINDOW + "; " + setWindow,
		};
		List<String> out = null;

		if (target.isRunning(1)) {
			// コマンド実行
			out = my.systemcall(command);

			// ウィンドウをフォーカス
			App.focus(BS5Instance.get_emu_name());
			my.sleep(100);
			reg = App.focusedWindow();

			// 右端ウィンドウハンドラをちょこんと触る
			int window_handler_x = reg.getBottomRight().x;
			int window_handler_y = reg.getBottomRight().y - (reg.h / 4);
			my.single_tap_by_Location(new Location(window_handler_x, window_handler_y));

			// コマンド実行
			out = my.systemcall(command);

			// ウィンドウをフォーカス
			App.focus(BS5Instance.get_emu_name());
			my.sleep(10);
			reg = App.focusedWindow();
		}
		return reg;
	}

	/**
	 * フォーカスセット.
	 * @return フォーカスセットしたウィンドウのリージョン
	 */
	public static Region focus() {
		// my.move_window(BS5Instance.get_emu_name(), 0, 0);
		// my.sleep(10);
		// return App.focusedWindow();
		Region reg = Screen.getPrimaryScreen();
		App instance = get();

		if (instance.isRunning(2)) {
			// ウィンドウをフォーカス
			App.focus(BS5Instance.get_emu_name());
			my.sleep(100);
			reg = App.focusedWindow();
		}

		return reg;
	}

	public static void config(ConfigureModel config) {
		APP_PROCESS_NAME = config.APP_PROCESS_NAME;
		PATH_PS1_SET_WINDOW = config.PATH_PS1_SET_WINDOW;
		PATH_PS1_GET_WINDOW = config.PATH_PS1_GET_WINDOW;
	}

}
