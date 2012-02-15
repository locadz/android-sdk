/*
 * Copyright 2012. Blue Tang Studio LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.locadz;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;

/**
 * Utils for serializations.
 */
public abstract class SerializationUtils {

    /**
     * ObjectMapper is heavy weight object. We should reuse it when possible.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Hide constructor.
     */
    private SerializationUtils() {
    }

    /**
     * Read json input string the Java Object.
     *
     * @param input the input string.
     * @param clazz the type of the java object to read.
     * @param <T>   the type of the java object to read.
     * @return java representation of the json input string.
     * @throws IOException when read fails.
     */
    @SuppressWarnings("unchecked")
	public static <T> T fromJson(final String input, final TypeReference<T> clazz) throws IOException {
        return (T) MAPPER.readValue(input, clazz);
    }

    /**
     * Read json input string the Java Object.
     *
     * @param input the input string.
     * @param clazz the type of the java object to read.
     * @param <T>   the type of the java object to read.
     * @return java representation of the json input string.
     * @throws IOException when read fails.
     */
    public static <T> T fromJson(final String input, final Class<T> clazz) throws IOException {
        return MAPPER.readValue(input, clazz);
    }
    
    /**
     * Read json input string the Java Object.
     *
     * @param input the input string.
     * @param clazz the type of the java object to read.
     * @param <T>   the type of the java object to read.
     * @return java representation of the json input string.
     * @throws IOException when read fails.
     */
    public static <T> T fromJson(final byte[] input, final Class<T> clazz) throws IOException {
        return MAPPER.readValue(input, clazz);
    }

    /**
     * Read json input string the Java Object.
     *
     * @param input the input string.
     * @param clazz the type of the java object to read.
     * @param <T>   the type of the java object to read.
     * @return java representation of the json input string.
     * @throws IOException when read fails.
     */
    @SuppressWarnings("unchecked")
	public static <T> T fromJson(final byte[] input, final TypeReference<T> clazz) throws IOException {
        return (T) MAPPER.readValue(input, clazz);
    }

        /**
     * Read json input string the Java Object.
     *
     * @param input the input string.
     * @param clazz the type of the java object to read.
     * @param <T>   the type of the java object to read.
     * @return java representation of the json input string.
     * @throws IOException when read fails.
     */
    @SuppressWarnings("unchecked")
	public static <T> T fromJson(final byte[] input, final JavaType clazz) throws IOException {
        return (T) MAPPER.readValue(input, clazz);
    }
    
    /**
     * Write java object to json string.
     * @param obj   the java object
     * @return  the json representation of the java object.
     * @throws IOException  when write fails.
     */
    public static String toJson(Object obj) throws IOException {
        return MAPPER.writeValueAsString(obj);
    }

}
