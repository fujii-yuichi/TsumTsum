/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application.bs5;

import org.sikuli.script.Region;

/**
 * Region定義.
 */
public class BS5Regions {

	static public Region tsum = null;
	static public Region rank_list = null;
	static public Region title = null;
	static public Region rank1 = null;
	static public Region rank2 = null;
	static public Region rank3 = null;
	static public Region rank4 = null;
	static public Region hearts = null;
	static public Region close_upper = null;
	static public Region close_upper_left = null;
	static public Region close_middle = null;
	static public Region close_middle_left = null;
	static public Region close_lower = null;
	static public Region close_lower_left = null;
	static public Region close_lower_most_left = null;
	static public Region turn_back = null;
	static public Region high_score_so_far = null;
	static public Region mail_icon = null;
	static public Region mailbox = null;
	static public Region collectively_receive = null;
	static public Region colorful = null;
	static public Region ok_upper = null;
	static public Region retry = null;
	static public Region play = null;
	static public Region home = null;
	static public Region cancel_upper = null;
	static public Region cancel_middle = null;
	static public Region cancel_lower = null;
	static public Region tap_to_start = null;
	static public Region high_score = null;
	static public Region tsum_clock = null;
	static public Region not_accept_push = null;
	static public Region my_rank = null;

	/**
	 * ツムアプリRegionを元に各Regionセット.
	 * @param reg_tsum
	 */
	static public void set_region(Region reg_tsum) {
		BS5Regions.tsum = reg_tsum;
		BS5Regions.rank_list = new Region(reg_tsum.x + 1, reg_tsum.y + 305, 533, 400);
		BS5Regions.title = new Region(reg_tsum.x + 183, reg_tsum.y + 258, 94, 48);
		BS5Regions.rank1 = new Region(reg_tsum.x + 63, reg_tsum.y + 317, 46, 82);
		BS5Regions.rank2 = new Region(reg_tsum.x + 63, reg_tsum.y + 417, 46, 82);
		BS5Regions.rank3 = new Region(reg_tsum.x + 63, reg_tsum.y + 517, 46, 82);
		BS5Regions.rank4 = new Region(reg_tsum.x + 63, reg_tsum.y + 617, 46, 82);
		BS5Regions.hearts = new Region(reg_tsum.x + 443, reg_tsum.y + 304, 16, 400);
		BS5Regions.close_upper = new Region(reg_tsum.x + 206, reg_tsum.y + 699, 115, 55);
		BS5Regions.close_upper_left = new Region(reg_tsum.x + 88, reg_tsum.y + 695, 116, 55);
		BS5Regions.close_middle = new Region(reg_tsum.x + 206, reg_tsum.y + 735, 116, 111);
		BS5Regions.close_middle_left = new Region(reg_tsum.x + 88, reg_tsum.y + 735, 116, 111);
		BS5Regions.close_lower = new Region(reg_tsum.x + 206, reg_tsum.y + 824, 116, 55);
		BS5Regions.close_lower_left = new Region(reg_tsum.x + 88, reg_tsum.y + 824, 116, 55);
		BS5Regions.close_lower_most_left = new Region(reg_tsum.x + 45, reg_tsum.y + 824, 100, 55);
		BS5Regions.high_score_so_far = new Region(reg_tsum.x + 165, reg_tsum.y + 570, 204, 27);
		BS5Regions.mail_icon = new Region(reg_tsum.x + 419, reg_tsum.y + 188, 62, 60);
		BS5Regions.mailbox = new Region(reg_tsum.x + 161, reg_tsum.y + 232, 206, 40);
		BS5Regions.collectively_receive = new Region(reg_tsum.x + 277, reg_tsum.y + 707, 209, 51);
		BS5Regions.turn_back = new Region(reg_tsum.x + 38, reg_tsum.y + 828, 98, 38);
		BS5Regions.colorful = new Region(reg_tsum.x + 217, reg_tsum.y + 526, 97, 46);
		BS5Regions.ok_upper = new Region(reg_tsum.x + 309, reg_tsum.y + 541, 142, 71);
		BS5Regions.retry = new Region(reg_tsum.x + 271, reg_tsum.y + 520, 213, 69);
		BS5Regions.play = new Region(reg_tsum.x + 191, reg_tsum.y + 824, 147, 53);
		BS5Regions.home = new Region(reg_tsum.x + 42, reg_tsum.y + 979, 30, 15);
		BS5Regions.cancel_upper = new Region(reg_tsum.x + 86, reg_tsum.y + 541, 138, 71);
		BS5Regions.cancel_middle = new Region(reg_tsum.x + 86, reg_tsum.y + 615, 138, 109);
		BS5Regions.cancel_lower = new Region(reg_tsum.x + 86, reg_tsum.y + 721, 138, 71);
		BS5Regions.tap_to_start = new Region(reg_tsum.x + 116, reg_tsum.y + 782, 303, 71);
		BS5Regions.high_score = new Region(reg_tsum.x + 1, reg_tsum.y + 472, 533, 80);
		BS5Regions.tsum_clock = new Region(reg_tsum.x + 361, reg_tsum.y + 264, 119, 40);
		BS5Regions.not_accept_push = new Region(reg_tsum.x + 43, reg_tsum.y + 524, 211, 49);
		BS5Regions.my_rank = new Region(reg_tsum.x + 277, reg_tsum.y + 645, 80, 26);

		// BS5Regions.rank_list = new Region(reg_tsum.x + 2, reg_tsum.y + 313, 533, 400);

		GillsActivity.set_region();
		GachaActivity.set_region();
		HeartCountActivity.set_region();
	}
}
