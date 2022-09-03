package com.semantyca.dto;

import com.semantyca.model.phrase.Phrase;

import java.util.List;

public interface IPage {

    String getTitle();

    List<Phrase> getContent();

}
