package io.fusionauth.jwt.json;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;
import io.fusionauth.jwks.domain.JSONWebKey;
import io.fusionauth.jwt.domain.Algorithm;
import io.fusionauth.jwt.domain.Header;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.domain.Type;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NanoJsonObjectMapper {

	public <T> T readValue(byte[] src, Class<T> valueType) throws IOException {
		try (final ByteArrayInputStream stream = new ByteArrayInputStream(src)) {
			JsonObject jsonObject = JsonParser.object().from(stream);

			if (valueType.equals(Header.class)) {
				return (T) readHeader(jsonObject);
			} else if (valueType.equals(JWT.class)) {
				return (T) readJWT(jsonObject);
			}
			if (valueType.equals(Map.class)) {
				final Map<String, Object> map = new HashMap<>();
				for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
					map.put(entry.getKey(), entry.getValue());
				}
				return (T) map;
			} else {
				throw new UnsupportedOperationException("unsupported object type " + valueType.getSimpleName());
			}
		} catch (JsonParserException e) {
			throw new IOException(e);
		}
	}

	private static JWT readJWT(JsonObject jsonObject) {
		final JWT jwt = new JWT();

		final Object audObject = jsonObject.get("aud");
		if (audObject != null) {
			if (audObject instanceof String) {
				jwt.audience = audObject;
			} else if (audObject instanceof List) {
				List<String> audiences = new ArrayList<>();
				for (Object jsonValue : (List) audObject) {
					audiences.add(jsonValue.toString());
				}
				jwt.audience = audiences;
			}
			jsonObject.remove("aud");
		}

		final long exp = jsonObject.getLong("exp");
		if (exp != 0) {
			jwt.expiration = deserializeZonedDateTime(exp);
			jsonObject.remove("exp");
		}

		final long iat = jsonObject.getLong("iat");
		if (iat != 0) {
			jwt.issuedAt = deserializeZonedDateTime(iat);
			jsonObject.remove("iat");
		}

		jwt.issuer = jsonObject.getString("iss");
		jsonObject.remove("iss");

		final long nbf = jsonObject.getLong("nbf");
		if (nbf != 0) {
			jwt.notBefore = deserializeZonedDateTime(nbf);
			jsonObject.remove("nbf");
		}

		jwt.subject =jsonObject.getString("sub");
		jsonObject.remove("sub");

		jwt.uniqueId = jsonObject.getString("jti");
		jsonObject.remove("jti");

		jwt.otherClaims = jsonObject;
		return jwt;
	}

	private static Header readHeader(JsonObject jsonObject) {
		final Header header = new Header();
		final String algName = jsonObject.getString("alg");
		if (algName != null) {
			header.algorithm = Algorithm.valueOf(algName);
			jsonObject.remove("alg");
		}
		final String typ = jsonObject.getString("typ");
		header.type = Type.JWT;
		if (typ != null) {
			header.type = Type.valueOf(typ);
			jsonObject.remove("typ");
		}
		for (String propName : jsonObject.keySet()) {
			final String propValue = jsonObject.getString(propName);
			if (propValue != null) {
				header.set(propName, propValue);
			}
		}
		return header;
	}

	public byte[] writeValueAsBytes(Object value) {
		return writeValueAsString(value).getBytes(StandardCharsets.UTF_8);
	}

	public String writeValueAsString(Object value) {
		final JsonObject jsonObject = writeValueAsJsonObject(value);
		return JsonWriter.string(jsonObject);
	}

	private static JsonObject writeValueAsJsonObject(Object value) {
		final JsonObject jsonObject;
		if (value instanceof Header) {
			jsonObject = writeHeader((Header) value);
		} else if (value instanceof JWT) {
			jsonObject = writeJWT((JWT) value);
		} else if (value instanceof JSONWebKey) {
			jsonObject = writeJSONWebKey((JSONWebKey) value);

		} else if (value instanceof Map) {
			jsonObject = new JsonObject((Map) value);
		} else {
			throw new UnsupportedOperationException("unsupported object type " + value.getClass().getSimpleName());
		}
		return jsonObject;
	}

	private static JsonObject writeJSONWebKey(JSONWebKey jsonWebKey) {
		JsonObject jsonObject = new JsonObject();
		if (jsonWebKey.alg != null) {
			jsonObject.put("alg", jsonWebKey.alg.name());
		}
		if (jsonWebKey.crv != null) {
			jsonObject.put("crv", jsonWebKey.crv);
		}
		if (jsonWebKey.d != null) {
			jsonObject.put("d", jsonWebKey.d);
		}
		if (jsonWebKey.dp != null) {
			jsonObject.put("dp", jsonWebKey.dp);
		}
		if (jsonWebKey.dq != null) {
			jsonObject.put("dq", jsonWebKey.dq);
		}
		if (jsonWebKey.e != null) {
			jsonObject.put("e", jsonWebKey.e);
		}
		if (jsonWebKey.kid != null) {
			jsonObject.put("kid", jsonWebKey.kid);
		}
		if (jsonWebKey.kty != null) {
			jsonObject.put("kty", jsonWebKey.kty.name());
		}
		if (jsonWebKey.n != null) {
			jsonObject.put("n", jsonWebKey.n);
		}
		if (jsonWebKey.p != null) {
			jsonObject.put("p", jsonWebKey.p);
		}
		if (jsonWebKey.q != null) {
			jsonObject.put("q", jsonWebKey.q);
		}
		if (jsonWebKey.qi != null) {
			jsonObject.put("qi", jsonWebKey.qi);
		}
		if (jsonWebKey.use != null) {
			jsonObject.put("use", jsonWebKey.use);
		}
		if (jsonWebKey.x != null) {
			jsonObject.put("x", jsonWebKey.x);
		}
		if (jsonWebKey.x5c != null) {
			jsonObject.put("x5c", jsonWebKey.x5c);
		}
		if (jsonWebKey.x5t != null) {
			jsonObject.put("x5t", jsonWebKey.x5t);
		}
		if (jsonWebKey.x5t_256 != null) {
			jsonObject.put("x5t#S256", jsonWebKey.x5t_256);
		}
		if (jsonWebKey.y != null) {
			jsonObject.put("y", jsonWebKey.y);
		}

		for (Map.Entry<String, Object> entry : jsonWebKey.other.entrySet()) {
			jsonObject.put(entry.getKey(), entry.getValue());
		}
		return jsonObject;
	}

	private static JsonObject writeJWT(JWT jwt) {
		JsonObject jsonObject = new JsonObject();
		if (jwt.audience != null) {
			jsonObject.put("aud", jwt.audience);
		}

		if (jwt.subject != null) {
			jsonObject.put("sub", jwt.subject);
		}

		if (jwt.expiration != null) {
			jsonObject.put("exp", jwt.expiration.toEpochSecond());
		}

		if (jwt.issuedAt != null) {
			jsonObject.put("iat", jwt.issuedAt.toEpochSecond());
		}

		if (jwt.notBefore != null) {
			jsonObject.put("nbf", jwt.notBefore.toEpochSecond());
		}

		if (jwt.issuer != null) {
			jsonObject.put("iss", jwt.issuer);
		}

		if (jwt.uniqueId != null) {
			jsonObject.put("jti", jwt.uniqueId);
		}

		for (Map.Entry<String, Object> entry : jwt.otherClaims.entrySet()) {
			jsonObject.put(entry.getKey(), entry.getValue());
		}
		return jsonObject;
	}

	private static JsonObject writeHeader(Header header) {
		JsonObject jsonObject = new JsonObject();
		if (header.algorithm != null) {
			jsonObject.put("alg", header.algorithm.name());
			header.properties.remove("alg");
		}
		if (header.type != null) {
			jsonObject.put("typ", header.type.name());
			header.properties.remove("typ");
		}
		for (Map.Entry<String, String> entry : header.properties.entrySet()) {
			jsonObject.put(entry.getKey(), entry.getValue());
		}
		return jsonObject;
	}

	public byte[] prettyPrint(Object value) {
		final JsonObject jsonObject = writeValueAsJsonObject(value);
		return JsonWriter.string(jsonObject).getBytes(StandardCharsets.UTF_8);
	}

	static ZonedDateTime deserializeZonedDateTime(long value) {
		return Instant.ofEpochSecond(value).atZone(ZoneOffset.UTC);
	}
}
