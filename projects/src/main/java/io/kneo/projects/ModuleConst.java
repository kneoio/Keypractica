package io.kneo.projects;


import io.kneo.core.localization.LanguageCode;
import io.kneo.core.server.DefaultAppConst;

import java.util.List;

public class ModuleConst extends DefaultAppConst {
	public static final String CODE = "prj";
	public static final String NAME = "Projects";
	public static List<LanguageCode> AVAILABLE_LANGUAGES = List.of(LanguageCode.ENG, LanguageCode.POR);
	public static String NAME_ENG = "Projects";
	public static String NAME_RUS = "Проекты";
	public static String NAME_KAZ = "Жобалар";
	public static String NAME_POR = "Projetos";
	public static String NAME_SPA = "Proyectos";
}
