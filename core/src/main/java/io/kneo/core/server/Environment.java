package io.kneo.core.server;

import io.kneo.core.localization.Vocabulary;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

public class Environment {
    public static Date startTime;
    public static String orgName;
    public static String orgShortName;
    public static String color;
    public static String logo;
    public static String wallpaper;

    public static IUtilityDatabase utilityDatabase;
    public static Vocabulary vocabulary;
    public static boolean isDevMode;
    //public static String httpSchema = WebServer.HTTP_SCHEMA;
    public static int httpPort = EnvConst.DEFAULT_HTTP_PORT;
    //public static AppEnv adminApplication;
    public static HashMap<String, String> mimeHash = new HashMap<>();
    public static String tmpDir;
    public static String trash;
    public static Boolean isTLSEnable = false;
    public static int secureHttpPort;
    public static String certFile = "";
    public static String certKeyFile = "";

    public static Boolean mailEnable = false;
    public static String smtpPort = "25";
    public static boolean smtpAuth;
    public static String smtpHost;
    public static String smtpUser;
    public static String smtpPassword;
    public static String smtpUserName;

    public static Boolean slackEnable = false;
    public static String slackToken;

    public static Boolean translatorEnable = false;
    //public static String translatorEngine = EnvConst.DEFAULT_TRANSLATOR_ENGINE;
    public static String yandexTranslatorApiKey;

    public static Boolean elasticSearchEnable = false;
    public static int elasticSearchPort;
    public static String elasticSearchHost;

    public static Boolean weatherServiceEnable = false;
    public static String weatherServiceApiKey;
    public static String weatherServiceLocality;

    public static Boolean mapServiceEnable = false;
    public static String mapsApiKey;
    public static boolean integrationHubEnable = false;
    public static String integrationHubHost;
    public static boolean isVerbosedLogging;
    public static Boolean activityRecordingEnable = false;
   // public static PeriodicalServices periodicalServices;

    private static String hostName;
    private static String virtualHostName;
    //private static HashMap<String, AppEnv> applications = new HashMap<>();

    private static String officeFrameDir;
    private static String kernelDir;

    public static void init() {
        startTime = new Date();
        initProcess();
    }

    private static void initProcess() {


        final File tmp = new File("tmp");
        if (!tmp.exists()) {
            tmp.mkdir();
        }

        tmpDir = tmp.getAbsolutePath();

        final File jrDir = new File(tmpDir + File.separator + "trash");
        if (!jrDir.exists()) {
            jrDir.mkdir();
        }

        trash = jrDir.getAbsolutePath();




    }

}
