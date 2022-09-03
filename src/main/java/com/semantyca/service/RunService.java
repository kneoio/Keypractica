package com.semantyca.service;


import com.semantyca.dto.BasicPage;
import com.semantyca.dto.IPage;
import com.semantyca.model.phrase.Phrase;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import static org.asciidoctor.Asciidoctor.Factory.create;
import static org.asciidoctor.OptionsBuilder.options;

@ApplicationScoped
public class RunService {
    private static final Logger LOGGER = LoggerFactory.getLogger("RunService");

    Asciidoctor asciidoctor;
    ArrayList<String> pre = new ArrayList<String>();
    public RunService() {
         asciidoctor = create();
    }



    public IPage start(String presentationId) throws URISyntaxException, IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("kazakh_lessons.adoc");
        if (resource == null) {
            throw new IllegalArgumentException("file not found! ");
        }

        FileReader reader = new FileReader(new File(resource.toURI()));
        StringWriter writer = new StringWriter();

        Document doc = asciidoctor.loadFile(new File(resource.toURI()), options().asMap());

        StringBuffer htmlBuffer = writer.getBuffer();
        BasicPage page = new BasicPage();
        page.setTitle("1");
        page.addPhrase(new Phrase("Я помою это", " Мен оны жу##амын## __(жуу)__"));
        page.addPhrase(new Phrase("Я помою посуду", "Мен ыдыс жу##амын##"));
        return page;
    }



}
