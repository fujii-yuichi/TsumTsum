/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application.bs5;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.sikuli.basics.Settings;
import org.sikuli.script.ImagePath;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;

import club.u_1.tsum.application.ConfigureModel;
import club.u_1.tsum.application.EmuInstanceModelInfo;
import club.u_1.tsum.application.my;
import club.u_1.tsum.application.myDateTime;
import club.u_1.tsum.application.myHotkeyListener;
import club.u_1.tsum.application.myIO;
import club.u_1.tsum.application.myLogger;
import club.u_1.tsum.application.mySS;

public class Test {

    // ----- 定数定義 -----
    static final String CONFIG_FILE = "config.json";
    static final String INSTANCE_LIST_FILE = "instance_list.json";

    public static void main(String[] args) {
        if (my.is_windows) {
            File location = new File(my.location);
            if (location.isDirectory()) {
                my.println("ImagePath.setBundlePath( " + my.location + " )");
                ImagePath.setBundlePath(my.location);
            }
        }

        // 設定ファイルの読み込み
        ConfigureModel config = myIO.<ConfigureModel> read(Paths.get(CONFIG_FILE), ConfigureModel.class);
        EmuInstanceModelInfo instances = myIO.<EmuInstanceModelInfo> read(Paths.get("instance_list.json"), EmuInstanceModelInfo.class);

        // 設定値を反映
        myLogger.config(config);
        mySS.config(config);
        myDateTime.config(config);
        ADBWrapper.config(config);
        BS5App.config(config);
        BS5Activity.config(config);
        GillsActivity.config(config);
        GachaActivity.config(config);
        TsumClock.config(config);
        BS5Instance.instances = instances;

        // ホットキーの登録
        org.sikuli.basics.HotkeyManager.getInstance().addHotkey('`', 0, new myHotkeyListener());

        // SikuliXの設定
        Settings.AutoWaitTimeout = 0.001f;
        Settings.WaitScanRate = 100f;

        // ファイル日時の設定
        myDateTime.set_FileDateTime(LocalDateTime.now());

        // エミュインスタンスセット
        BS5Instance.setNo(0);

        // エミュセット
        Region reg_tsum = Screen.getPrimaryScreen();
        if (BS5App.is_running(1)) {
            // ウィンドウ移動&リサイズ
            reg_tsum = BS5App.move_resize_window(0, 0, 568, 983);
            // Regionセット
            BS5Regions.set_region(reg_tsum);
        } else {
            // エミュ再起動
            BS5Activity.bs_restart(BS5Activity.LIMIT_SECONDS_RESTART);
        }

        // // テストスクリーンショット撮影
        // if (BS5App.is_running(1)) {
        //     // // エミュフォーカスセット
        //     // Region reg_tsum = NoxApp.focus();
        //     // // Regionセット
        //     // BS5Regions.set_region(reg_tsum);
        //     // reg_tsum = BS5App.move_resize_window(0, 0, 549, 950);
        //     for (int i = 0; i < 1; i++) {
        //         for (int j = 0; j > 0; j--) {
        //             System.out.println(j);
        //             my.sleep(1000);
        //         }
        //         System.out.println(0);

        //         mySS.take("test");
        //         // mySS.take_test(BS5Regions.title);
        //         // BS5Activity.swipe_n(-4.0d);
        //     }

        //     // // type test
        //     // if (Objects.nonNull(reg_tsum.exists(BS5Patterns.textbox_mailaddress_088, 1))) {
        //     // 	Match m = reg_tsum.getLastMatch();
        //     // 	my.single_tap_by_Location(m.getTarget());
        //     // 	my.sleep(500);
        //     // 	reg_tsum.paste("yukiushagi@gmail.com");
        //     // }
        //     // if (Objects.nonNull(reg_tsum.exists(BS5Patterns.textbox_password_088, 1))) {
        //     // 	Match m = reg_tsum.getLastMatch();
        //     // 	my.single_tap_by_Location(m.getTarget());
        //     // 	my.sleep(500);
        //     // 	reg_tsum.paste("misato1002");
        //     // }
        // }
        // my.exit(0);

        // if (Objects.nonNull(BS5Regions.high_score_so_far.exists(BS5Patterns.high_score_so_far_090, 1.0d))) {
        //     Match m = BS5Regions.high_score_so_far.getLastMatch();
        //     m.highlight(2);
        // }
        // my.exit(0);
        BS5Activity.recovery_to_ranking(120);
        // GillsActivity.test();
        BS5ActivityTest.test_recognition_numbers_in_a_page(1);
        // for (int i = 0; i < 10; i++) {
        //     BS5ActivityTest.test_recognition_numbers_in_a_page_bottom_to_up02();
        // }
        // BS5ActivityTest.test_stop_and_create(10);
        // // 自身の位置取得
        // int number_of_my_rank = BS5Activity.to_my_ranking();
        // my.println(String.format("自身%1$d位", number_of_my_rank));

        // 正常終了
        my.exit(0);
    }
}
