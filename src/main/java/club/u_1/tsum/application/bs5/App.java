/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application.bs5;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
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

/**
 * Hello world!
 *
 */
public class App {

    // ----- 定数定義 -----
    static final String LOCK_FILE = ".tsum.bs5.locks";
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

        Screen.getPrimaryScreen();
        mySS.set_FileDateTime(LocalDateTime.now());

        // 多重起動防止
        FileOutputStream fos;
        FileChannel fc;
        FileLock fl;
        try {
            File file = Paths.get(System.getProperty("java.io.tmpdir"), LOCK_FILE).toFile();
            fos = new FileOutputStream(file);
            fc = fos.getChannel();
            fl = fc.tryLock();
            if (fl == null) {
                // 多重起動だった
                myLogger.error("多重起動を検出した！");
                my.exit(0);
                return;
            } else {
                // 多重起動ではなかった
            }
        } catch (FileNotFoundException e) {
            myLogger.warn("多重起動検出でFileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            myLogger.warn("多重起動検出でIOException");
            e.printStackTrace();
        }

        // ホットキーの登録
        org.sikuli.basics.HotkeyManager.getInstance().addHotkey('`', 0, new myHotkeyListener());

        // SikuliXの設定
        Settings.AutoWaitTimeout = 0.001f;
        Settings.WaitScanRate = 100f;

        // ファイル日時の設定
        myDateTime.set_FileDateTime(LocalDateTime.now());

        // エミュインスタンスセット
        BS5Instance.setNo(BS5Instance.getNextNo());

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

        // メイン
        BS5Activity main = new BS5Activity();
        main.run();

        // 正常終了
        my.exit(0);
    }
}
