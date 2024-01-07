/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application.bs5;

import org.sikuli.script.Pattern;

/**
 * Pattern定義.
 */
public class BS5Patterns {

	// ----- 静的フィールド -----

	/* ランチャー画面 */
	public static final Pattern tsum_app_icon_portrait_088 = new Pattern("/bs5/tsum_app_icon_portrait.png").similar(0.88f);

	/* LINEログイン関連 */
	public static final Pattern LINE_GAME_088 = new Pattern("/bs5/LINE_GAME.png").similar(0.88f);
	public static final Pattern consent_dialog_accept_088 = new Pattern("/bs5/consent_dialog_accept.png").similar(0.88f);
	public static final Pattern consent_checkbox_094 = new Pattern("/bs5/consent_checkbox.png").similar(0.94f).targetOffset(58, 0);;
	public static final Pattern LINE_088 = new Pattern("/bs5/LINE.png").similar(0.88f);
	public static final Pattern textbox_mailaddress_088 = new Pattern("/bs5/textbox_mailaddress.png").similar(0.88f);
	public static final Pattern textbox_password_088 = new Pattern("/bs5/textbox_password.png").similar(0.88f);
	public static final Pattern menu_select_all_088 = new Pattern("/bs5/menu_select_all.png").similar(0.88f);
	public static final Pattern menu_paste_088 = new Pattern("/bs5/menu_paste.png").similar(0.88f);
	public static final Pattern login_094 = new Pattern("/bs5/login.png").similar(0.94f);
	public static final Pattern accept_088 = new Pattern("/bs5/accept.png").similar(0.88f);
	public static final Pattern login_reCAPTCHA_088 = new Pattern("/bs5/LINE_login_reCAPTCHA.png").similar(0.88f);

	/* 黒通知関連 */
	public static final Pattern root_exist_088 = new Pattern("/bs5/root_exist.png").similar(0.88f).targetOffset(90, 0);
	public static final Pattern notice_popup_2buttons_088 = new Pattern("/bs5/notice_popup_2buttons.png").similar(0.88f).targetOffset(53, 0);
	public static final Pattern notice_popup_3buttons_088 = new Pattern("/bs5/notice_popup_3buttons.png").similar(0.88f).targetOffset(79, 0);

	/* オープニング */
	public static final Pattern tap_to_start_088 = new Pattern("/bs5/TAP_TO_START.png").similar(0.88f);
	public static final Pattern blackout_094 = new Pattern("/bs5/blackout.png").similar(0.94f);
	public static final Pattern blackout2_094 = new Pattern("/bs5/blackout2.png").similar(0.94f);

	/* ツムツム汎用 */
	public static final Pattern ok_090 = new Pattern("/bs5/ok.png").similar(0.90f);
	public static final Pattern cancel_088 = new Pattern("/bs5/cancel.png").similar(0.88f);
	public static final Pattern colorful_tsum_088 = new Pattern("/bs5/colorful_tsum.png").similar(0.88f);
	public static final Pattern retry_090 = new Pattern("/bs5/retry.png").similar(0.90f);
	public static final Pattern close_088 = new Pattern("/bs5/close.png").similar(0.88f);
	public static final Pattern close_small_088 = new Pattern("/bs5/close_small.png").similar(0.88f);
	public static final Pattern turn_back_088 = new Pattern("/bs5/turn_back.png").similar(0.88f);
	public static final Pattern not_accept_push_088 = new Pattern("/bs5/not_accept_push.png").similar(0.88f);

	/* ホーム画面 */
	public static final Pattern title_home_094 = new Pattern("/bs5/title_home.png").similar(0.94f);

	/* ランキング画面 */
	public static final Pattern title_weekly_ranking_094 = new Pattern("/bs5/title_weekly_ranking.png").similar(0.94f);
	public static final Pattern play_090 = new Pattern("/bs5/play.png").similar(0.90f);
	public static final Pattern my_play_history_088 = new Pattern("/bs5/my_play_history.png").similar(0.88f);
	public static final Pattern separator_line_087 = new Pattern("/bs5/separator_line.png").similar(0.87f);
	public static final Pattern invitation_085 = new Pattern("/bs5/invitation.png").similar(0.85f);
	public static final Pattern heart_full_093 = new Pattern("/bs5/heart_full.png").similar(0.93f);
	public static final Pattern high_score_so_far_090 = new Pattern("/bs5/high_score_so_far.png").similar(0.90f);
	public static final Pattern ranking_094 = new Pattern("/bs5/ranking.png").similar(0.94f);

	/* メールボックス画面 */
	public static final Pattern title_mailbox_088 = new Pattern("/bs5/title_mailbox.png").similar(0.88f);
	public static final Pattern collectively_receive_090 = new Pattern("/bs5/collectively_receive.png").similar(0.90f);

