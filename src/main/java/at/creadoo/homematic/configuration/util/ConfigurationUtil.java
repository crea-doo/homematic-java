/*
 * Copyright 2017 crea-doo.at
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package at.creadoo.homematic.configuration.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import at.creadoo.homematic.configuration.Configuration;

/**
 * Class providing the JSON serializer and deserialzer for objects
 */
public final class ConfigurationUtil {

	private static final GsonBuilder builder = new GsonBuilder();
	
	private static final Gson gson;
	
	private static final Gson gsonPretty;
	
	private ConfigurationUtil() {
		//
	}

	static {
		builder.serializeNulls();
		builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		gson = builder.create();
		
		builder.setPrettyPrinting();
		gsonPretty = builder.create();
	}

	/**
	 * Serializes an {@link Object} to a JsonString.
	 * 
	 * @param object
	 *            - the {@link Object}
	 * @return - the JSON string
	 */
	public static String toJson(final Object object) {
		return toJson(object, false);
	}

	/**
	 * Serializes an {@link Object} to a JsonString.
	 * 
	 * @param object
	 *            - the {@link Object}
	 * @param prettyPrint
	 *            - enable/disable pretty printing
	 * @return - the JSON string
	 */
	public static String toJson(final Object object, final boolean prettyPrint) {
		if (prettyPrint) {
			return gsonPretty.toJson(object);
		} else {
			return gson.toJson(object);
		}
	}

	/**
	 * Serializes an {@link Object} to a JsonString.
	 * 
	 * @param object
	 *            - the {@link Object}
	 * @param clazz
	 *            - the {@link Class}
	 * @return - the JSON string
	 */
	public static String toJson(final Object object, final Class<?> clazz) {
		return toJson(object, clazz, false);
	}

	/**
	 * Serializes an {@link Object} to a JsonString.
	 * 
	 * @param object
	 *            - the {@link Object}
	 * @param clazz
	 *            - the {@link Class}
	 * @param prettyPrint
	 *            - enable/disable pretty printing
	 * @return - the JSON string
	 */
	public static String toJson(final Object object, final Class<?> clazz, final boolean prettyPrint) {
		if (prettyPrint) {
			return gsonPretty.toJson(object, clazz);
		} else {
			return gson.toJson(object, clazz);
		}
	}

	/**
	 * Deserializes a JsonString to an {@link Object}.
	 * 
	 * @param json
	 *            - the Json String
	 * @return - the Object
	 */
	public static Object toObject(final String json) {
		return gson.fromJson(json, Object.class);
	}

	/**
	 * Deserializes a JsonString to an {@link Object}.
	 * 
	 * @param json
	 *            - the Json String
	 * @return - the Object
	 */
	public static Object toObject(final String json, final Class<?> objectClass) {
		return gson.fromJson(json, objectClass);
	}
	
	/**
	 * Deserializes a JsonString to {@link Configuration}.
	 * 
	 * @param json
	 *            - the Json String
	 * @return - the Configuration
	 */
	public static Configuration toConfiguration(final String json) {
		return gson.fromJson(json, Configuration.class);
	}
	
	public static boolean isJsonPrimitive(final Class<?> clazz) {
		return  clazz.isPrimitive() ||
				clazz.equals(Boolean.class) || 
				clazz.equals(Integer.class) ||
				clazz.equals(Character.class) ||
				clazz.equals(Byte.class) ||
				clazz.equals(Short.class) ||
				clazz.equals(Double.class) ||
				clazz.equals(Long.class) ||
				clazz.equals(Float.class) ||
				clazz.equals(String.class);		
	}
 
}
