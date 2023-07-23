package com.semantyca.core.service;

import com.semantyca.core.dto.rls.RLSDTO;
import com.semantyca.core.model.embedded.RLS;
import com.semantyca.core.repository.UserRepository;
import jakarta.inject.Inject;

public abstract class AbstractService {

    @Inject
    protected UserRepository userRepository;
    protected RLSDTO convertRlSEntries(RLS rls) {
        return new RLSDTO(userRepository.getUserName(rls.getReader()), rls.getAccessLevel().getAlias(), rls.getReadingTime());
    }

}
