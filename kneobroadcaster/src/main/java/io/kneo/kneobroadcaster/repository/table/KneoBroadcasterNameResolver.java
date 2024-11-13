package io.kneo.kneobroadcaster.repository.table;

import io.kneo.core.repository.table.EntityData;
import io.kneo.core.repository.table.TableNameResolver;

public class KneoBroadcasterNameResolver extends TableNameResolver {
    public static final String SOUND_FRAGMENT = "sound fragment";

    private static final String SOUND_FRAGMENT_TABLE_NAME = "kneobroadcaster__sound_fragments";
    private static final String SOUND_FRAGMENT_ACCESS_TABLE_NAME = "kneobroadcaster__sound_fragments_readers";
    private static final String SOUND_FRAGMENT_IMAGES_TABLE_NAME = "kneobroadcaster__sound_fragments_images";

    public EntityData getEntityNames(String type) {
        return switch (type) {
            case SOUND_FRAGMENT -> new EntityData(
                    SOUND_FRAGMENT_TABLE_NAME,
                    SOUND_FRAGMENT_ACCESS_TABLE_NAME,
                    null,
                    SOUND_FRAGMENT_IMAGES_TABLE_NAME
            );
            default -> super.getEntityNames(type);
        };
    }

    public static KneoBroadcasterNameResolver create() {
        return new KneoBroadcasterNameResolver();
    }
}
