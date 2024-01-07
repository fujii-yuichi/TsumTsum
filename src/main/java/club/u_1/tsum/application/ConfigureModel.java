/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application;

import java.io.Serializable;

/**
 * 設定情報.
 */
public class ConfigureModel implements Serializable {
    /**
     * 検出タイムアウト(second).
     */
    public double WAIT_TIMEOUT = 0.001;
    /**
     * 検出タイムアウト(second) for リカバリ.
     */
    public double WAIT_TIMEOUT_FOR_RECOVERY = 0.100;
    /**
     * 検出タイムアウト(second) for ツム時計.
     */
    public double WAIT_TIMEOUT_FOR_CLOCK = 0.000001;

    /**
     * 起動間隔(hour).
     */
    public int LAUNCH_INTERVAL_HOURS = 1;
    /**
     * 起動間隔(minute).
     */
    public int LAUNCH_INTERVAL_MINUTES = 0;
    /**
     * 起動間隔(second).
     */
    public int LAUNCH_INTERVAL_SECONDS = -20;

    /**
     * タップ間隔(millisecond).
     */
    public long TAP_INTERVAL_MILLIS = 200;

    /**
     * 監視間隔(millisecond).
     */
    public int MONITORING_INTERVAL_MILLIS = 10;
    /**
     * 監視間隔(nanosecond).
     */
    public long MONITORING_INTERVAL_NANOS = (long) MONITORING_INTERVAL_MILLIS * 1000000L;

    /**
     * 再起動用オフセット時間(minute).
     */
    public int OFFSET_MINUTES_RESTART = -4;
    /**
     * ギルス生成用オフセット時間(minute).
     */
    public int OFFSET_MINUTES_GILLS_CREATE = -5;
    /**
     * 月曜日用オフセット時間(minute).
     */
    public int OFFSET_MINUTES_FOR_MONDAY = -4;

    /**
     * リトライ回数
     */
    public int RETRY_SYSTEMCALL = 3;
    public int RETRY_GET_DATETIME = 2;
    public int RETRY_PRE_PROCESS = 2;
    public int RETRY_POST_PROCESS = 2;
    public int RETRY_CONNECT_ADB = 1;
    public int RETRY_SEND_HEARTS_ALL = 10;
    public int RETRY_EXISTS = 50;
    public int RETRY_EXISTS_NUMBER_ON_MICKEY = 3;
    public int RETRY_EXISTS_WITH_COMMUNICATION = 500;
    public int RETRY_EXISTS_UPPER_CLOSE = 42;
    public int RETRY_EXISTS_OK = 36;
    public int RETRY_EXISTS_HIGH_SCORE = 200;
    public int RETRY_DISAPPEAR_HIGH_SCORE = 50;
    public int RETRY_TAP_HIGH_SCORE = 50;
    public int RETRY_TAP_UPPER_CLOSE = 50;
    public int RETRY_TAP_HEART = 50;
    public int RETRY_TAP_HEART_PRESENT_OK = 50;
    public int RETRY_SEEK = 30;

    /**
     * 制限時間(second).
     */
    public long LIMIT_SECONDS_RESTART = 240;
    public long LIMIT_SECONDS_START = 90;
    public long LIMIT_SECONDS_GET_TSUM_CLOCK = 300;
    public long LIMIT_SECONDS_GIVE_A_HEART = 10;
    public long LIMIT_SECONDS_TAKE_ALL_HEART = 10;
    public long LIMIT_SECONDS_EXISTS_HEART = 3600;
    public long LIMIT_SECONDS_RECOVERY_TO_RANKING = 30;
    public long LIMIT_SECONDS_ROOT_EXIST = 30;
    public long LIMIT_SECONDS_PLAY_ROBOTMON = 600;
    public long LIMIT_SECONDS_LOGIN_LINE = 60;
    public long AFTER_SECONDS_POPS_UP_HEART = 3610;
    public long LIMIT_SECONDS_POPS_UP_HEART = 3660;
    public long LIMIT_SECONDS_FREEZED = 12;

    /**
     * しきい値.
     */
    public long THRESHOLD_SECONDS_JUDGMENT_UPDATE = 3;
    public long THRESHOLD_MILLIS_JUDGMENT_LEAD = 500;
    public long THRESHOLD_MILLIS_JUDGMENT_ABS = 200;

    /**
     * システム再起動スケジュール(hour).
     */
    public Integer[] SYSTEM_REBOOT_SCHEDULE_HOUR = new Integer[] { 0, 6, 12, 18 };

    /**
     * ランキングリスト左端からのオフセットX座標.
     */
    public int OFFSET_X_FROM_LEADING_EDGE_OF_RANK_LIST = 222;

    /**
     * 生存通知ファイル.
     */
    public String DATETIME_ALIVE = "alive.txt";
    /**
     * 次回実行日時ファイル.
     */
    public String DATETIME_NEXT = "next.txt";

    /**
     * エミュレータプロセス名.
     */
    public String APP_PROCESS_NAME = "HD-Player";

    /**
     * パス関連.
     */
    public String PATH_SS_SAVE = "";
    public String DIR_FREEZED_EXISTS = ".freezed";
    public String DIR_TSUM_CLOCK_SAVE = ".clock";
    public String PATH_DATETIME = "";
    public String PATH_HEARTS = "";
    public String PATH_FINE = "";
    public String PATH_MEMBERS = "";
    public String FILENAME_HEARTS = "hearts.";
    public String FILENAME_FINE = "fine.";
    public String FILENAME_MEMBERS = "members.";
    public String EXT = ".txt";
    public String PATH_ADB = "";
    public String PATH_PS1_SET_WINDOW = "";
    public String PATH_PS1_GET_WINDOW = "";
    public String PATH_LONG_CLOSE_HIGHSCORE = "";
    public String PATH_MM_UPDATE_SAVE = "";
    public String FILENAME_MEMBERS_LIST = "members_list.bin";
    public String FILENAME_MEMBERS_LIST_BAK = "members_list.bak";
    public String FILENAME_MEMBERS_LIST_JSON = "members_list.json";
    public String FILENAME_MEMBERS_LIST_BAK_JSON = "members_list.bak.json";
    public String PATH_HC_UPDATE_SAVE = "";
    public String PATH_HC_SAVE = "";

    /**
     * LINE API トークン.
     */
    public String LINE_API_TOKEN_ALERT = "";
    public String LINE_API_TOKEN_NOTIFICATION = "";
}