	/* アイテムセット画面 */
	public static final Pattern start_090 = new Pattern("/bs5/start.png").similar(0.90f);

	/* プレイ画面 */
	public static final Pattern bs5_pause_on_090 = new Pattern("/bs5/bs5_pause_on.png").similar(0.90f);
	public static final Pattern bs5_count0_090 = new Pattern("/bs5/bs5_count0.png").similar(0.90f);
	public static final Pattern bs5_score_090 = new Pattern("/bs5/bs5_score.png").similar(0.90f);
	public static final Pattern bs5_coin_090 = new Pattern("/bs5/bs5_coin.png").similar(0.90f);
	public static final Pattern bs5_exp_090 = new Pattern("/bs5/bs5_exp.png").similar(0.90f);
	public static final Pattern bs5_time_090 = new Pattern("/bs5/bs5_time.png").similar(0.90f);
	public static final Pattern bs5_bomb_090 = new Pattern("/bs5/bs5_bomb.png").similar(0.90f);
	public static final Pattern bs5_5_4_090 = new Pattern("/bs5/bs5_5_4.png").similar(0.90f);
	public static final Pattern bs5_combo_090 = new Pattern("/bs5/bs5_combo.png").similar(0.90f);

	/* BS5関連 */
	public static final Pattern android_app_abort_ok_090 = new Pattern("/bs5/android_app_abort_ok.png").similar(0.90f);
	public static final Pattern bs5_exit_088 = new Pattern("/bs5/bs5_exit.png").similar(0.88f);
	public static final Pattern bs5_confirm_exit_090 = new Pattern("/bs5/bs5_confirm_exit.png").similar(0.90f).targetOffset(50, 0);
	public static final Pattern bs5_gamecenter_088 = new Pattern("/bs5/bs5_gamecenter.png").similar(0.88f);
	public static final Pattern bs5_updater_close_090 = new Pattern("/bs5/bs5_updater_close.png").similar(0.90f);
	public static final Pattern bs5_engine_reboot_090 = new Pattern("/bs5/bs5_engine_reboot.png").similar(0.90f);
	public static final Pattern bs5_recommends_close_090 = new Pattern("/bs5/bs5_recommends_close.png").similar(0.90f);
	public static final Pattern bs5_continue_090 = new Pattern("/bs5/bs5_continue.png").similar(0.90f);
	public static final Pattern bs5_low_memory_ok_090 = new Pattern("/bs5/bs5_low_memory_ok.png").similar(0.90f);

	/**
	 * 数字画像配列 ランキングNo.用.
	 * <P>
	 * インデックス＝数値
	 */
	public static final Pattern numbers_array_on_ranking[][] = {
		{ new Pattern("/bs5/m2_0.png").similar(0.83f), new Pattern("/bs5/m3_0.png").similar(0.83f) },
		{ new Pattern("/bs5/m2_1.png").similar(0.83f), new Pattern("/bs5/m3_1.png").similar(0.83f), new Pattern("/bs5/m1_1.png").similar(0.88f) },
		{ new Pattern("/bs5/m2_2.png").similar(0.83f), new Pattern("/bs5/m3_2.png").similar(0.83f), new Pattern("/bs5/m1_2.png").similar(0.88f) },
		{ new Pattern("/bs5/m2_3.png").similar(0.83f), new Pattern("/bs5/m3_3.png").similar(0.83f), new Pattern("/bs5/m1_3.png").similar(0.88f) },
		{ new Pattern("/bs5/m2_4.png").similar(0.83f), new Pattern("/bs5/m3_4.png").similar(0.83f), new Pattern("/bs5/m1_4.png").similar(0.88f) },
		{ new Pattern("/bs5/m2_5.png").similar(0.83f), new Pattern("/bs5/m3_5.png").similar(0.83f), new Pattern("/bs5/m1_5.png").similar(0.88f) },
		{ new Pattern("/bs5/m2_6.png").similar(0.83f), new Pattern("/bs5/m3_6.png").similar(0.83f), new Pattern("/bs5/m1_6.png").similar(0.88f) },
		{ new Pattern("/bs5/m2_7.png").similar(0.83f), new Pattern("/bs5/m3_7.png").similar(0.83f), new Pattern("/bs5/m1_7.png").similar(0.88f) },
		{ new Pattern("/bs5/m2_8.png").similar(0.83f), new Pattern("/bs5/m3_8.png").similar(0.83f), new Pattern("/bs5/m1_8.png").similar(0.88f) },
		{ new Pattern("/bs5/m2_9.png").similar(0.83f), new Pattern("/bs5/m3_9.png").similar(0.83f), new Pattern("/bs5/m1_9.png").similar(0.88f) },
	};

