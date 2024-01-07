/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmuInstanceModel implements Serializable {
	public String emu_name;
	public String emu_fullpath;
	public String line_id;
	public String line_password;
	public String adb_host;
	public String mm_save_path;
	public String datetime_next;
	public String datetime_last_time_last_send;
	public String datetime_store_members_list;

	/**
	 * インスタンス生成.
	 * @param emu_name
	 * @param emu_fullpath
	 * @param line_id
	 * @param line_password
	 * @param adb_host
	 * @param mm_save_path
	 * @param datetime_next
	 * @param datetime_last_time_last_send
	 * @param datetime_store_members_list
	 * @return 生成したインスタンス
	 */
	public static EmuInstanceModel of(
		String emu_name,
		String emu_fullpath,
		String line_id,
		String line_password,
		String adb_host,
		String mm_save_path,
		String datetime_next,
		String datetime_last_time_last_send,
		String datetime_store_members_list) {
		EmuInstanceModel instance = new EmuInstanceModel();
		instance.emu_name = emu_name;
		instance.emu_fullpath = emu_fullpath;
		instance.line_id = line_id;
		instance.line_password = line_password;
		instance.adb_host = adb_host;
		instance.mm_save_path = mm_save_path;
		instance.datetime_next = datetime_next;
		instance.datetime_last_time_last_send = datetime_last_time_last_send;
		instance.datetime_store_members_list = datetime_store_members_list;
		return instance;
	}

	// TODO:前回送信日時などの記憶先
	// TODO:あとなにがある？？？
}
