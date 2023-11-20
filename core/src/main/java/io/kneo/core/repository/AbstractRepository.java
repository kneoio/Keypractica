package io.kneo.core.repository;

import java.time.ZoneId;

public class AbstractRepository {
    public static int NO_ACCESS = 0;
    public static int READ_ONLY = 1;
    public static int EDIT_IS_ALLOWED = 2;
    public static int EDIT_AND_DELETE_ARE_ALLOWED = 3;

    private static final ZoneId zoneId = ZoneId.of( "Europe/Riga" );


}
