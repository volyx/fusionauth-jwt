package io.fusionauth.perf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.json.MinimalJsonObjectMapper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/*

Benchmark                                                            Mode      Cnt    Score     Error   Units
JsonBenchmark.testDecodeJackson                                     thrpt        5  786,778 ± 269,645  ops/ms
JsonBenchmark.testDecodeMinimalJson                                 thrpt        5  225,488 ±  79,774  ops/ms
JsonBenchmark.testEncodeJackson                                     thrpt        5   87,847 ±  43,182  ops/ms
JsonBenchmark.testEncodeMinimalJson                                 thrpt        5   92,763 ±  38,017  ops/ms
JsonBenchmark.testDecodeJackson                                      avgt        5    0,001 ±   0,001   ms/op
JsonBenchmark.testDecodeMinimalJson                                  avgt        5    0,004 ±   0,001   ms/op
JsonBenchmark.testEncodeJackson                                      avgt        5    0,009 ±   0,003   ms/op
JsonBenchmark.testEncodeMinimalJson                                  avgt        5    0,009 ±   0,001   ms/op
JsonBenchmark.testDecodeJackson                                    sample  1416324    0,001 ±   0,001   ms/op
JsonBenchmark.testDecodeJackson:testDecodeJackson·p0.00            sample             0,001             ms/op
JsonBenchmark.testDecodeJackson:testDecodeJackson·p0.50            sample             0,001             ms/op
JsonBenchmark.testDecodeJackson:testDecodeJackson·p0.90            sample             0,001             ms/op
JsonBenchmark.testDecodeJackson:testDecodeJackson·p0.95            sample             0,002             ms/op
JsonBenchmark.testDecodeJackson:testDecodeJackson·p0.99            sample             0,002             ms/op
JsonBenchmark.testDecodeJackson:testDecodeJackson·p0.999           sample             0,032             ms/op
JsonBenchmark.testDecodeJackson:testDecodeJackson·p0.9999          sample             0,369             ms/op
JsonBenchmark.testDecodeJackson:testDecodeJackson·p1.00            sample             6,554             ms/op
JsonBenchmark.testDecodeMinimalJson                                sample  1443125    0,005 ±   0,001   ms/op
JsonBenchmark.testDecodeMinimalJson:testDecodeMinimalJson·p0.00    sample             0,003             ms/op
JsonBenchmark.testDecodeMinimalJson:testDecodeMinimalJson·p0.50    sample             0,003             ms/op
JsonBenchmark.testDecodeMinimalJson:testDecodeMinimalJson·p0.90    sample             0,006             ms/op
JsonBenchmark.testDecodeMinimalJson:testDecodeMinimalJson·p0.95    sample             0,006             ms/op
JsonBenchmark.testDecodeMinimalJson:testDecodeMinimalJson·p0.99    sample             0,012             ms/op
JsonBenchmark.testDecodeMinimalJson:testDecodeMinimalJson·p0.999   sample             0,094             ms/op
JsonBenchmark.testDecodeMinimalJson:testDecodeMinimalJson·p0.9999  sample             0,902             ms/op
JsonBenchmark.testDecodeMinimalJson:testDecodeMinimalJson·p1.00    sample            15,434             ms/op
JsonBenchmark.testEncodeJackson                                    sample  1430536    0,009 ±   0,001   ms/op
JsonBenchmark.testEncodeJackson:testEncodeJackson·p0.00            sample             0,008             ms/op
JsonBenchmark.testEncodeJackson:testEncodeJackson·p0.50            sample             0,008             ms/op
JsonBenchmark.testEncodeJackson:testEncodeJackson·p0.90            sample             0,009             ms/op
JsonBenchmark.testEncodeJackson:testEncodeJackson·p0.95            sample             0,009             ms/op
JsonBenchmark.testEncodeJackson:testEncodeJackson·p0.99            sample             0,017             ms/op
JsonBenchmark.testEncodeJackson:testEncodeJackson·p0.999           sample             0,062             ms/op
JsonBenchmark.testEncodeJackson:testEncodeJackson·p0.9999          sample             0,804             ms/op
JsonBenchmark.testEncodeJackson:testEncodeJackson·p1.00            sample             1,997             ms/op
JsonBenchmark.testEncodeMinimalJson                                sample  1485701    0,009 ±   0,001   ms/op
JsonBenchmark.testEncodeMinimalJson:testEncodeMinimalJson·p0.00    sample             0,007             ms/op
JsonBenchmark.testEncodeMinimalJson:testEncodeMinimalJson·p0.50    sample             0,008             ms/op
JsonBenchmark.testEncodeMinimalJson:testEncodeMinimalJson·p0.90    sample             0,008             ms/op
JsonBenchmark.testEncodeMinimalJson:testEncodeMinimalJson·p0.95    sample             0,010             ms/op
JsonBenchmark.testEncodeMinimalJson:testEncodeMinimalJson·p0.99    sample             0,018             ms/op
JsonBenchmark.testEncodeMinimalJson:testEncodeMinimalJson·p0.999   sample             0,082             ms/op
JsonBenchmark.testEncodeMinimalJson:testEncodeMinimalJson·p0.9999  sample             0,813             ms/op
JsonBenchmark.testEncodeMinimalJson:testEncodeMinimalJson·p1.00    sample            39,125             ms/op
JsonBenchmark.testDecodeJackson                                        ss           129,521             ms/op
JsonBenchmark.testDecodeMinimalJson                                    ss             7,669             ms/op
JsonBenchmark.testEncodeJackson                                        ss           137,199             ms/op
JsonBenchmark.testEncodeMinimalJson                                    ss            17,252             ms/op

Process finished with exit code 0



 */

