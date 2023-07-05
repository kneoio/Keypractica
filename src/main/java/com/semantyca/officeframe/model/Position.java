package com.semantyca.officeframe.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.model.SimpleReferenceEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class Position extends SimpleReferenceEntity {
    private int rank = 999;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }


}
