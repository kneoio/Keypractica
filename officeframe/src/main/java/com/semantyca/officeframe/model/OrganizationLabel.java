package com.semantyca.officeframe.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.semantyca.core.localization.LanguageCode;
import com.semantyca.core.model.SimpleReferenceEntity;

import java.util.List;
import java.util.Map;

public class OrganizationLabel extends SimpleReferenceEntity {

    private List<Organization> labels;

    private Map<LanguageCode, String> localizedDescr;

    @JsonIgnore
    public List<Organization> getLabels() {
        return labels;
    }

    public Map<LanguageCode, String> getLocalizedDescr() {
        return localizedDescr;
    }

    public void setLocalizedDescr(Map<LanguageCode, String> localizedDescr) {
        this.localizedDescr = localizedDescr;
    }


}
