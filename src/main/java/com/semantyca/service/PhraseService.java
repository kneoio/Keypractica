package com.semantyca.service;

import com.semantyca.dto.FeedbackEntry;
import com.semantyca.dto.IDTO;
import com.semantyca.dto.PhraseDTO;
import com.semantyca.dto.ProcessFeedback;
import com.semantyca.dto.constant.MessageLevel;
import com.semantyca.model.phrase.Phrase;
import com.semantyca.model.user.AnonymousUser;
import com.semantyca.repository.PhraseRepository;
import com.semantyca.repository.exception.DocumentExists;
import com.semantyca.repository.exception.DocumentModificationAccessException;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PhraseService {
    private static final Logger LOGGER = LoggerFactory.getLogger("PhraseService");

    private PhraseRepository repository;

    @Inject
    public PhraseService(Jdbi jdbi) {
        this.repository = new PhraseRepository(jdbi);
    }

    public List<Phrase> getAll() {
        return repository.findAll(100, 0);
    }

    public Optional<Phrase> getById(String id) {
        return repository.findById(UUID.fromString(id));
    }
    public Phrase add(PhraseDTO dto) throws DocumentExists {
        Optional<Phrase> sOptional = repository.findByValue(dto.getBase());
        if (sOptional.isEmpty()) {

            Phrase sentence = new Phrase.Builder()
                    .setBase(dto.getBase())
                    .setTranslation(dto.getTranslation())
                    .build();
            return repository.insert(sentence);
        } else {
            throw new DocumentExists(dto.getBase());
        }
    }

    public IDTO update(PhraseDTO phraseDTO) throws DocumentModificationAccessException {
        repository.update(new Phrase.Builder()
                .setBase(phraseDTO.getBase())
                .setTranslation(phraseDTO.getTranslation()).build(), AnonymousUser.ID);
        return new PhraseDTO();
    }

    public ProcessFeedback delete(String id) {
        ProcessFeedback feedback = new ProcessFeedback();
        if (id.equals("all")) {
            List<Phrase> sentenceList = repository.findAll(0, 0);
            for (Phrase sentence : sentenceList) {
                feedback.addEntry(buildFeedBackEntry(sentence.getId().toString(), repository.delete(sentence)));
            }
        } else {
            Optional<Phrase> sentenceOptional = repository.findById(UUID.fromString(id));
            if (sentenceOptional.isPresent()) {
                feedback.addEntry(buildFeedBackEntry(id, repository.delete(sentenceOptional.get())));
            } else {
                FeedbackEntry feedbackEntry = new FeedbackEntry();
                feedbackEntry.setId(id);
                feedbackEntry.setLevel(MessageLevel.FAILURE);
                feedbackEntry.setDescription("Document not found");
                feedback.addEntry(feedbackEntry);
                LOGGER.debug("Document ${u} not found", id);

            }
        }
        return feedback;
    }

    private FeedbackEntry buildFeedBackEntry(String id, int result) {
        FeedbackEntry feedbackEntry = new FeedbackEntry();
        feedbackEntry.setId(id);
        if (result == 1) {
            feedbackEntry.setLevel(MessageLevel.SUCCESS);
            feedbackEntry.setDescription("Document has been deleted");
            LOGGER.debug("Document ${u} has been deleted", id);
        } else {
            feedbackEntry.setLevel(MessageLevel.FAILURE);
            feedbackEntry.setDescription("Something wrong happened during deleting process");
            LOGGER.debug("Document ${u} didnt delete", id);
        }
        return feedbackEntry;
    }
}
