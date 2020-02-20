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
JsonBenchmark.testDecode                          Jackson   thrpt        5       0,975 ±  0,014  ops/us
JsonBenchmark.testDecode                      MinimalJson   thrpt        5       0,301 ±  0,007  ops/us
JsonBenchmark.testDecode                         NanoJson   thrpt        5       0,098 ±  0,004  ops/us
JsonBenchmark.testDecode                            Plain   thrpt        5       0,646 ±  0,004  ops/us
JsonBenchmark.testDecode                             None   thrpt        5       8,132 ±  0,151  ops/us
JsonBenchmark.testEncode                          Jackson   thrpt        5       0,113 ±  0,003  ops/us
JsonBenchmark.testEncode                      MinimalJson   thrpt        5       0,123 ±  0,001  ops/us
JsonBenchmark.testEncode                         NanoJson   thrpt        5       0,124 ±  0,001  ops/us
JsonBenchmark.testEncode                            Plain   thrpt        5       0,202 ±  0,002  ops/us
JsonBenchmark.testEncode                             None   thrpt        5      30,120 ±  0,364  ops/us
JsonBenchmark.testDecode                          Jackson    avgt        5       1,059 ±  0,029   us/op
JsonBenchmark.testDecode                      MinimalJson    avgt        5       3,321 ±  0,033   us/op
JsonBenchmark.testDecode                         NanoJson    avgt        5       9,978 ±  0,042   us/op
JsonBenchmark.testDecode                            Plain    avgt        5       1,502 ±  0,010   us/op
JsonBenchmark.testDecode                             None    avgt        5       0,113 ±  0,001   us/op
JsonBenchmark.testEncode                          Jackson    avgt        5       8,330 ±  0,070   us/op
JsonBenchmark.testEncode                      MinimalJson    avgt        5       7,702 ±  0,052   us/op
JsonBenchmark.testEncode                         NanoJson    avgt        5       7,855 ±  0,042   us/op
JsonBenchmark.testEncode                            Plain    avgt        5       4,935 ±  0,066   us/op
JsonBenchmark.testEncode                             None    avgt        5       0,033 ±  0,001   us/op
JsonBenchmark.testDecode                          Jackson  sample  1531802       1,109 ±  0,017   us/op
JsonBenchmark.testDecode:testDecode·p0.00         Jackson  sample                0,973            us/op
JsonBenchmark.testDecode:testDecode·p0.50         Jackson  sample                1,024            us/op
JsonBenchmark.testDecode:testDecode·p0.90         Jackson  sample                1,082            us/op
JsonBenchmark.testDecode:testDecode·p0.95         Jackson  sample                1,114            us/op
JsonBenchmark.testDecode:testDecode·p0.99         Jackson  sample                1,286            us/op
JsonBenchmark.testDecode:testDecode·p0.999        Jackson  sample                7,008            us/op
JsonBenchmark.testDecode:testDecode·p0.9999       Jackson  sample               57,076            us/op
JsonBenchmark.testDecode:testDecode·p1.00         Jackson  sample             2183,168            us/op
JsonBenchmark.testDecode                      MinimalJson  sample  1852612       3,446 ±  0,020   us/op
JsonBenchmark.testDecode:testDecode·p0.00     MinimalJson  sample                2,760            us/op
JsonBenchmark.testDecode:testDecode·p0.50     MinimalJson  sample                3,276            us/op
JsonBenchmark.testDecode:testDecode·p0.90     MinimalJson  sample                3,480            us/op
JsonBenchmark.testDecode:testDecode·p0.95     MinimalJson  sample                3,604            us/op
JsonBenchmark.testDecode:testDecode·p0.99     MinimalJson  sample                4,424            us/op
JsonBenchmark.testDecode:testDecode·p0.999    MinimalJson  sample               14,560            us/op
JsonBenchmark.testDecode:testDecode·p0.9999   MinimalJson  sample              703,488            us/op
JsonBenchmark.testDecode:testDecode·p1.00     MinimalJson  sample             1740,800            us/op
JsonBenchmark.testDecode                         NanoJson  sample  1233510      10,220 ±  0,041   us/op
JsonBenchmark.testDecode:testDecode·p0.00        NanoJson  sample                7,408            us/op
JsonBenchmark.testDecode:testDecode·p0.50        NanoJson  sample                9,696            us/op
JsonBenchmark.testDecode:testDecode·p0.90        NanoJson  sample               10,832            us/op
JsonBenchmark.testDecode:testDecode·p0.95        NanoJson  sample               11,312            us/op
JsonBenchmark.testDecode:testDecode·p0.99        NanoJson  sample               13,760            us/op
JsonBenchmark.testDecode:testDecode·p0.999       NanoJson  sample               42,368            us/op
JsonBenchmark.testDecode:testDecode·p0.9999      NanoJson  sample              807,936            us/op
JsonBenchmark.testDecode:testDecode·p1.00        NanoJson  sample             3100,672            us/op
JsonBenchmark.testDecode                            Plain  sample  1012996       1,669 ±  0,028   us/op
JsonBenchmark.testDecode:testDecode·p0.00           Plain  sample                1,460            us/op
JsonBenchmark.testDecode:testDecode·p0.50           Plain  sample                1,520            us/op
JsonBenchmark.testDecode:testDecode·p0.90           Plain  sample                1,620            us/op
JsonBenchmark.testDecode:testDecode·p0.95           Plain  sample                1,672            us/op
JsonBenchmark.testDecode:testDecode·p0.99           Plain  sample                2,068            us/op
JsonBenchmark.testDecode:testDecode·p0.999          Plain  sample               11,872            us/op
JsonBenchmark.testDecode:testDecode·p0.9999         Plain  sample              731,239            us/op
JsonBenchmark.testDecode:testDecode·p1.00           Plain  sample             1048,576            us/op
JsonBenchmark.testDecode                             None  sample  1499331       0,188 ±  0,009   us/op
JsonBenchmark.testDecode:testDecode·p0.00            None  sample                0,145            us/op
JsonBenchmark.testDecode:testDecode·p0.50            None  sample                0,165            us/op
JsonBenchmark.testDecode:testDecode·p0.90            None  sample                0,176            us/op
JsonBenchmark.testDecode:testDecode·p0.95            None  sample                0,181            us/op
JsonBenchmark.testDecode:testDecode·p0.99            None  sample                0,241            us/op
JsonBenchmark.testDecode:testDecode·p0.999           None  sample                1,208            us/op
JsonBenchmark.testDecode:testDecode·p0.9999          None  sample               11,345            us/op
JsonBenchmark.testDecode:testDecode·p1.00            None  sample             1484,800            us/op
JsonBenchmark.testEncode                          Jackson  sample  1423337       8,876 ±  0,036   us/op
JsonBenchmark.testEncode:testEncode·p0.00         Jackson  sample                8,120            us/op
JsonBenchmark.testEncode:testEncode·p0.50         Jackson  sample                8,496            us/op
JsonBenchmark.testEncode:testEncode·p0.90         Jackson  sample                8,656            us/op
JsonBenchmark.testEncode:testEncode·p0.95         Jackson  sample                9,104            us/op
JsonBenchmark.testEncode:testEncode·p0.99         Jackson  sample               15,536            us/op
JsonBenchmark.testEncode:testEncode·p0.999        Jackson  sample               35,626            us/op
JsonBenchmark.testEncode:testEncode·p0.9999       Jackson  sample              860,842            us/op
JsonBenchmark.testEncode:testEncode·p1.00         Jackson  sample             1136,640            us/op
JsonBenchmark.testEncode                      MinimalJson  sample  1612768       7,815 ±  0,030   us/op
JsonBenchmark.testEncode:testEncode·p0.00     MinimalJson  sample                7,088            us/op
JsonBenchmark.testEncode:testEncode·p0.50     MinimalJson  sample                7,520            us/op
JsonBenchmark.testEncode:testEncode·p0.90     MinimalJson  sample                7,776            us/op
JsonBenchmark.testEncode:testEncode·p0.95     MinimalJson  sample                8,048            us/op
JsonBenchmark.testEncode:testEncode·p0.99     MinimalJson  sample                9,312            us/op
JsonBenchmark.testEncode:testEncode·p0.999    MinimalJson  sample               29,031            us/op
JsonBenchmark.testEncode:testEncode·p0.9999   MinimalJson  sample              787,172            us/op
JsonBenchmark.testEncode:testEncode·p1.00     MinimalJson  sample             1062,912            us/op
JsonBenchmark.testEncode                         NanoJson  sample  1582979       7,977 ±  0,031   us/op
JsonBenchmark.testEncode:testEncode·p0.00        NanoJson  sample                6,992            us/op
JsonBenchmark.testEncode:testEncode·p0.50        NanoJson  sample                7,624            us/op
JsonBenchmark.testEncode:testEncode·p0.90        NanoJson  sample                8,016            us/op
JsonBenchmark.testEncode:testEncode·p0.95        NanoJson  sample                8,336            us/op
JsonBenchmark.testEncode:testEncode·p0.99        NanoJson  sample               10,240            us/op
JsonBenchmark.testEncode:testEncode·p0.999       NanoJson  sample               32,641            us/op
JsonBenchmark.testEncode:testEncode·p0.9999      NanoJson  sample              782,336            us/op
JsonBenchmark.testEncode:testEncode·p1.00        NanoJson  sample             1320,960            us/op
JsonBenchmark.testEncode                            Plain  sample  1257348       5,095 ±  0,033   us/op
JsonBenchmark.testEncode:testEncode·p0.00           Plain  sample                4,504            us/op
JsonBenchmark.testEncode:testEncode·p0.50           Plain  sample                4,840            us/op
JsonBenchmark.testEncode:testEncode·p0.90           Plain  sample                5,032            us/op
JsonBenchmark.testEncode:testEncode·p0.95           Plain  sample                5,168            us/op
JsonBenchmark.testEncode:testEncode·p0.99           Plain  sample                6,136            us/op
JsonBenchmark.testEncode:testEncode·p0.999          Plain  sample               22,133            us/op
JsonBenchmark.testEncode:testEncode·p0.9999         Plain  sample              782,336            us/op
JsonBenchmark.testEncode:testEncode·p1.00           Plain  sample             1071,104            us/op
JsonBenchmark.testEncode                             None  sample  1396409       0,084 ±  0,004   us/op
JsonBenchmark.testEncode:testEncode·p0.00            None  sample                0,029            us/op
JsonBenchmark.testEncode:testEncode·p0.50            None  sample                0,072            us/op
JsonBenchmark.testEncode:testEncode·p0.90            None  sample                0,085            us/op
JsonBenchmark.testEncode:testEncode·p0.95            None  sample                0,093            us/op
JsonBenchmark.testEncode:testEncode·p0.99            None  sample                0,168            us/op
JsonBenchmark.testEncode:testEncode·p0.999           None  sample                1,154            us/op
JsonBenchmark.testEncode:testEncode·p0.9999          None  sample               10,448            us/op
JsonBenchmark.testEncode:testEncode·p1.00            None  sample              949,248            us/op
JsonBenchmark.testDecode                          Jackson      ss           115133,625            us/op
JsonBenchmark.testDecode                      MinimalJson      ss             5960,566            us/op
JsonBenchmark.testDecode                         NanoJson      ss             3951,875            us/op
JsonBenchmark.testDecode                            Plain      ss             1630,485            us/op
JsonBenchmark.testDecode                             None      ss              158,147            us/op
JsonBenchmark.testEncode                          Jackson      ss           122994,507            us/op
JsonBenchmark.testEncode                      MinimalJson      ss            10987,324            us/op
JsonBenchmark.testEncode                         NanoJson      ss             9945,058            us/op
JsonBenchmark.testEncode                            Plain      ss             2025,324            us/op
JsonBenchmark.testEncode                             None      ss               61,227            us/op

Process finished with exit code 0

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