@State(Scope.Thread)
public class JsonBenchmark {

	byte[] json;

	JWT jwt;

	ObjectMapper objectMapper;
	MinimalJsonObjectMapper minimalJsonObjectMapper;

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
				.include(JsonBenchmark.class.getSimpleName())
				.timeUnit(TimeUnit.MICROSECONDS)
				.mode(Mode.All)
				.forks(1)
				.build();

		new Runner(opt).run();
	}


	@Setup
	public void setup() {

		String jsonString = "{\n" +
				"  \"alg\" : \"ES256\",\n" +
				"  \"crv\" : \"P-256\",\n" +
				"  \"d\" : \"y3F4UN_uqaNn4o4G8UHT3Gq6Ab_2CdjFeoDpLREcGaA\",\n" +
				"  \"kty\" : \"EC\",\n" +
				"  \"use\" : \"sig\",\n" +
				"  \"x\" : \"axfR8uEsQkf4vOblY6RA8ncDfYEt6zOg9KE5RdiYwpY\",\n" +
				"  \"y\" : \"T-NC4v4af5uO5-tKfA-eFivOM1drMV7Oy7ZAaDe_UfU\"\n" +
				"}";

		json = jsonString.getBytes(StandardCharsets.UTF_8);

		jwt = new JWT()
				.setAudience(Arrays.asList("www.acme.com", "www.vandelayindustries.com"))
				.setExpiration(ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(60).truncatedTo(ChronoUnit.SECONDS))
				.setIssuedAt(ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS))
				.setIssuer("www.inversoft.com")
				.setNotBefore(ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5).truncatedTo(ChronoUnit.SECONDS))
				.setUniqueId(UUID.randomUUID().toString())
				.setSubject("123456789")
				.addClaim("foo", "bar")
				.addClaim("timestamp", 1476062602926L)
				.addClaim("bigInteger", new BigInteger("100000000000000000000000000000000000000000000000000000000000000000000000000000000"))
				.addClaim("bigDecimal", new BigDecimal("11.2398732934908570987534209857423098743209857"))
				.addClaim("double", 3.14d)
				.addClaim("float", 3.14f)
				.addClaim("meaningOfLife", 42)
				.addClaim("bar", Arrays.asList("bing", "bam", "boo"))
				.addClaim("object", Collections.singletonMap("nested", Collections.singletonMap("foo", "bar")))
				.addClaim("www.inversoft.com/claims/is_admin", true);

		objectMapper = new ObjectMapper();
		minimalJsonObjectMapper = new MinimalJsonObjectMapper();

	}

	@Benchmark
	public void testDecodeJackson(Blackhole bh) throws IOException {
		final JWT decoded = objectMapper.readValue(json, JWT.class);
		bh.consume(decoded);
	}

	@Benchmark
	public void testEncodeJackson(Blackhole bh) throws JsonProcessingException {
		final byte[] encoded = objectMapper.writeValueAsBytes(jwt);
		bh.consume(encoded);
	}

	@Benchmark
	public void testDecodeMinimalJson(Blackhole bh) throws IOException {
		final JWT decoded = minimalJsonObjectMapper.readValue(json, JWT.class);
		bh.consume(decoded);
	}

	@Benchmark
	public void testEncodeMinimalJson(Blackhole bh) throws JsonProcessingException {
		final byte[] encoded = minimalJsonObjectMapper.writeValueAsBytes(jwt);
		bh.consume(encoded);
	}


}
