package com.semantyca.core.server;

import com.semantyca.core.localization.Vocabulary;

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

       /*     orgName = XMLUtil.getTextContent(xmlDocument, "/nextbase/orgname");
            if (orgName.isEmpty()) {
                orgName = EnvConst.APP_ID;
            }
            orgShortName = orgName;

            color = XMLUtil.getTextContent(xmlDocument, "/nextbase/orgcolor");
            if (color.isEmpty()) {
                color = EnvConst.DEFAULT_COLOR;
            }

            logo = XMLUtil.getTextContent(xmlDocument, "/nextbase/orglogo");
            File logoFile = new File(EnvConst.WEB_APPS_FOLDER + logo);
            if (!logoFile.exists()) {
                logo = EnvConst.DEAFAULT_APP_LOGO;
            }

            wallpaper = XMLUtil.getTextContent(xmlDocument, "/nextbase/orgwallpaper");
            if (wallpaper.isEmpty()) {
                wallpaper = EnvConst.DEFAULT_WALLPAPER;
            }


            hostName = XMLUtil.getTextContent(xmlDocument, "/nextbase/hostname");
            if (hostName.isEmpty()) {
                InetAddress addr = null;
                try {
                    addr = InetAddress.getLocalHost();
                } catch (final UnknownHostException e) {
                    Server.logger.exception(e);
                }
                hostName = addr.getHostName();
            }

            virtualHostName = XMLUtil.getTextContent(xmlDocument, "/nextbase/virtualhostname");

            final String portAsText = XMLUtil.getTextContent(xmlDocument, "/nextbase/port");
            try {
                httpPort = Integer.parseInt(portAsText);
            } catch (final NumberFormatException nfe) {

            }

            try {
                isTLSEnable = XMLUtil.getTextContent(xmlDocument, "/nextbase/tls/@mode").equalsIgnoreCase("on");
                if (isTLSEnable) {
                    String tlsPort = XMLUtil.getTextContent(xmlDocument, "/nextbase/tls/port");
                    try {
                        secureHttpPort = Integer.parseInt(tlsPort);
                    } catch (final NumberFormatException nfe) {
                        secureHttpPort = EnvConst.DEFAULT_HTTP_PORT;
                    }
                    certFile = XMLUtil.getTextContent(xmlDocument, "/nextbase/tls/certfile");
                    certKeyFile = XMLUtil.getTextContent(xmlDocument, "/nextbase/tls/certkeyfile");

                    Server.logger.info("TLS is enabled");
                    httpPort = secureHttpPort;
                    httpSchema = WebServer.HTTP_SECURE_SCHEMA;
                }
            } catch (final Exception ex) {
                Server.logger.info("TLS configuration error");
                isTLSEnable = false;
                certFile = "";
                certKeyFile = "";
            }
*/
  /*          try {
                mailEnable = XMLUtil.getTextContent(xmlDocument, "/nextbase/mail/@mode").equalsIgnoreCase("on") ? true
                        : false;
                if (mailEnable) {
                    smtpHost = XMLUtil.getTextContent(xmlDocument, "/nextbase/mail/smtphost");
                    if (!smtpHost.isEmpty()) {
                        smtpAuth = Boolean.valueOf(XMLUtil.getTextContent(xmlDocument, "/nextbase/mail/smtpauth"));
                        smtpUser = XMLUtil.getTextContent(xmlDocument, "/nextbase/mail/smtpuser");
                        smtpPassword = XMLUtil.getTextContent(xmlDocument, "/nextbase/mail/smtppassword");
                        smtpUserName = XMLUtil.getTextContent(xmlDocument, "/nextbase/mail/smtpusername",
                                EnvConst.APP_ID + ", bot");
                        smtpPort = XMLUtil.getTextContent(xmlDocument, "/nextbase/mail/smtpport");
                        Server.logger.info("MailAgent is going to redirect some messages to host: " + smtpHost);
                    } else {
                        Server.logger.warning("SMTP host is not set, the MailAgent has been switched off");
                        mailEnable = false;
                    }
                } else {
                    Server.logger.info("MailAgent is switched off");
                }
            } catch (final NumberFormatException nfe) {
                Server.logger.info("MailAgent is not set");
            }
*/


    }

}
