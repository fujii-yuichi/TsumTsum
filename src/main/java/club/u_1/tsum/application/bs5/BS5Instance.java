/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application.bs5;

import java.time.LocalDateTime;

import club.u_1.tsum.application.EmuInstanceModel;
import club.u_1.tsum.application.EmuInstanceModelInfo;
import club.u_1.tsum.application.my;
import club.u_1.tsum.application.myDateTime;

public class BS5Instance {

	// ----- 静的フィールド -----

	private static EmuInstanceModel instance = new EmuInstanceModel();
	private static int no;

	public static EmuInstanceModelInfo instances = new EmuInstanceModelInfo();

	public static String get_emu_name() {
		return instance.emu_name;
	}

	public static String get_emu_fullpath() {
		return instance.emu_fullpath;
	}

	public static String get_line_id() {
		return instance.line_id;
	}

	public static String get_line_password() {
		return instance.line_password;
	}

	public static String get_adb_host() {
		return instance.adb_host;
	}

	public static String get_mm_save_path() {
		return instance.mm_save_path;
	}

	public static String get_datetime_next() {
		return instance.datetime_next;
	}

	public static String get_datetime_last_time_last_send() {
		return instance.datetime_last_time_last_send;
	}

	public static String get_datetime_store_members_list() {
		return instance.datetime_store_members_list;
	}

	public static int getNextNo() {
		int no = 0;
		LocalDateTime next_datetime = LocalDateTime.MAX;
		String[] datetime_next = instances.instance_list.stream()
			.map(p -> p.datetime_next)
			.toArray(String[]::new);

		for (int i = 0; i < datetime_next.length; i++) {
			// 次回実行日時を復元
			LocalDateTime candidate_ldt = myDateTime.get_datetime(datetime_next[i]);
			my.println(String.format("next datetime = %1$s",
				myDateTime.formatter.format(candidate_ldt)));
			if (next_datetime.isAfter(candidate_ldt)) {
				next_datetime = candidate_ldt;
				no = i;
			}
		}

		return no;
	}

	public static int getNo() {
		return no;
	}

	public static void setNo(int value) {
		no = value % instances.size();
		instance = instances.get(no);
	}

}
