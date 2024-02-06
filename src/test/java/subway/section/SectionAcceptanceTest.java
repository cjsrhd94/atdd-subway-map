package subway.section;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import subway.line.LineRequest;
import subway.line.LineResponse;
import subway.station.Station;
import subway.station.StationResponse;
import util.RestAssuredUtil;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(value = "/table_truncate.sql")
@DisplayName("지하철 노선 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SectionAcceptanceTest {
    private static Long SINSA_STATION_ID;
    private static Long GWANGGYO_STATION_ID;
    private static Long SUSEO_STATION_ID;
    private static Long LINE_SHINBUNDANG_ID;
    private static SectionRequest SECTION_TWO;

    @BeforeEach
    void setFixture() {
        SINSA_STATION_ID = RestAssuredUtil.post(new Station("신사역"), "stations")
                .as(StationResponse.class).getId();
        GWANGGYO_STATION_ID = RestAssuredUtil.post(new Station("광교역"), "stations")
                .as(StationResponse.class).getId();
        SUSEO_STATION_ID = RestAssuredUtil.post(new Station("수서역"), "stations")
                .as(StationResponse.class).getId();
        LINE_SHINBUNDANG_ID = RestAssuredUtil.post(
                new LineRequest(0L, "신분당선", "bg-red-600", 10L, SINSA_STATION_ID, GWANGGYO_STATION_ID),
                "/lines").as(LineResponse.class).getId();
        SECTION_TWO = new SectionRequest(GWANGGYO_STATION_ID, SUSEO_STATION_ID, 30L);
    }

    /**
     * When 지하철 구간을 생성하면
     * Then 지하철 노선 목록 조회 시 생성한 구간을 찾을 수 있다
     */
    @DisplayName("지하철 구간을 생성한다.")
    @Test
    void createSection() {
        ExtractableResponse<Response> response
                = RestAssuredUtil.post(SECTION_TWO, "/lines/" + LINE_SHINBUNDANG_ID + "/sections");

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        // then
        LineResponse res
                = RestAssuredUtil.get("/lines/" + LINE_SHINBUNDANG_ID).as(LineResponse.class);
        assertThat(res.getStations().get(0).getName()).isEqualTo("신사역");
        assertThat(res.getStations().get(1).getName()).isEqualTo("광교역");
        assertThat(res.getStations().get(2).getName()).isEqualTo("수서역");
    }
}
