package io.fusionauth.jwt.json;

import io.fusionauth.jwks.domain.JSONWebKey;
import io.fusionauth.jwt.domain.Algorithm;
import io.fusionauth.jwt.domain.Header;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.domain.Type;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static io.fusionauth.jwt.json.NanoJsonObjectMapper.deserializeZonedDateTime;

public class PlainObjectMapper {
	public <T> T readValue(byte[] bytes, Class<T> value) {

		if (value.equals(Header.class)) {
			return (T) readHeader(bytes);
		} else if (value.equals(JWT.class)) {
			return (T) readJWT(bytes);
		}
		if (value.equals(Map.class)) {
			try {
				final JsonReader jsonReader = new JsonReader(bytes);
				return (T) jsonReader.readMap();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new UnsupportedOperationException("unsupported object type " + value.getSimpleName());
		}
	}

	private static JWT readJWT(byte[] bytes) {
		final JsonReader jsonReader = new JsonReader(bytes);
		final JWT jwt = new JWT();
		try {

			jsonReader.expect('{', "Expected map");
			for (boolean needComma = false; jsonReader.skipWhitespace() != '}'; needComma = true) {
				if (needComma) {
					jsonReader.expect(',', "Unexpected end of map");
					jsonReader.skipWhitespace();
				}

				final String key = jsonReader.readString();
				jsonReader.skipWhitespace();
				jsonReader.expect(':', "Expected key-value pair");
				jsonReader.skipWhitespace();


				switch (key) {
					case "aud":
						jwt.audience = jsonReader.readObject();;
						break;
					case "exp":
						final long exp = jsonReader.readLong();
						if (exp != 0) {
							jwt.expiration = deserializeZonedDateTime(exp);
						}
						break;
					case "iat":
						final long iat = jsonReader.readLong();
						if (iat != 0) {
							jwt.issuedAt = deserializeZonedDateTime(iat);
						}
						break;
					case "iss":
						jwt.issuer = jsonReader.readString();
						break;
					case "nbf":
						final long nbf = jsonReader.readLong();
						if (nbf != 0) {
							jwt.notBefore = deserializeZonedDateTime(nbf);
						}
						break;
					case "sub":
						jwt.subject = jsonReader.readString();
						break;
					case "jti":
						jwt.uniqueId = jsonReader.readString();
						break;
					default:
						jwt.otherClaims.put(key, jsonReader.readObject());
				}
			}
			jsonReader.read();


		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return jwt;
	}

	private static Header readHeader(byte[] bytes) {
		final Header header = new Header();
		final JsonReader jsonReader = new JsonReader(bytes);
		try {

			jsonReader.expect('{', "Expected map");
			for (boolean needComma = false; jsonReader.skipWhitespace() != '}'; needComma = true) {
				if (needComma) {
					jsonReader.expect(',', "Unexpected end of map");
					jsonReader.skipWhitespace();
				}

				String key = jsonReader.readString();
				jsonReader.skipWhitespace();
				jsonReader.expect(':', "Expected key-value pair");
				jsonReader.skipWhitespace();
				final Object value = jsonReader.readObject();

				switch (key) {
					case "alg":
						header.algorithm = Algorithm.valueOf(value.toString());
						break;
					case "typ":
						header.type = Type.valueOf(value.toString());
						break;
					default:
						header.properties.put(key, value.toString());
				}
			}
			jsonReader.read();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return header;
	}

	public byte[] prettyPrint(Object object) {
		return new byte[0];
	}

	public byte[] writeValueAsBytes(Object value) {
		return writeValueAsString(value).getBytes(StandardCharsets.UTF_8);
	}

	public String writeValueAsString(Object value) {
		if (value instanceof Header) {
			return writeHeader((Header) value);
		} else if (value instanceof JWT) {
			return writeJWT((JWT) value);
		} else if (value instanceof JSONWebKey) {
			return writeJSONWebKey((JSONWebKey) value);
		} else if (value instanceof Map) {
			return writeMap((Map<String, Object>) value);
		} else {
			throw new UnsupportedOperationException("unsupported object type " + value.getClass().getSimpleName());
		}
	}

	private static void writeMap(Map<String, Object> value, StringBuilder sb) {
		for (Map.Entry<String, Object> entry : value.entrySet()) {

			if (entry.getValue() instanceof String) {
				sb.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"").append(',');
			}
			if (entry.getValue() instanceof List) {
				sb.append("\"").append(entry.getKey()).append("\":");
				writeList((List<String>) entry.getValue(), sb);
				sb.append(',');
			}
			if (entry.getValue() instanceof Map) {
				final Map<String, Object> nested = (Map<String, Object>) entry.getValue();
				sb.append("\"").append(entry.getKey()).append("\":");
				sb.append('{');
				writeMap(nested, sb);
				sb.append('}');
				sb.append(',');
			}

			if (entry.getValue() instanceof Boolean) {
				sb.append("\"").append(entry.getKey()).append("\":").append(entry.getValue()).append(',');
			}
			if (entry.getValue() instanceof Integer) {
				final Integer entryInt = (Integer) entry.getValue();
				sb.append("\"").append(entry.getKey()).append("\":").append(entryInt).append(',');
			}
			if (entry.getValue() instanceof Float) {
				final Float entryFloat = (Float) entry.getValue();
				sb.append("\"").append(entry.getKey()).append("\":").append(BigDecimal.valueOf(entryFloat)).append(',');
			}
			if (entry.getValue() instanceof Double) {
				final Double entryDouble = (Double) entry.getValue();
				sb.append("\"").append(entry.getKey()).append("\":").append(BigDecimal.valueOf(entryDouble)).append(',');
			}
			if (entry.getValue() instanceof BigInteger) {
				final BigInteger bigInteger = (BigInteger) entry.getValue();
				sb.append("\"").append(entry.getKey()).append("\":").append(bigInteger.toString()).append(',');
			}
			if (entry.getValue() instanceof BigDecimal) {
				final BigDecimal bigDecimal = (BigDecimal) entry.getValue();
				sb.append("\"").append(entry.getKey()).append("\":").append(bigDecimal.toString()).append(',');
			}
		}

		if (sb.charAt(sb.length() - 1) == ',')
			sb.deleteCharAt(sb.length() - 1);

	}

	private static String writeMap(Map<String, Object> value) {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		writeMap(value, sb);
		sb.append('}');
		return sb.toString();
	}

	private static String writeJSONWebKey(JSONWebKey jsonWebKey) {
		StringBuilder sb = new StringBuilder();
		sb.append('{');

		if (jsonWebKey.alg != null) {
			sb.append("\"alg\":\"").append(jsonWebKey.alg.name()).append("\"").append(",");
		}
		if (jsonWebKey.crv != null) {
			sb.append("\"crv\":\"").append(jsonWebKey.crv).append("\"").append(",");
		}
		if (jsonWebKey.d != null) {
			sb.append("\"d\":\"").append(jsonWebKey.d).append("\"").append(",");
		}
		if (jsonWebKey.dp != null) {
			sb.append("\"dp\":\"").append(jsonWebKey.dp).append("\"").append(",");
		}
		if (jsonWebKey.dq != null) {
			sb.append("\"dq\":\"").append(jsonWebKey.dq).append("\"").append(",");
		}
		if (jsonWebKey.e != null) {
			sb.append("\"e\":\"").append(jsonWebKey.e).append("\"").append(",");
		}
		if (jsonWebKey.kid != null) {
			sb.append("\"kid\":\"").append(jsonWebKey.kid).append("\"").append(",");
		}
		if (jsonWebKey.kty != null) {
			sb.append("\"kty\":\"").append(jsonWebKey.kty).append("\"").append(",");
		}
		if (jsonWebKey.n != null) {
			sb.append("\"n\":\"").append(jsonWebKey.n).append("\"").append(",");
		}
		if (jsonWebKey.p != null) {
			sb.append("\"p\":\"").append(jsonWebKey.p).append("\"").append(",");
		}
		if (jsonWebKey.q != null) {
			sb.append("\"q\":\"").append(jsonWebKey.q).append("\"").append(",");
		}
		if (jsonWebKey.qi != null) {
			sb.append("\"qi\":\"").append(jsonWebKey.qi).append("\"").append(",");
		}
		if (jsonWebKey.use != null) {
			sb.append("\"use\":\"").append(jsonWebKey.use).append("\"").append(",");
		}
		if (jsonWebKey.x != null) {
			sb.append("\"x\":\"").append(jsonWebKey.x).append("\"").append(",");
		}
		if (jsonWebKey.x5c != null) {
			sb.append("\"x5c\":\"").append(jsonWebKey.x5c).append("\"").append(",");
		}
		if (jsonWebKey.x5t != null) {
			sb.append("\"x5t\":\"").append(jsonWebKey.x5t).append("\"").append(",");
		}
		if (jsonWebKey.x5t_256 != null) {
			sb.append("\"x5t#S256\":\"").append(jsonWebKey.x5t_256).append("\"").append(",");
		}
		if (jsonWebKey.y != null) {
			sb.append("\"y\":\"").append(jsonWebKey.y).append("\"").append(",");
		}

		writeMap(jsonWebKey.other, sb);

		if (sb.charAt(sb.length() - 1) == ',')
			sb.deleteCharAt(sb.length() - 1);

		sb.append('}');
		return sb.toString();
	}

	private static String writeJWT(JWT jwt) {
		StringBuilder sb = new StringBuilder();
		sb.append('{');

		if (jwt.audience != null) {
			if (jwt.audience instanceof String)
				sb.append("\"aud\":\"").append(jwt.audience).append("\"").append(',');

			if (jwt.audience instanceof List) {
				sb.append("\"aud\":");
				writeList((List<String>) jwt.audience, sb);
				sb.append(',');
			}
		}

		if (jwt.subject != null) {
			sb.append("\"sub\":\"").append(jwt.subject).append("\"").append(',');
		}

		if (jwt.expiration != null) {
			sb.append("\"exp\":").append(jwt.expiration.toEpochSecond()).append(',');
		}

		if (jwt.issuedAt != null) {
			sb.append("\"iat\":").append(jwt.issuedAt.toEpochSecond()).append(',');
		}

		if (jwt.notBefore != null) {
			sb.append("\"nbf\":").append(jwt.notBefore.toEpochSecond()).append(',');
		}

		if (jwt.issuer != null) {
			sb.append("\"iss\":\"").append(jwt.issuer).append("\"").append(',');
		}

		if (jwt.uniqueId != null) {
			sb.append("\"jti\":\"").append(jwt.uniqueId).append("\"").append(',');
		}

		writeMap(jwt.otherClaims, sb);

		if (sb.charAt(sb.length() - 1) == ',')
			sb.deleteCharAt(sb.length() - 1);

		sb.append('}');
		return sb.toString();
	}

	private static void writeList(List<String> list, StringBuilder sb) {
		sb.append("[");
		for (int i = 0; i < list.size(); i++) {
			final String aud = list.get(i);
			sb.append("\"").append(aud).append("\"");
			if (i != list.size() - 1) {
				sb.append(",");
			}
		}
		sb.append(']');
	}

	private static String writeHeader(Header header) {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		if (header.algorithm != null) {
			sb.append("\"alg\":\"").append(header.algorithm.name()).append("\"").append(',');
			header.properties.remove("alg");
		}
		if (header.type != null) {
			sb.append("\"typ\":\"").append(header.type.name()).append("\"").append(',');
			header.properties.remove("typ");
		}
		for (Map.Entry<String, String> entry : header.properties.entrySet()) {
			sb.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"").append(',');
		}
		if (sb.charAt(sb.length() - 1) == ',')
			sb.deleteCharAt(sb.length() - 1);

		sb.append('}');
		return sb.toString();
	}
}
