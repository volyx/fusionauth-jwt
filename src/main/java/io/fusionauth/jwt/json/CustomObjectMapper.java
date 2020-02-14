package io.fusionauth.jwt.json;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.WriterConfig;
import io.fusionauth.jwks.domain.JSONWebKey;
import io.fusionauth.jwt.domain.Algorithm;
import io.fusionauth.jwt.domain.Header;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.domain.Type;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomObjectMapper {

	@SuppressWarnings("unchecked")
	public <T> T readValue(byte[] src, Class<T> valueType) throws IOException {
		try (final InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(src))) {
			final JsonValue value = Json.parse(reader);
			final JsonObject jsonObject = value.asObject();

			if (valueType.equals(Header.class)) {
				final Header header = new Header();
				final JsonValue algName = jsonObject.get("alg");
				if (algName != null && algName.isString()) {
					header.algorithm = Algorithm.valueOf(algName.asString());
					jsonObject.remove("alg");
				}
				final String typ = jsonObject.getString("typ", Type.JWT.name());
				if (!typ.isEmpty()) {
					header.type = Type.valueOf(typ);
					jsonObject.remove("typ");
				}
				for (String propName : jsonObject.names()) {
					final String propValue = jsonObject.getString(propName, "");
					if (!propValue.isEmpty()) {
						header.set(propName, propValue);
					}
				}
				return (T) header;
			} else if (valueType.equals(JWT.class)) {
				final JWT jwt = new JWT();

				final JsonValue audObject = jsonObject.get("aud");
				if (audObject != null) {
					if (audObject.isString()) {
						jwt.audience = audObject.asString();
					} else if (audObject.isArray()) {
						List<String> audiences = new ArrayList<>();
						for (JsonValue jsonValue : audObject.asArray()) {
							audiences.add(jsonValue.asString());
						}
						jwt.audience = audiences;
					}
					jwt.otherClaims.remove("aud");
				}

				final JsonValue expObject = jsonObject.get("exp");
				if (expObject != null) {
					jwt.expiration = deserialize(expObject);
					jwt.otherClaims.remove("exp");
				}

				final JsonValue iatObject = jsonObject.get("iat");
				if (iatObject != null) {
					jwt.issuedAt = deserialize(iatObject);
					jwt.otherClaims.remove("iat");
				}

				final JsonValue issObject = jsonObject.get("iss");
				if (issObject != null && issObject.isString()) {
					jwt.issuer = issObject.asString();
					jwt.otherClaims.remove("iss");
				}

				final JsonValue nbfObject = jsonObject.get("nbf");
				if (nbfObject != null) {
					jwt.notBefore = deserialize(nbfObject);
					jwt.otherClaims.remove("nbf");
				}

				final JsonValue subObject = jsonObject.get("sub");
				if (subObject != null && subObject.isString()) {
					jwt.subject = subObject.asString();
					jwt.otherClaims.remove("sub");
				}

				final JsonValue jtiObject = jsonObject.get("jti");
				if (jtiObject != null && jtiObject.isString()) {
					jwt.uniqueId = jtiObject.asString();
					jwt.otherClaims.remove("jti");
				}

				for (String propName : jsonObject.names()) {
					final JsonValue propValue = jsonObject.get(propName);
					if (propValue != null) {
						if (propValue.isString()) {
							jwt.otherClaims.put(propName, propValue.asString());
						}
						if (propValue.isArray()) {
							jwt.otherClaims.put(propName, propValue.asArray());
						}
						if (propValue.isBoolean()) {
							jwt.otherClaims.put(propName, propValue.asBoolean());
						}
						if (propValue.isNumber()) {
							try {
								jwt.otherClaims.put(propName, propValue.asInt());
							} catch (NumberFormatException nfe) {
								jwt.otherClaims.put(propName, propValue.asLong());
							}
						}
					}
				}
				return (T) jwt;
			} if (valueType.equals(Map.class)) {
				Map<String, Object> map = new HashMap<>();
				for (String propName : jsonObject.names()) {
					map.put(propName, jsonObject.get(propName).toString());
				}
				return (T) map;
			} else {
				throw new UnsupportedOperationException("unsupported object type " + valueType.getSimpleName());
			}
		}
	}

	public byte[] writeValueAsBytes(Object value) {
		final JsonObject jsonObject = writeValueAsJsonObject(value);
		return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
	}

	private JsonObject writeValueAsJsonObject(Object value) {
		final JsonObject jsonObject;
		if (value instanceof Header) {
			final Header header = (Header) value;
			jsonObject = new JsonObject();
			if (header.algorithm != null) {
				jsonObject.add("alg", header.algorithm.name());
			}
			if (header.type != null) {
				jsonObject.add("typ", header.type.name());
			}
			for (Map.Entry<String, String> entry : header.properties.entrySet()) {
				jsonObject.add(entry.getKey(), entry.getValue());
			}
		} else if (value instanceof JWT) {
			final JWT jwt = (JWT) value;
			jsonObject = new JsonObject();
			if (jwt.audience != null) {
				if (jwt.audience instanceof String) {
					final String audienceValue = (String) jwt.audience;
					jsonObject.add("aud", audienceValue);
				}
				if (jwt.audience instanceof List) {
					final List audienceList = (List) jwt.audience;
					final JsonArray array = new JsonArray();
					for (Object o : audienceList) {
						array.add(o.toString());
					}
					jsonObject.add("aud", array);
				}
			}

			if (jwt.subject != null) {
				jsonObject.add("sub", jwt.subject);
			}

			if (jwt.expiration != null) {
				jsonObject.add("exp", serialize(jwt.expiration));
			}

			if (jwt.issuedAt != null) {
				jsonObject.add("iat", serialize(jwt.issuedAt));
			}

			if (jwt.notBefore != null) {
				jsonObject.add("nbf", serialize(jwt.notBefore));
			}

			if (jwt.issuer != null) {
				jsonObject.add("iss", jwt.issuer);
			}

			if (jwt.uniqueId != null) {
				jsonObject.add("jti", jwt.uniqueId);
			}

			for (Map.Entry<String, Object> entry : jwt.otherClaims.entrySet()) {
				if (entry.getValue() instanceof String) {
					jsonObject.add(entry.getKey(), entry.getValue().toString());
				}
				if (entry.getValue() instanceof List) {
					jsonObject.add(entry.getKey(), entry.getValue().toString());
				}
				if (entry.getValue() instanceof Boolean) {
					jsonObject.add(entry.getKey(), entry.getValue().toString());
				}
			}
		} else if (value instanceof JSONWebKey) {
			final JSONWebKey jsonWebKey = (JSONWebKey) value;

			jsonObject = new JsonObject();
			if (jsonWebKey.alg != null) {
				jsonObject.add("alg", jsonWebKey.alg.name());
			}
			if (jsonWebKey.crv != null) {
				jsonObject.add("crv", jsonWebKey.crv);
			}
			if (jsonWebKey.d != null) {
				jsonObject.add("d", jsonWebKey.d);
			}
			if (jsonWebKey.dp != null) {
				jsonObject.add("dp", jsonWebKey.dp);
			}
			if (jsonWebKey.dq != null) {
				jsonObject.add("dq", jsonWebKey.dq);
			}
			if (jsonWebKey.e != null) {
				jsonObject.add("e", jsonWebKey.e);
			}
			if (jsonWebKey.kid != null) {
				jsonObject.add("kid", jsonWebKey.kid);
			}
			if (jsonWebKey.kty != null) {
				jsonObject.add("kty", jsonWebKey.kty.name());
			}
			if (jsonWebKey.n != null) {
				jsonObject.add("n", jsonWebKey.n);
			}
			if (jsonWebKey.p != null) {
				jsonObject.add("p", jsonWebKey.p);
			}
			if (jsonWebKey.q != null) {
				jsonObject.add("q", jsonWebKey.q);
			}
			if (jsonWebKey.qi != null) {
				jsonObject.add("qi", jsonWebKey.qi);
			}
			if (jsonWebKey.use != null) {
				jsonObject.add("use", jsonWebKey.use);
			}
			if (jsonWebKey.x != null) {
				jsonObject.add("x", jsonWebKey.x);
			}
			if (jsonWebKey.x5c != null) {
				jsonObject.add("x5c", jsonWebKey.x5c.toString());
			}
			if (jsonWebKey.x5t != null) {
				jsonObject.add("x5t", jsonWebKey.x5t);
			}
			if (jsonWebKey.x5t_256 != null) {
				jsonObject.add("x5t#S256", jsonWebKey.x5t_256);
			}
			if (jsonWebKey.y != null) {
				jsonObject.add("y", jsonWebKey.y);
			}

			for (Map.Entry<String, Object> entry : jsonWebKey.other.entrySet()) {
				jsonObject.add(entry.getKey(), entry.getValue().toString());
			}

		} else if (value instanceof Map) {
			jsonObject = new JsonObject();
			final Map<String, Object> map = (Map<String, Object>) value;
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				jsonObject.add(entry.getKey(), entry.getValue().toString());
			}
		} else {
			throw new UnsupportedOperationException("unsupported object type " + value.getClass().getSimpleName());
		}
		return jsonObject;
	}

	public byte[] prettyPrint(Object value) {
		final JsonObject jsonObject = writeValueAsJsonObject(value);
		return jsonObject.toString(WriterConfig.PRETTY_PRINT).getBytes(StandardCharsets.UTF_8);
	}

	ZonedDateTime deserialize(JsonValue jp) throws IOException {
		long value;
		if (jp.isNumber()) {
			value = jp.asLong();
		} else if (jp.isString()) {
			String str = jp.asString().trim();
			if (str.length() == 0) {
				return null;
			}

			try {
				value = Long.parseLong(str);
			} catch (NumberFormatException e) {
				throw e;
			}
		} else {
			throw new IOException();
		}

		return Instant.ofEpochSecond(value).atZone(ZoneOffset.UTC);
	}

	JsonValue serialize(ZonedDateTime value) {
		if (value == null) {
			return Json.NULL;
		} else {
			return Json.value(value.toEpochSecond());
		}
	}
}
