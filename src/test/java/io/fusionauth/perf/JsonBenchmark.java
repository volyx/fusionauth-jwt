package io.fusionauth.perf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.json.JacksonModule;
import io.fusionauth.jwt.json.MinimalJsonObjectMapper;
import io.fusionauth.jwt.json.NanoJsonObjectMapper;
import io.fusionauth.jwt.json.PlainObjectMapper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
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
Benchmark                                    (serializer)    Mode      Cnt       Score    Error   Units
JsonBenchmark.testDecode                          Jackson   thrpt        5       0,800 ±  0,329  ops/us
JsonBenchmark.testDecode                      MinimalJson   thrpt        5       0,247 ±  0,093  ops/us
JsonBenchmark.testDecode                         NanoJson   thrpt        5       0,063 ±  0,062  ops/us
JsonBenchmark.testDecode                            Plain   thrpt        5       0,538 ±  0,229  ops/us
JsonBenchmark.testDecode                             None   thrpt        5       6,863 ±  1,322  ops/us
JsonBenchmark.testEncode                          Jackson   thrpt        5       0,235 ±  0,040  ops/us
JsonBenchmark.testEncode                      MinimalJson   thrpt        5       0,097 ±  0,013  ops/us
JsonBenchmark.testEncode                         NanoJson   thrpt        5       0,088 ±  0,102  ops/us
JsonBenchmark.testEncode                            Plain   thrpt        5       0,157 ±  0,077  ops/us
JsonBenchmark.testEncode                             None   thrpt        5      24,158 ± 21,829  ops/us
JsonBenchmark.testDecode                          Jackson    avgt        5       1,189 ±  0,222   us/op
JsonBenchmark.testDecode                      MinimalJson    avgt        5       4,250 ±  2,976   us/op
JsonBenchmark.testDecode                         NanoJson    avgt        5      14,250 ±  6,014   us/op
JsonBenchmark.testDecode                            Plain    avgt        5       1,955 ±  2,875   us/op
JsonBenchmark.testDecode                             None    avgt        5       0,137 ±  0,033   us/op
JsonBenchmark.testEncode                          Jackson    avgt        5       5,854 ± 10,353   us/op
JsonBenchmark.testEncode                      MinimalJson    avgt        5      10,302 ± 10,342   us/op
JsonBenchmark.testEncode                         NanoJson    avgt        5       9,826 ±  7,726   us/op
JsonBenchmark.testEncode                            Plain    avgt        5       5,453 ±  0,905   us/op
JsonBenchmark.testEncode                             None    avgt        5       0,043 ±  0,013   us/op
JsonBenchmark.testDecode                          Jackson  sample  1201091       1,692 ±  0,097   us/op
JsonBenchmark.testDecode:testDecode·p0.00         Jackson  sample                0,957            us/op
JsonBenchmark.testDecode:testDecode·p0.50         Jackson  sample                1,024            us/op
JsonBenchmark.testDecode:testDecode·p0.90         Jackson  sample                1,880            us/op
JsonBenchmark.testDecode:testDecode·p0.95         Jackson  sample                2,184            us/op
JsonBenchmark.testDecode:testDecode·p0.99         Jackson  sample                4,200            us/op
JsonBenchmark.testDecode:testDecode·p0.999        Jackson  sample               78,336            us/op
JsonBenchmark.testDecode:testDecode·p0.9999       Jackson  sample              786,226            us/op
JsonBenchmark.testDecode:testDecode·p1.00         Jackson  sample            26869,760            us/op
JsonBenchmark.testDecode                      MinimalJson  sample  1248740       6,599 ±  0,489   us/op
JsonBenchmark.testDecode:testDecode·p0.00     MinimalJson  sample                2,860            us/op
JsonBenchmark.testDecode:testDecode·p0.50     MinimalJson  sample                3,360            us/op
JsonBenchmark.testDecode:testDecode·p0.90     MinimalJson  sample                5,472            us/op
JsonBenchmark.testDecode:testDecode·p0.95     MinimalJson  sample                5,872            us/op
JsonBenchmark.testDecode:testDecode·p0.99     MinimalJson  sample               17,280            us/op
JsonBenchmark.testDecode:testDecode·p0.999    MinimalJson  sample              410,245            us/op
JsonBenchmark.testDecode:testDecode·p0.9999   MinimalJson  sample             4738,070            us/op
JsonBenchmark.testDecode:testDecode·p1.00     MinimalJson  sample            90177,536            us/op
JsonBenchmark.testDecode                         NanoJson  sample   925259      14,588 ±  0,234   us/op
JsonBenchmark.testDecode:testDecode·p0.00        NanoJson  sample                7,344            us/op
JsonBenchmark.testDecode:testDecode·p0.50        NanoJson  sample               10,256            us/op
JsonBenchmark.testDecode:testDecode·p0.90        NanoJson  sample               15,440            us/op
JsonBenchmark.testDecode:testDecode·p0.95        NanoJson  sample               21,440            us/op
JsonBenchmark.testDecode:testDecode·p0.99        NanoJson  sample               59,802            us/op
JsonBenchmark.testDecode:testDecode·p0.999       NanoJson  sample              663,552            us/op
JsonBenchmark.testDecode:testDecode·p0.9999      NanoJson  sample             2514,731            us/op
JsonBenchmark.testDecode:testDecode·p1.00        NanoJson  sample            20054,016            us/op
JsonBenchmark.testDecode                            Plain  sample  1526500       2,457 ±  0,138   us/op
JsonBenchmark.testDecode:testDecode·p0.00           Plain  sample                1,480            us/op
JsonBenchmark.testDecode:testDecode·p0.50           Plain  sample                1,572            us/op
JsonBenchmark.testDecode:testDecode·p0.90           Plain  sample                2,788            us/op
JsonBenchmark.testDecode:testDecode·p0.95           Plain  sample                3,184            us/op
JsonBenchmark.testDecode:testDecode·p0.99           Plain  sample                6,416            us/op
JsonBenchmark.testDecode:testDecode·p0.999          Plain  sample               98,304            us/op
JsonBenchmark.testDecode:testDecode·p0.9999         Plain  sample              891,597            us/op
JsonBenchmark.testDecode:testDecode·p1.00           Plain  sample            54001,664            us/op
JsonBenchmark.testDecode                             None  sample  1371906       0,821 ±  0,246   us/op
JsonBenchmark.testDecode:testDecode·p0.00            None  sample                0,139            us/op
JsonBenchmark.testDecode:testDecode·p0.50            None  sample                0,163            us/op
JsonBenchmark.testDecode:testDecode·p0.90            None  sample                0,297            us/op
JsonBenchmark.testDecode:testDecode·p0.95            None  sample                0,311            us/op
JsonBenchmark.testDecode:testDecode·p0.99            None  sample                0,554            us/op
JsonBenchmark.testDecode:testDecode·p0.999           None  sample               19,744            us/op
JsonBenchmark.testDecode:testDecode·p0.9999          None  sample              801,597            us/op
JsonBenchmark.testDecode:testDecode·p1.00            None  sample            38010,880            us/op
JsonBenchmark.testEncode                          Jackson  sample  1411865       4,719 ±  0,049   us/op
JsonBenchmark.testEncode:testEncode·p0.00         Jackson  sample                3,468            us/op
JsonBenchmark.testEncode:testEncode·p0.50         Jackson  sample                3,584            us/op
JsonBenchmark.testEncode:testEncode·p0.90         Jackson  sample                5,936            us/op
JsonBenchmark.testEncode:testEncode·p0.95         Jackson  sample                7,056            us/op
JsonBenchmark.testEncode:testEncode·p0.99         Jackson  sample               15,568            us/op
JsonBenchmark.testEncode:testEncode·p0.999        Jackson  sample              128,913            us/op
JsonBenchmark.testEncode:testEncode·p0.9999       Jackson  sample              849,538            us/op
JsonBenchmark.testEncode:testEncode·p1.00         Jackson  sample             4415,488            us/op
JsonBenchmark.testEncode                      MinimalJson  sample  1263047      10,270 ±  0,091   us/op
JsonBenchmark.testEncode:testEncode·p0.00     MinimalJson  sample                7,064            us/op
JsonBenchmark.testEncode:testEncode·p0.50     MinimalJson  sample                7,632            us/op
JsonBenchmark.testEncode:testEncode·p0.90     MinimalJson  sample               13,536            us/op
JsonBenchmark.testEncode:testEncode·p0.95     MinimalJson  sample               15,920            us/op
JsonBenchmark.testEncode:testEncode·p0.99     MinimalJson  sample               40,448            us/op
JsonBenchmark.testEncode:testEncode·p0.999    MinimalJson  sample              265,728            us/op
JsonBenchmark.testEncode:testEncode·p0.9999   MinimalJson  sample             1106,095            us/op
JsonBenchmark.testEncode:testEncode·p1.00     MinimalJson  sample            10371,072            us/op
JsonBenchmark.testEncode                         NanoJson  sample  1378816       9,582 ±  0,330   us/op
JsonBenchmark.testEncode:testEncode·p0.00        NanoJson  sample                7,032            us/op
JsonBenchmark.testEncode:testEncode·p0.50        NanoJson  sample                7,856            us/op
JsonBenchmark.testEncode:testEncode·p0.90        NanoJson  sample                9,344            us/op
JsonBenchmark.testEncode:testEncode·p0.95        NanoJson  sample               12,928            us/op
JsonBenchmark.testEncode:testEncode·p0.99        NanoJson  sample               23,232            us/op
JsonBenchmark.testEncode:testEncode·p0.999       NanoJson  sample              164,911            us/op
JsonBenchmark.testEncode:testEncode·p0.9999      NanoJson  sample             1223,867            us/op
JsonBenchmark.testEncode:testEncode·p1.00        NanoJson  sample           116391,936            us/op
JsonBenchmark.testEncode                            Plain  sample  1085856       6,361 ±  0,711   us/op
JsonBenchmark.testEncode:testEncode·p0.00           Plain  sample                4,512            us/op
JsonBenchmark.testEncode:testEncode·p0.50           Plain  sample                4,848            us/op
JsonBenchmark.testEncode:testEncode·p0.90           Plain  sample                5,688            us/op
JsonBenchmark.testEncode:testEncode·p0.95           Plain  sample                8,192            us/op
JsonBenchmark.testEncode:testEncode·p0.99           Plain  sample               15,303            us/op
JsonBenchmark.testEncode:testEncode·p0.999          Plain  sample              122,752            us/op
JsonBenchmark.testEncode:testEncode·p0.9999         Plain  sample             1221,105            us/op
JsonBenchmark.testEncode:testEncode·p1.00           Plain  sample           228589,568            us/op
JsonBenchmark.testEncode                             None  sample  1201647       0,136 ±  0,016   us/op
JsonBenchmark.testEncode:testEncode·p0.00            None  sample                0,027            us/op
JsonBenchmark.testEncode:testEncode·p0.50            None  sample                0,073            us/op
JsonBenchmark.testEncode:testEncode·p0.90            None  sample                0,105            us/op
JsonBenchmark.testEncode:testEncode·p0.95            None  sample                0,133            us/op
JsonBenchmark.testEncode:testEncode·p0.99            None  sample                0,288            us/op
JsonBenchmark.testEncode:testEncode·p0.999           None  sample                9,904            us/op
JsonBenchmark.testEncode:testEncode·p0.9999          None  sample               61,941            us/op
JsonBenchmark.testEncode:testEncode·p1.00            None  sample             2854,912            us/op
JsonBenchmark.testDecode                          Jackson      ss           118276,270            us/op
JsonBenchmark.testDecode                      MinimalJson      ss             8362,015            us/op
JsonBenchmark.testDecode                         NanoJson      ss            21499,819            us/op
JsonBenchmark.testDecode                            Plain      ss             3517,849            us/op
JsonBenchmark.testDecode                             None      ss              100,837            us/op
JsonBenchmark.testEncode                          Jackson      ss           106437,109            us/op
JsonBenchmark.testEncode                      MinimalJson      ss            14163,948            us/op
JsonBenchmark.testEncode                         NanoJson      ss            20346,964            us/op
JsonBenchmark.testEncode                            Plain      ss             1746,693            us/op
JsonBenchmark.testEncode                             None      ss               40,539            us/op

 */

