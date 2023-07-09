package com.semantyca.core.server;


import com.semantyca.core.server.cnst.InterfaceType;
import com.semantyca.core.server.cnst.VisibilityMode;

public class DefaultAppConst {
    public static String MODULE_VERSION = "1.0";
    public static VisibilityMode VISIBILITY = VisibilityMode.NORMAL;
    public static String NAME = "";
    public static String CODE = NAME.toLowerCase();
    public static String NAME_ENG = "";
    public static String NAME_DEU = "";
    public static String NAME_FRA = "";
    public static String NAME_POR = "";
    public static String NAME_SPA = "";
    public static String NAME_ITA = "";
    public static String NAME_CHI = "";
    public static String NAME_ARA = "";
    public static String NAME_BUL = "";
    public static String NAME_RUS = "";
    public static String NAME_KAZ = "";
    public static String BASE_URL = "/wrong_application_name/";
    public static String[] ROLES = {};
    public static String[] ORG_LABELS = {};
    public static String APPROVAL_ROUTE_CATEGORIES[] = {};
    public static InterfaceType AVAILABLE_MODE[] = {InterfaceType.HTML};
    public static String DEFAULT_PAGE = "index";
    public static String AVAILABLE_THEME[] = {"azul", "cinzento", "branco", "preto"};
    public static String DEFAULT_THEME = "branco";
    public static String DEPENDENCIES[] = new String[0];
    public static boolean FORCE_DEPLOYING = false;
    public static String[][] NOT_NULL = null;

    public static String TAG_CATEGORIES[] = {};
    public static String[] UNIT_CATEGORIES = {"quantity","area"};
}
