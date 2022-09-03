package com.semantyca.model;


import com.semantyca.model.embedded.RLSEntry;
import com.semantyca.model.exception.RLSIsNotNormalized;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class SecureDataEntity<T> extends DataEntity<T> {

    private Map<Integer, RLSEntry> readers = new HashMap<>();

    public Collection<RLSEntry> getReaders() {
        return readers.values();
    }

    public SecureDataEntity addReader(RLSEntry reader){
        readers.put(reader.getReader(), reader);
        return this;
    }



    public RLSEntry getRLS(int reader) throws RLSIsNotNormalized {
        if (readers != null) {
            RLSEntry entry = readers.get(reader);
            if (entry == null) {
                entry = new RLSEntry();
            }
            return entry;
        } else {
            throw new RLSIsNotNormalized();
        }
    }
}
