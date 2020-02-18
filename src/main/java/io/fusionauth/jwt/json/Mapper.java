/*
 * Copyright (c) 2016-2019, FusionAuth, All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package io.fusionauth.jwt.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.fusionauth.jwt.InvalidJWTException;

import java.io.IOException;

/**
 * Serialize and de-serialize JWT header and payload.
 *
 * @author Daniel DeGroff
 */
public class Mapper {
  private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final MinimalJsonObjectMapper CUSTOM_OBJECT_MAPPER = new MinimalJsonObjectMapper();
  private static final NanoJsonObjectMapper NANO_OBJECT_MAPPER = new NanoJsonObjectMapper();
  private static final PlainObjectMapper PLAIN_OBJECT_MAPPER = new PlainObjectMapper();
  public static final boolean isMinimalJsonDecode = Boolean.getBoolean("minimal.json.decode");
  public static final boolean isMinimalJsonEncode = Boolean.getBoolean("minimal.json.encode");

  public static final boolean isNanoJsonDecode = Boolean.getBoolean("nano.json.decode");
  public static final boolean isNanoJsonEncode = Boolean.getBoolean("nano.json.encode");

  public static final boolean isPlainJsonDecode = Boolean.getBoolean("plain.json.decode");
  public static final boolean isPlainJsonEncode = Boolean.getBoolean("plain.json.encode");

  public static <T> T deserialize(byte[] bytes, Class<T> type) throws InvalidJWTException {
    try {
      if (isMinimalJsonDecode) {
        return CUSTOM_OBJECT_MAPPER.readValue(bytes, type);
      }
      if (isNanoJsonDecode) {
        return NANO_OBJECT_MAPPER.readValue(bytes, type);
      }
      if (isPlainJsonDecode) {
        return PLAIN_OBJECT_MAPPER.readValue(bytes, type);
      }
      return OBJECT_MAPPER.readValue(bytes, type);
    } catch (IOException e) {
      throw new InvalidJWTException("The JWT could not be de-serialized.", e);
    }
  }

  public static byte[] prettyPrint(Object object) throws InvalidJWTException {
    try {
      if (isMinimalJsonEncode) {
        return CUSTOM_OBJECT_MAPPER.prettyPrint(object);
      }
      if (isNanoJsonEncode) {
        return NANO_OBJECT_MAPPER.prettyPrint(object);
      }
      if (isPlainJsonEncode) {
        return PLAIN_OBJECT_MAPPER.prettyPrint(object);
      }
      return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsBytes(object);
    } catch (JsonProcessingException e) {
      throw new InvalidJWTException("The object could not be serialized.", e);
    }
  }

  public static byte[] serialize(Object object) throws InvalidJWTException {
    try {
      if (isMinimalJsonEncode) {
        return CUSTOM_OBJECT_MAPPER.writeValueAsBytes(object);
      }
      if (isNanoJsonEncode) {
        return NANO_OBJECT_MAPPER.writeValueAsBytes(object);
      }

      if (isPlainJsonEncode) {
        return PLAIN_OBJECT_MAPPER.writeValueAsBytes(object);
      }
      return OBJECT_MAPPER.writeValueAsBytes(object);
    } catch (JsonProcessingException e) {
      throw new InvalidJWTException("The JWT could not be serialized.", e);
    }
  }

  static {
    OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
        .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
        .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
        .configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true)
        .registerModule(new JacksonModule());
  }
}
