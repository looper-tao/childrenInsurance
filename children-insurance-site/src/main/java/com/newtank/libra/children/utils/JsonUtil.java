package com.newtank.libra.children.utils;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class JsonUtil {

  private static Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);

  public static String toJson(Object object) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
    mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
    try {
      return mapper.writeValueAsString(object);
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
    }

    return "";
  }

  public static <T> T toObject(String json, Class<T> clazz) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
    mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    try {
      return mapper.readValue(json, clazz);
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
    }

    return null;
  }

  public static <T> List<T> toObjectList(String json, Class<T> clazz) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    JavaType type = mapper.getTypeFactory().constructParametricType(List.class, clazz);
    try {
      return mapper.readValue(json, type);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T toCamelCaseObject(String json, Class<T> clazz) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    try {
      return mapper.readValue(json, clazz);
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
    }
    return null;
  }

}
