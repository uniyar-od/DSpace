/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.ldn;

import java.io.FileInputStream;
import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.jsonldjava.utils.JsonUtils;
import org.dspace.app.ldn.model.Notification;

public class MockNotificationUtility {

    public static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.enable(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    }

    private MockNotificationUtility() {
        // Intentionally empty
    }

    public static Notification read(String relativeToRootFilepath) throws IllegalArgumentException, IOException {
        try (FileInputStream fis = new FileInputStream(relativeToRootFilepath)) {
            return objectMapper.convertValue(JsonUtils.fromInputStream(fis), Notification.class);
        }
    }

}
