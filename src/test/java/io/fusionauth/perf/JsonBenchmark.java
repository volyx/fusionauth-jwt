package io.fusionauth.perf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fusionauth.jwt.domain.JWT;
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
JsonBenchmark.testDecode                          Jackson   thrpt        5       1,020 ±  0,028  ops/us
JsonBenchmark.testDecode                      MinimalJson   thrpt        5       0,300 ±  0,003  ops/us
JsonBenchmark.testDecode                         NanoJson   thrpt        5       0,098 ±  0,006  ops/us
JsonBenchmark.testDecode                            Plain   thrpt        5       0,594 ±  0,005  ops/us
JsonBenchmark.testDecode                             None   thrpt        5       8,207 ±  0,044  ops/us
JsonBenchmark.testEncode                          Jackson   thrpt        5       0,116 ±  0,001  ops/us
JsonBenchmark.testEncode                      MinimalJson   thrpt        5       0,124 ±  0,001  ops/us
JsonBenchmark.testEncode                         NanoJson   thrpt        5       0,124 ±  0,001  ops/us
JsonBenchmark.testEncode                            Plain   thrpt        5       0,203 ±  0,001  ops/us
JsonBenchmark.testEncode                             None   thrpt        5      30,251 ±  0,176  ops/us
JsonBenchmark.testDecode                          Jackson    avgt        5       1,000 ±  0,014   us/op
JsonBenchmark.testDecode                      MinimalJson    avgt        5       3,385 ±  0,016   us/op
JsonBenchmark.testDecode                         NanoJson    avgt        5      10,412 ±  0,851   us/op
JsonBenchmark.testDecode                            Plain    avgt        5       1,716 ±  0,020   us/op
JsonBenchmark.testDecode                             None    avgt        5       0,122 ±  0,001   us/op
JsonBenchmark.testEncode                          Jackson    avgt        5       8,852 ±  1,547   us/op
JsonBenchmark.testEncode                      MinimalJson    avgt        5       8,085 ±  0,113   us/op
JsonBenchmark.testEncode                         NanoJson    avgt        5       8,024 ±  0,053   us/op
JsonBenchmark.testEncode                            Plain    avgt        5       4,983 ±  0,042   us/op
JsonBenchmark.testEncode                             None    avgt        5       0,033 ±  0,001   us/op
JsonBenchmark.testDecode                          Jackson  sample  1468217       1,178 ±  0,021   us/op
JsonBenchmark.testDecode:testDecode·p0.00         Jackson  sample                1,000            us/op
JsonBenchmark.testDecode:testDecode·p0.50         Jackson  sample                1,052            us/op
JsonBenchmark.testDecode:testDecode·p0.90         Jackson  sample                1,120            us/op
JsonBenchmark.testDecode:testDecode·p0.95         Jackson  sample                1,168            us/op
JsonBenchmark.testDecode:testDecode·p0.99         Jackson  sample                1,834            us/op
JsonBenchmark.testDecode:testDecode·p0.999        Jackson  sample               12,880            us/op
JsonBenchmark.testDecode:testDecode·p0.9999       Jackson  sample               87,643            us/op
JsonBenchmark.testDecode:testDecode·p1.00         Jackson  sample             4161,536            us/op
JsonBenchmark.testDecode                      MinimalJson  sample  1762085       3,649 ±  0,024   us/op
JsonBenchmark.testDecode:testDecode·p0.00     MinimalJson  sample                2,768            us/op
JsonBenchmark.testDecode:testDecode·p0.50     MinimalJson  sample                3,256            us/op
JsonBenchmark.testDecode:testDecode·p0.90     MinimalJson  sample                3,652            us/op
JsonBenchmark.testDecode:testDecode·p0.95     MinimalJson  sample                4,104            us/op
JsonBenchmark.testDecode:testDecode·p0.99     MinimalJson  sample                8,720            us/op
JsonBenchmark.testDecode:testDecode·p0.999    MinimalJson  sample               39,296            us/op
JsonBenchmark.testDecode:testDecode·p0.9999   MinimalJson  sample              721,706            us/op
JsonBenchmark.testDecode:testDecode·p1.00     MinimalJson  sample             2093,056            us/op
JsonBenchmark.testDecode                         NanoJson  sample  1154809      11,265 ±  0,178   us/op
JsonBenchmark.testDecode:testDecode·p0.00        NanoJson  sample                7,448            us/op
JsonBenchmark.testDecode:testDecode·p0.50        NanoJson  sample                9,664            us/op
JsonBenchmark.testDecode:testDecode·p0.90        NanoJson  sample               11,456            us/op
JsonBenchmark.testDecode:testDecode·p0.95        NanoJson  sample               13,424            us/op
JsonBenchmark.testDecode:testDecode·p0.99        NanoJson  sample               22,432            us/op
JsonBenchmark.testDecode:testDecode·p0.999       NanoJson  sample              144,433            us/op
JsonBenchmark.testDecode:testDecode·p0.9999      NanoJson  sample             1516,972            us/op
JsonBenchmark.testDecode:testDecode·p1.00        NanoJson  sample            31031,296            us/op
JsonBenchmark.testDecode                            Plain  sample  1790389       1,851 ±  0,018   us/op
JsonBenchmark.testDecode:testDecode·p0.00           Plain  sample                1,560            us/op
JsonBenchmark.testDecode:testDecode·p0.50           Plain  sample                1,634            us/op
JsonBenchmark.testDecode:testDecode·p0.90           Plain  sample                1,796            us/op
JsonBenchmark.testDecode:testDecode·p0.95           Plain  sample                1,968            us/op
JsonBenchmark.testDecode:testDecode·p0.99           Plain  sample                3,404            us/op
JsonBenchmark.testDecode:testDecode·p0.999          Plain  sample               27,904            us/op
JsonBenchmark.testDecode:testDecode·p0.9999         Plain  sample              130,043            us/op
JsonBenchmark.testDecode:testDecode·p1.00           Plain  sample             1765,376            us/op
JsonBenchmark.testDecode                             None  sample  1428407       0,271 ±  0,041   us/op
JsonBenchmark.testDecode:testDecode·p0.00            None  sample                0,135            us/op
JsonBenchmark.testDecode:testDecode·p0.50            None  sample                0,160            us/op
JsonBenchmark.testDecode:testDecode·p0.90            None  sample                0,202            us/op
JsonBenchmark.testDecode:testDecode·p0.95            None  sample                0,263            us/op
JsonBenchmark.testDecode:testDecode·p0.99            None  sample                0,427            us/op
JsonBenchmark.testDecode:testDecode·p0.999           None  sample               12,480            us/op
JsonBenchmark.testDecode:testDecode·p0.9999          None  sample              100,306            us/op
JsonBenchmark.testDecode:testDecode·p1.00            None  sample            11550,720            us/op
JsonBenchmark.testEncode                          Jackson  sample  1189368      11,065 ±  0,241   us/op
JsonBenchmark.testEncode:testEncode·p0.00         Jackson  sample                8,176            us/op
JsonBenchmark.testEncode:testEncode·p0.50         Jackson  sample                8,544            us/op
JsonBenchmark.testEncode:testEncode·p0.90         Jackson  sample               10,992            us/op
JsonBenchmark.testEncode:testEncode·p0.95         Jackson  sample               17,088            us/op
JsonBenchmark.testEncode:testEncode·p0.99         Jackson  sample               34,112            us/op
JsonBenchmark.testEncode:testEncode·p0.999        Jackson  sample              230,723            us/op
JsonBenchmark.testEncode:testEncode·p0.9999       Jackson  sample             1891,209            us/op
JsonBenchmark.testEncode:testEncode·p1.00         Jackson  sample            55836,672            us/op
JsonBenchmark.testEncode                      MinimalJson  sample  1426700       9,012 ±  0,124   us/op
JsonBenchmark.testEncode:testEncode·p0.00     MinimalJson  sample                7,280            us/op
JsonBenchmark.testEncode:testEncode·p0.50     MinimalJson  sample                7,752            us/op
JsonBenchmark.testEncode:testEncode·p0.90     MinimalJson  sample                8,592            us/op
JsonBenchmark.testEncode:testEncode·p0.95     MinimalJson  sample               11,904            us/op
JsonBenchmark.testEncode:testEncode·p0.99     MinimalJson  sample               18,176            us/op
JsonBenchmark.testEncode:testEncode·p0.999    MinimalJson  sample              109,734            us/op
JsonBenchmark.testEncode:testEncode·p0.9999   MinimalJson  sample             1225,380            us/op
JsonBenchmark.testEncode:testEncode·p1.00     MinimalJson  sample            38207,488            us/op
JsonBenchmark.testEncode                         NanoJson  sample  1303091      10,095 ±  0,243   us/op
JsonBenchmark.testEncode:testEncode·p0.00        NanoJson  sample                7,096            us/op
JsonBenchmark.testEncode:testEncode·p0.50        NanoJson  sample                7,864            us/op
JsonBenchmark.testEncode:testEncode·p0.90        NanoJson  sample               10,080            us/op
JsonBenchmark.testEncode:testEncode·p0.95        NanoJson  sample               13,344            us/op
JsonBenchmark.testEncode:testEncode·p0.99        NanoJson  sample               25,600            us/op
JsonBenchmark.testEncode:testEncode·p0.999       NanoJson  sample              233,192            us/op
JsonBenchmark.testEncode:testEncode·p0.9999      NanoJson  sample             2470,185            us/op
JsonBenchmark.testEncode:testEncode·p1.00        NanoJson  sample            46202,880            us/op
JsonBenchmark.testEncode                            Plain  sample  1173034       5,533 ±  0,049   us/op
JsonBenchmark.testEncode:testEncode·p0.00           Plain  sample                4,504            us/op
JsonBenchmark.testEncode:testEncode·p0.50           Plain  sample                4,840            us/op
JsonBenchmark.testEncode:testEncode·p0.90           Plain  sample                5,360            us/op
JsonBenchmark.testEncode:testEncode·p0.95           Plain  sample                7,544            us/op
JsonBenchmark.testEncode:testEncode·p0.99           Plain  sample               11,786            us/op
JsonBenchmark.testEncode:testEncode·p0.999          Plain  sample               64,064            us/op
JsonBenchmark.testEncode:testEncode·p0.9999         Plain  sample              781,312            us/op
JsonBenchmark.testEncode:testEncode·p1.00           Plain  sample             8249,344            us/op
JsonBenchmark.testEncode                             None  sample  1382726       0,087 ±  0,004   us/op
JsonBenchmark.testEncode:testEncode·p0.00            None  sample                0,013            us/op
JsonBenchmark.testEncode:testEncode·p0.50            None  sample                0,072            us/op
JsonBenchmark.testEncode:testEncode·p0.90            None  sample                0,083            us/op
JsonBenchmark.testEncode:testEncode·p0.95            None  sample                0,094            us/op
JsonBenchmark.testEncode:testEncode·p0.99            None  sample                0,193            us/op
JsonBenchmark.testEncode:testEncode·p0.999           None  sample                1,424            us/op
JsonBenchmark.testEncode:testEncode·p0.9999          None  sample               13,267            us/op
JsonBenchmark.testEncode:testEncode·p1.00            None  sample              764,928            us/op
JsonBenchmark.testDecode                          Jackson      ss           121366,825            us/op
JsonBenchmark.testDecode                      MinimalJson      ss             6317,960            us/op
JsonBenchmark.testDecode                         NanoJson      ss            19759,491            us/op
JsonBenchmark.testDecode                            Plain      ss             4339,398            us/op
JsonBenchmark.testDecode                             None      ss              121,165            us/op
JsonBenchmark.testEncode                          Jackson      ss           118334,962            us/op
JsonBenchmark.testEncode                      MinimalJson      ss            12759,799            us/op
JsonBenchmark.testEncode                         NanoJson      ss            19407,559            us/op
JsonBenchmark.testEncode                            Plain      ss             1791,129            us/op
JsonBenchmark.testEncode                             None      ss              112,710            us/op

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

			objectMapper = new ObjectMapper();
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
