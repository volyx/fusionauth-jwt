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
JsonBenchmark.testDecode                          Jackson   thrpt        5       0,852 ±  0,881  ops/us
JsonBenchmark.testDecode                      MinimalJson   thrpt        5       0,283 ±  0,076  ops/us
JsonBenchmark.testDecode                         NanoJson   thrpt        5       0,096 ±  0,002  ops/us
JsonBenchmark.testDecode                            Plain   thrpt        5       0,096 ±  0,001  ops/us
JsonBenchmark.testDecode                             None   thrpt        5       7,993 ±  1,868  ops/us
JsonBenchmark.testEncode                          Jackson   thrpt        5       0,109 ±  0,027  ops/us
JsonBenchmark.testEncode                      MinimalJson   thrpt        5       0,109 ±  0,004  ops/us
JsonBenchmark.testEncode                         NanoJson   thrpt        5       0,110 ±  0,030  ops/us
JsonBenchmark.testEncode                            Plain   thrpt        5       0,125 ±  0,001  ops/us
JsonBenchmark.testEncode                             None   thrpt        5      29,904 ±  0,740  ops/us
JsonBenchmark.testDecode                          Jackson    avgt        5       1,025 ±  0,141   us/op
JsonBenchmark.testDecode                      MinimalJson    avgt        5       3,774 ±  0,945   us/op
JsonBenchmark.testDecode                         NanoJson    avgt        5      10,906 ±  1,853   us/op
JsonBenchmark.testDecode                            Plain    avgt        5      10,498 ±  0,251   us/op
JsonBenchmark.testDecode                             None    avgt        5       0,125 ±  0,010   us/op
JsonBenchmark.testEncode                          Jackson    avgt        5       8,616 ±  0,129   us/op
JsonBenchmark.testEncode                      MinimalJson    avgt        5       8,105 ±  0,112   us/op
JsonBenchmark.testEncode                         NanoJson    avgt        5       8,210 ±  1,461   us/op
JsonBenchmark.testEncode                            Plain    avgt        5       8,108 ±  0,200   us/op
JsonBenchmark.testEncode                             None    avgt        5       0,033 ±  0,001   us/op
JsonBenchmark.testDecode                          Jackson  sample  1447253       1,230 ±  0,025   us/op
JsonBenchmark.testDecode:testDecode·p0.00         Jackson  sample                0,978            us/op
JsonBenchmark.testDecode:testDecode·p0.50         Jackson  sample                1,026            us/op
JsonBenchmark.testDecode:testDecode·p0.90         Jackson  sample                1,118            us/op
JsonBenchmark.testDecode:testDecode·p0.95         Jackson  sample                1,240            us/op
JsonBenchmark.testDecode:testDecode·p0.99         Jackson  sample                2,356            us/op
JsonBenchmark.testDecode:testDecode·p0.999        Jackson  sample               24,800            us/op
JsonBenchmark.testDecode:testDecode·p0.9999       Jackson  sample              203,781            us/op
JsonBenchmark.testDecode:testDecode·p1.00         Jackson  sample             4251,648            us/op
JsonBenchmark.testDecode                      MinimalJson  sample  1797905       3,586 ±  0,024   us/op
JsonBenchmark.testDecode:testDecode·p0.00     MinimalJson  sample                2,836            us/op
JsonBenchmark.testDecode:testDecode·p0.50     MinimalJson  sample                3,236            us/op
JsonBenchmark.testDecode:testDecode·p0.90     MinimalJson  sample                3,556            us/op
JsonBenchmark.testDecode:testDecode·p0.95     MinimalJson  sample                3,876            us/op
JsonBenchmark.testDecode:testDecode·p0.99     MinimalJson  sample                6,208            us/op
JsonBenchmark.testDecode:testDecode·p0.999    MinimalJson  sample               43,776            us/op
JsonBenchmark.testDecode:testDecode·p0.9999   MinimalJson  sample              721,110            us/op
JsonBenchmark.testDecode:testDecode·p1.00     MinimalJson  sample             2441,216            us/op
JsonBenchmark.testDecode                         NanoJson  sample  1171993      10,815 ±  0,050   us/op
JsonBenchmark.testDecode:testDecode·p0.00        NanoJson  sample                7,352            us/op
JsonBenchmark.testDecode:testDecode·p0.50        NanoJson  sample                9,984            us/op
JsonBenchmark.testDecode:testDecode·p0.90        NanoJson  sample               11,456            us/op
JsonBenchmark.testDecode:testDecode·p0.95        NanoJson  sample               12,544            us/op
JsonBenchmark.testDecode:testDecode·p0.99        NanoJson  sample               20,096            us/op
JsonBenchmark.testDecode:testDecode·p0.999       NanoJson  sample               83,201            us/op
JsonBenchmark.testDecode:testDecode·p0.9999      NanoJson  sample              822,068            us/op
JsonBenchmark.testDecode:testDecode·p1.00        NanoJson  sample             5644,288            us/op
JsonBenchmark.testDecode                            Plain  sample  1175766      10,791 ±  0,058   us/op
JsonBenchmark.testDecode:testDecode·p0.00           Plain  sample                7,392            us/op
JsonBenchmark.testDecode:testDecode·p0.50           Plain  sample                9,936            us/op
JsonBenchmark.testDecode:testDecode·p0.90           Plain  sample               11,472            us/op
JsonBenchmark.testDecode:testDecode·p0.95           Plain  sample               12,608            us/op
JsonBenchmark.testDecode:testDecode·p0.99           Plain  sample               19,936            us/op
JsonBenchmark.testDecode:testDecode·p0.999          Plain  sample               83,200            us/op
JsonBenchmark.testDecode:testDecode·p0.9999         Plain  sample              818,176            us/op
JsonBenchmark.testDecode:testDecode·p1.00           Plain  sample            11534,336            us/op
JsonBenchmark.testDecode                             None  sample  1473190       0,203 ±  0,010   us/op
JsonBenchmark.testDecode:testDecode·p0.00            None  sample                0,146            us/op
JsonBenchmark.testDecode:testDecode·p0.50            None  sample                0,163            us/op
JsonBenchmark.testDecode:testDecode·p0.90            None  sample                0,182            us/op
JsonBenchmark.testDecode:testDecode·p0.95            None  sample                0,196            us/op
JsonBenchmark.testDecode:testDecode·p0.99            None  sample                0,315            us/op
JsonBenchmark.testDecode:testDecode·p0.999           None  sample                4,645            us/op
JsonBenchmark.testDecode:testDecode·p0.9999          None  sample               34,860            us/op
JsonBenchmark.testDecode:testDecode·p1.00            None  sample             1755,136            us/op
JsonBenchmark.testEncode                          Jackson  sample  1369066       9,258 ±  0,079   us/op
JsonBenchmark.testEncode:testEncode·p0.00         Jackson  sample                7,992            us/op
JsonBenchmark.testEncode:testEncode·p0.50         Jackson  sample                8,448            us/op
JsonBenchmark.testEncode:testEncode·p0.90         Jackson  sample                9,072            us/op
JsonBenchmark.testEncode:testEncode·p0.95         Jackson  sample                9,328            us/op
JsonBenchmark.testEncode:testEncode·p0.99         Jackson  sample               18,464            us/op
JsonBenchmark.testEncode:testEncode·p0.999        Jackson  sample               81,280            us/op
JsonBenchmark.testEncode:testEncode·p0.9999       Jackson  sample              844,896            us/op
JsonBenchmark.testEncode:testEncode·p1.00         Jackson  sample            16777,216            us/op
JsonBenchmark.testEncode                      MinimalJson  sample  1564022       8,097 ±  0,033   us/op
JsonBenchmark.testEncode:testEncode·p0.00     MinimalJson  sample                7,152            us/op
JsonBenchmark.testEncode:testEncode·p0.50     MinimalJson  sample                7,592            us/op
JsonBenchmark.testEncode:testEncode·p0.90     MinimalJson  sample                7,992            us/op
JsonBenchmark.testEncode:testEncode·p0.95     MinimalJson  sample                8,432            us/op
JsonBenchmark.testEncode:testEncode·p0.99     MinimalJson  sample               13,920            us/op
JsonBenchmark.testEncode:testEncode·p0.999    MinimalJson  sample               62,400            us/op
JsonBenchmark.testEncode:testEncode·p0.9999   MinimalJson  sample              783,360            us/op
JsonBenchmark.testEncode:testEncode·p1.00     MinimalJson  sample             1603,584            us/op
JsonBenchmark.testEncode                         NanoJson  sample  1491619       8,476 ±  0,060   us/op
JsonBenchmark.testEncode:testEncode·p0.00        NanoJson  sample                6,952            us/op
JsonBenchmark.testEncode:testEncode·p0.50        NanoJson  sample                7,648            us/op
JsonBenchmark.testEncode:testEncode·p0.90        NanoJson  sample                8,432            us/op
JsonBenchmark.testEncode:testEncode·p0.95        NanoJson  sample               10,144            us/op
JsonBenchmark.testEncode:testEncode·p0.99        NanoJson  sample               19,328            us/op
JsonBenchmark.testEncode:testEncode·p0.999       NanoJson  sample               74,112            us/op
JsonBenchmark.testEncode:testEncode·p0.9999      NanoJson  sample              807,936            us/op
JsonBenchmark.testEncode:testEncode·p1.00        NanoJson  sample            20054,016            us/op
JsonBenchmark.testEncode                            Plain  sample  1511131       8,399 ±  0,048   us/op
JsonBenchmark.testEncode:testEncode·p0.00           Plain  sample                6,848            us/op
JsonBenchmark.testEncode:testEncode·p0.50           Plain  sample                7,624            us/op
JsonBenchmark.testEncode:testEncode·p0.90           Plain  sample                8,320            us/op
JsonBenchmark.testEncode:testEncode·p0.95           Plain  sample                9,248            us/op
JsonBenchmark.testEncode:testEncode·p0.99           Plain  sample               18,944            us/op
JsonBenchmark.testEncode:testEncode·p0.999          Plain  sample               75,136            us/op
JsonBenchmark.testEncode:testEncode·p0.9999         Plain  sample              809,984            us/op
JsonBenchmark.testEncode:testEncode·p1.00           Plain  sample            10665,984            us/op
JsonBenchmark.testEncode                             None  sample  1317292       0,131 ±  0,051   us/op
JsonBenchmark.testEncode:testEncode·p0.00            None  sample                0,032            us/op
JsonBenchmark.testEncode:testEncode·p0.50            None  sample                0,076            us/op
JsonBenchmark.testEncode:testEncode·p0.90            None  sample                0,088            us/op
JsonBenchmark.testEncode:testEncode·p0.95            None  sample                0,107            us/op
JsonBenchmark.testEncode:testEncode·p0.99            None  sample                0,221            us/op
JsonBenchmark.testEncode:testEncode·p0.999           None  sample                2,420            us/op
JsonBenchmark.testEncode:testEncode·p0.9999          None  sample               33,361            us/op
JsonBenchmark.testEncode:testEncode·p1.00            None  sample            16171,008            us/op
JsonBenchmark.testDecode                          Jackson      ss           248459,639            us/op
JsonBenchmark.testDecode                      MinimalJson      ss             9389,887            us/op
JsonBenchmark.testDecode                         NanoJson      ss            21450,854            us/op
JsonBenchmark.testDecode                            Plain      ss             3742,186            us/op
JsonBenchmark.testDecode                             None      ss              103,753            us/op
JsonBenchmark.testEncode                          Jackson      ss           204919,147            us/op
JsonBenchmark.testEncode                      MinimalJson      ss            12637,367            us/op
JsonBenchmark.testEncode                         NanoJson      ss            21939,862            us/op
JsonBenchmark.testEncode                            Plain      ss             6556,408            us/op
JsonBenchmark.testEncode                             None      ss               66,902            us/op

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
