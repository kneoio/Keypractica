package com.semantyca.service;


import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PhraseService {
   /* private static final Logger LOGGER = LoggerFactory.getLogger("PhraseService");

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
    public Phrase add(PhraseDTO dto) throws DocumentExistsException {
        Optional<Phrase> sOptional = repository.findByValue(dto.getBase());
        if (sOptional.isEmpty()) {

            Phrase phrase = new Phrase.Builder()
                    .setBase(dto.getBase())
                    .setTranslation(dto.getTranslation())
                    .setLabels(dto.getLabels())
                    .build();

            return repository.insert(phrase);
        } else {
            throw new DocumentExistsException(dto.getBase());
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
    }*/
}
