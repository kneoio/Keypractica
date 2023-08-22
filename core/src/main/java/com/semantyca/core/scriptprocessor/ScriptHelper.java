package com.semantyca.core.scriptprocessor;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.semantyca.core.server.EnvConst;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;


public class ScriptHelper {

    private static final Logger LOGGER = Logger.getLogger(ScriptHelper.class);


    public static boolean writeJS(Object obj, String path) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat(EnvConst.DEFAULT_DATETIME_FORMAT));
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            File wf = new File(path);
            mapper.writeValue(wf, obj);
        } catch (IOException e) {
            LOGGER.error(e);
            return false;
        }
        return true;
    }





    public static void println(Object text) {
        System.out.println(text.toString());
    }





}
