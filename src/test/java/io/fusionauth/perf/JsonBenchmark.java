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

http://klikr.org/ac740cf88935c80d83aaf24e6c2d.txt

Benchmark                                    (serializer)    Mode      Cnt       Score    Error   Units
JsonBenchmark.testDecode                          Jackson   thrpt        5       0,916 ±  0,082  ops/us
JsonBenchmark.testDecode                      MinimalJson   thrpt        5       0,220 ±  0,080  ops/us
JsonBenchmark.testDecode                         NanoJson   thrpt        5       0,075 ±  0,049  ops/us
JsonBenchmark.testEncode                          Jackson   thrpt        5       0,099 ±  0,015  ops/us
JsonBenchmark.testEncode                      MinimalJson   thrpt        5       0,075 ±  0,012  ops/us
JsonBenchmark.testEncode                         NanoJson   thrpt        5       0,097 ±  0,042  ops/us
JsonBenchmark.testDecode                          Jackson    avgt        5       1,421 ±  1,374   us/op
JsonBenchmark.testDecode                      MinimalJson    avgt        5       9,705 ± 20,836   us/op
JsonBenchmark.testDecode                         NanoJson    avgt        5      13,406 ±  5,431   us/op
JsonBenchmark.testEncode                          Jackson    avgt        5      11,697 ± 11,304   us/op
JsonBenchmark.testEncode                      MinimalJson    avgt        5      10,082 ±  2,633   us/op
JsonBenchmark.testEncode                         NanoJson    avgt        5      10,903 ±  2,098   us/op
JsonBenchmark.testDecode                          Jackson  sample  1338641       1,597 ±  0,124   us/op
JsonBenchmark.testDecode:testDecode·p0.00         Jackson  sample                0,957            us/op
JsonBenchmark.testDecode:testDecode·p0.50         Jackson  sample                1,009            us/op
JsonBenchmark.testDecode:testDecode·p0.90         Jackson  sample                1,168            us/op
JsonBenchmark.testDecode:testDecode·p0.95         Jackson  sample                1,884            us/op
JsonBenchmark.testDecode:testDecode·p0.99         Jackson  sample                2,532            us/op
JsonBenchmark.testDecode:testDecode·p0.999        Jackson  sample               49,559            us/op
JsonBenchmark.testDecode:testDecode·p0.9999       Jackson  sample              870,678            us/op
JsonBenchmark.testDecode:testDecode·p1.00         Jackson  sample            30244,864            us/op
JsonBenchmark.testDecode                      MinimalJson  sample  1606315       4,102 ±  0,047   us/op
JsonBenchmark.testDecode:testDecode·p0.00     MinimalJson  sample                2,836            us/op
JsonBenchmark.testDecode:testDecode·p0.50     MinimalJson  sample                3,260            us/op
JsonBenchmark.testDecode:testDecode·p0.90     MinimalJson  sample                4,936            us/op
JsonBenchmark.testDecode:testDecode·p0.95     MinimalJson  sample                5,600            us/op
JsonBenchmark.testDecode:testDecode·p0.99     MinimalJson  sample               13,600            us/op
JsonBenchmark.testDecode:testDecode·p0.999    MinimalJson  sample               79,488            us/op
JsonBenchmark.testDecode:testDecode·p0.9999   MinimalJson  sample              782,336            us/op
JsonBenchmark.testDecode:testDecode·p1.00     MinimalJson  sample             7151,616            us/op
JsonBenchmark.testDecode                         NanoJson  sample  1133994      14,376 ±  0,456   us/op
JsonBenchmark.testDecode:testDecode·p0.00        NanoJson  sample                7,392            us/op
JsonBenchmark.testDecode:testDecode·p0.50        NanoJson  sample               10,016            us/op
JsonBenchmark.testDecode:testDecode·p0.90        NanoJson  sample               13,952            us/op
JsonBenchmark.testDecode:testDecode·p0.95        NanoJson  sample               15,440            us/op
JsonBenchmark.testDecode:testDecode·p0.99        NanoJson  sample               43,200            us/op
JsonBenchmark.testDecode:testDecode·p0.999       NanoJson  sample              758,784            us/op
JsonBenchmark.testDecode:testDecode·p0.9999      NanoJson  sample             4993,847            us/op
JsonBenchmark.testDecode:testDecode·p1.00        NanoJson  sample            58327,040            us/op
JsonBenchmark.testEncode                          Jackson  sample  1212865      11,215 ±  0,305   us/op
JsonBenchmark.testEncode:testEncode·p0.00         Jackson  sample                7,952            us/op
JsonBenchmark.testEncode:testEncode·p0.50         Jackson  sample                8,384            us/op
JsonBenchmark.testEncode:testEncode·p0.90         Jackson  sample                9,488            us/op
JsonBenchmark.testEncode:testEncode·p0.95         Jackson  sample               16,224            us/op
JsonBenchmark.testEncode:testEncode·p0.99         Jackson  sample               26,912            us/op
JsonBenchmark.testEncode:testEncode·p0.999        Jackson  sample              388,677            us/op
JsonBenchmark.testEncode:testEncode·p0.9999       Jackson  sample             2898,769            us/op
JsonBenchmark.testEncode:testEncode·p1.00         Jackson  sample            56754,176            us/op
JsonBenchmark.testEncode                      MinimalJson  sample  1212690      12,045 ±  0,738   us/op
JsonBenchmark.testEncode:testEncode·p0.00     MinimalJson  sample                7,272            us/op
JsonBenchmark.testEncode:testEncode·p0.50     MinimalJson  sample                7,832            us/op
JsonBenchmark.testEncode:testEncode·p0.90     MinimalJson  sample               12,224            us/op
JsonBenchmark.testEncode:testEncode·p0.95     MinimalJson  sample               14,624            us/op
JsonBenchmark.testEncode:testEncode·p0.99     MinimalJson  sample               31,808            us/op
JsonBenchmark.testEncode:testEncode·p0.999    MinimalJson  sample              646,460            us/op
JsonBenchmark.testEncode:testEncode·p0.9999   MinimalJson  sample             4358,144            us/op
JsonBenchmark.testEncode:testEncode·p1.00     MinimalJson  sample           173277,184            us/op
JsonBenchmark.testEncode                         NanoJson  sample  1355756       9,705 ±  0,189   us/op
JsonBenchmark.testEncode:testEncode·p0.00        NanoJson  sample                7,080            us/op
JsonBenchmark.testEncode:testEncode·p0.50        NanoJson  sample                7,784            us/op
JsonBenchmark.testEncode:testEncode·p0.90        NanoJson  sample                9,200            us/op
JsonBenchmark.testEncode:testEncode·p0.95        NanoJson  sample               13,120            us/op
JsonBenchmark.testEncode:testEncode·p0.99        NanoJson  sample               24,736            us/op
JsonBenchmark.testEncode:testEncode·p0.999       NanoJson  sample              185,150            us/op
JsonBenchmark.testEncode:testEncode·p0.9999      NanoJson  sample             1976,010            us/op
JsonBenchmark.testEncode:testEncode·p1.00        NanoJson  sample            27262,976            us/op
JsonBenchmark.testDecode                          Jackson      ss           132440,720            us/op
JsonBenchmark.testDecode                      MinimalJson      ss            11010,669            us/op
JsonBenchmark.testDecode                         NanoJson      ss            21058,245            us/op
JsonBenchmark.testEncode                          Jackson      ss           135081,680            us/op
JsonBenchmark.testEncode                      MinimalJson      ss            15883,072            us/op
JsonBenchmark.testEncode                         NanoJson      ss            22326,541            us/op


 */

public class JsonBenchmark {

	@State( Scope.Benchmark )
	public static class Parameters {

		@Param( {
				"Jackson",
				"MinimalJson",
				"NanoJson",
				"Plain"
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
				decoded = parameters.nanoJsonObjectMapper.readValue(parameters.json, JWT.class);
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
				encoded = parameters.nanoJsonObjectMapper.writeValueAsBytes(parameters.jwt);
				break;
		}

		bh.consume(encoded);
	}
}