	/**
	 * 数字画像配列 ランキングNo.1桁用.
	 * <P>
	 * インデックス＝数値
	 */
	public static final Pattern numbers_array_on_ranking1[] = {
		new Pattern("/bs5/m2_0.png").similar(0.83f), // ダミー
		new Pattern("/bs5/m1_1.png").similar(0.88f),
		new Pattern("/bs5/m1_2.png").similar(0.88f),
		new Pattern("/bs5/m1_3.png").similar(0.88f),
		new Pattern("/bs5/m1_4.png").similar(0.88f),
		new Pattern("/bs5/m1_5.png").similar(0.88f),
		new Pattern("/bs5/m1_6.png").similar(0.88f),
		new Pattern("/bs5/m1_7.png").similar(0.88f),
		new Pattern("/bs5/m1_8.png").similar(0.88f),
		new Pattern("/bs5/m1_9.png").similar(0.88f),
	};

	/**
	 * 数字画像配列 ランキングNo.2+3桁用.
	 * <P>
	 * インデックス＝数値
	 */
	public static final Pattern numbers_array_on_ranking23[] = {
		new Pattern("/bs5/m2_0.png").similar(0.82f),
		new Pattern("/bs5/m2_1.png").similar(0.82f),
		new Pattern("/bs5/m2_2.png").similar(0.82f),
		new Pattern("/bs5/m2_3.png").similar(0.82f),
		new Pattern("/bs5/m2_4.png").similar(0.82f),
		new Pattern("/bs5/m2_5.png").similar(0.82f),
		new Pattern("/bs5/m2_6.png").similar(0.82f),
		new Pattern("/bs5/m2_7.png").similar(0.82f),
		new Pattern("/bs5/m2_8.png").similar(0.82f),
		new Pattern("/bs5/m2_9.png").similar(0.82f),
		new Pattern("/bs5/m3_0.png").similar(0.82f),
		new Pattern("/bs5/m3_1.png").similar(0.82f),
		new Pattern("/bs5/m3_2.png").similar(0.82f),
		new Pattern("/bs5/m3_3.png").similar(0.82f),
	};

	/**
	 * 数字画像配列 ランキングNo.2桁用.
	 * <P>
	 * インデックス＝数値
	 */
	public static final Pattern numbers_array_on_ranking2[] = {
		new Pattern("/bs5/m2_0.png").similar(0.82f),
		new Pattern("/bs5/m2_1.png").similar(0.82f),
		new Pattern("/bs5/m2_2.png").similar(0.82f),
		new Pattern("/bs5/m2_3.png").similar(0.82f),
		new Pattern("/bs5/m2_4.png").similar(0.82f),
		new Pattern("/bs5/m2_5.png").similar(0.82f),
		new Pattern("/bs5/m2_6.png").similar(0.82f),
		new Pattern("/bs5/m2_7.png").similar(0.82f),
		new Pattern("/bs5/m2_8.png").similar(0.82f),
		new Pattern("/bs5/m2_9.png").similar(0.82f),
	};

	/**
	 * 数字画像配列 ランキングNo.3桁用.
	 * <P>
	 * インデックス＝数値
	 */
	public static final Pattern numbers_array_on_ranking3[] = {
		new Pattern("/bs5/m3_0.png").similar(0.83f),
		new Pattern("/bs5/m3_1.png").similar(0.83f),
		new Pattern("/bs5/m3_2.png").similar(0.83f),
		new Pattern("/bs5/m3_3.png").similar(0.83f),
		new Pattern("/bs5/m3_4.png").similar(0.83f),
		new Pattern("/bs5/m3_5.png").similar(0.83f),
		new Pattern("/bs5/m3_6.png").similar(0.83f),
		new Pattern("/bs5/m3_7.png").similar(0.83f),
		new Pattern("/bs5/m3_8.png").similar(0.83f),
		new Pattern("/bs5/m3_9.png").similar(0.83f),
	};

	/**
	 * 数字画像配列 自身のランキングNo.用.
	 * <P>
	 * インデックス＝数値
	 */
	public static final Pattern numbers_array_on_my_ranking[] = {
		new Pattern("/bs5/m4_0.png").similar(0.82f),
		new Pattern("/bs5/m4_1.png").similar(0.82f),
		new Pattern("/bs5/m4_2.png").similar(0.82f),
		new Pattern("/bs5/m4_3.png").similar(0.82f),
		new Pattern("/bs5/m4_4.png").similar(0.82f),
		new Pattern("/bs5/m4_5.png").similar(0.82f),
		new Pattern("/bs5/m4_6.png").similar(0.82f),
		new Pattern("/bs5/m4_7.png").similar(0.82f),
		new Pattern("/bs5/m4_8.png").similar(0.82f),
		new Pattern("/bs5/m4_9.png").similar(0.82f),
	};
}
