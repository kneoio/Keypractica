package io.kneo.kneobroadcaster.model;

import io.kneo.core.model.SecureDataEntity;
import io.kneo.kneobroadcaster.model.cnst.FragmentType;
import io.kneo.kneobroadcaster.model.cnst.SourceType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
public class SoundFragment extends SecureDataEntity<UUID> {
    private SourceType source;
    private int status;
    private String fileUri;
    private String localPath;
    private FragmentType type;
    private String name;
    private String artist;
    private String createdAt;
    private String genre;
    private String album;
    private byte[] file;

}