public class JsonBenchmark {

	@State( Scope.Benchmark )
	public static class Parameters {

		@Param( {
				"Jackson",
				"MinimalJson",
				"NanoJson",
				"Plain",
				"None"
		} )
		public String serializer;


		byte[] json;

		JWT jwt;

		ObjectMapper objectMapper;
		MinimalJsonObjectMapper minimalJsonObjectMapper;
		NanoJsonObjectMapper nanoJsonObjectMapper;
		PlainObjectMapper plainObjectMapper;

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

			objectMapper = new ObjectMapper()
					.setSerializationInclusion(JsonInclude.Include.NON_NULL)
					.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
					.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
					.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
					.configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true)
					.registerModule(new JacksonModule());

			minimalJsonObjectMapper = new MinimalJsonObjectMapper();
			nanoJsonObjectMapper = new NanoJsonObjectMapper();
			plainObjectMapper = new PlainObjectMapper();

		}

		@TearDown
		public void tearDown() {
			json = null;
			jwt = null;
			objectMapper = null;
			minimalJsonObjectMapper = null;
			nanoJsonObjectMapper = null;
			plainObjectMapper = null;
		}
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
				.include(JsonBenchmark.class.getSimpleName())
				.timeUnit(TimeUnit.MICROSECONDS)
				.mode(Mode.All)
				.forks(1)
				.build();

		new Runner(opt).run();
	}



	@Benchmark
	public void testDecode(Parameters parameters, Blackhole bh) throws IOException {
		JWT decoded = null;
		switch (parameters.serializer) {
			case "Jackson":
				decoded = parameters.objectMapper.readValue(parameters.json, JWT.class);
				break;
			case "MinimalJson":
				decoded = parameters.minimalJsonObjectMapper.readValue(parameters.json, JWT.class);
				break;
			case "NanoJson":
				decoded = parameters.nanoJsonObjectMapper.readValue(parameters.json, JWT.class);
				break;
			case "Plain":
				decoded = parameters.plainObjectMapper.readValue(parameters.json, JWT.class);
				break;
			case "None":
				decoded = new JWT();
				decoded.audience = System.currentTimeMillis() + "";
				break;
		}

		bh.consume(decoded);
	}

	@Benchmark
	public void testEncode(Parameters parameters, Blackhole bh) throws JsonProcessingException {
		byte[] encoded = new byte[0];
		switch (parameters.serializer) {
			case "Jackson":
				encoded = parameters.objectMapper.writeValueAsBytes(parameters.jwt);
				break;
			case "MinimalJson":
				encoded = parameters.minimalJsonObjectMapper.writeValueAsBytes(parameters.jwt);
				break;
			case "NanoJson":
				encoded = parameters.nanoJsonObjectMapper.writeValueAsBytes(parameters.jwt);
				break;
			case "Plain":
				encoded = parameters.plainObjectMapper.writeValueAsBytes(parameters.jwt);
				break;
			case "None":
				encoded = new byte[100];
				break;
		}

		bh.consume(encoded);
	}
}
