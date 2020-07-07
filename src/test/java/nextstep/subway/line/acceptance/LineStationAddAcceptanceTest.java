package nextstep.subway.line.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.station.dto.StationResponse;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nextstep.subway.line.acceptance.step.LineAcceptanceStep.지하철_노선_등록되어_있음;
import static nextstep.subway.line.acceptance.step.LineStationAddAcceptanceStep.*;
import static nextstep.subway.station.acceptance.step.StationAcceptanceStep.지하철역_등록되어_있음;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 노선에 역 등록 관련 기능")
public class LineStationAddAcceptanceTest extends AcceptanceTest {
    @DisplayName("지하철 노선에 역을 등록한다.")
    @Test
    void addLineStation() {
        // given
        ExtractableResponse<Response> createdLineResponse = 지하철_노선_등록되어_있음("2호선", "GREEN",
                LocalTime.of(5, 30), LocalTime.of(23, 30), 5);
        ExtractableResponse<Response> createdStationResponse = 지하철역_등록되어_있음("강남역");

        // when
        Long lineId = createdLineResponse.as(LineResponse.class).getId();
        Long stationId = createdStationResponse.as(StationResponse.class).getId();

        ExtractableResponse<Response> response = 지하철_노선에_지하철역_등록_요청(lineId, null, stationId, 4, 2);

        // then
        지하철_노선에_지하철역_등록됨(response);
    }



    @DisplayName("지하철 노선 상세정보 조회 시 역 정보가 포함된다.")
    @Test
    void getLineWithStations() {
        // given
        ExtractableResponse<Response> createdLineResponse = 지하철_노선_등록되어_있음("2호선", "GREEN",
                LocalTime.of(5, 30), LocalTime.of(23, 30), 5);
        ExtractableResponse<Response> createdStationResponse = 지하철역_등록되어_있음("강남역");

        Long lineId = createdLineResponse.as(LineResponse.class).getId();
        Long stationId = createdStationResponse.as(StationResponse.class).getId();
        지하철_노선에_지하철역_등록_요청(lineId, null, stationId, 4, 2);

        // when
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(lineId);

        // then
        지하철_노선_상세정보_응답됨(response);
    }


    @DisplayName("지하철 노선에 여러개의 역을 순서대로 등록한다.")
    @Test
    void addLineStationInOrder() {
        // given
        ExtractableResponse<Response> createdLineResponse = 지하철_노선_등록되어_있음("2호선", "GREEN",
                LocalTime.of(5, 30), LocalTime.of(23, 30), 5);
        ExtractableResponse<Response> createdStationResponse1 = 지하철역_등록되어_있음("강남역");
        ExtractableResponse<Response> createdStationResponse2 = 지하철역_등록되어_있음("역삼역");
        ExtractableResponse<Response> createdStationResponse3 = 지하철역_등록되어_있음("선릉역");

        // when
        Long lineId = createdLineResponse.as(LineResponse.class).getId();
        Long stationId1 = createdStationResponse1.as(StationResponse.class).getId();
        ExtractableResponse<Response> lineStationResponse = 지하철_노선에_지하철역_등록_요청(lineId, null, stationId1, 4, 2);

        Long stationId2 = createdStationResponse2.as(StationResponse.class).getId();
        지하철_노선에_지하철역_등록_요청(lineId, stationId1, stationId2, 4, 2);

        Long stationId3 = createdStationResponse3.as(StationResponse.class).getId();
        지하철_노선에_지하철역_등록_요청(lineId, stationId2, stationId3, 4, 2);


        // then
        assertThat(lineStationResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        // when
        // 지하철_노선_조회_요청
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(lineId);

        LineResponse lineResponse = response.as(LineResponse.class);
        assertThat(lineResponse).isNotNull();
        assertThat(lineResponse.getStations()).hasSize(3);
        List<Long> stationIds = lineResponse.getStations().stream()
                .map(it -> it.getStation().getId())
                .collect(Collectors.toList());
        assertThat(stationIds).containsExactlyElementsOf(Lists.newArrayList(1L, 2L, 3L));
    }

    @DisplayName("지하철 노선에 여러개의 역을 순서없이 등록한다.")
    @Test
    void addLineStationInAnyOrder() {
        // given
        ExtractableResponse<Response> createdLineResponse = 지하철_노선_등록되어_있음("2호선", "GREEN",
                LocalTime.of(5, 30), LocalTime.of(23, 30), 5);
        ExtractableResponse<Response> createdStationResponse1 = 지하철역_등록되어_있음("강남역");
        ExtractableResponse<Response> createdStationResponse2 = 지하철역_등록되어_있음("역삼역");
        ExtractableResponse<Response> createdStationResponse3 = 지하철역_등록되어_있음("선릉역");

        // when
        // 지하철_노선에_지하철역_등록_요청
        Long lineId = createdLineResponse.as(LineResponse.class).getId();
        Long stationId1 = createdStationResponse1.as(StationResponse.class).getId();
        ExtractableResponse<Response> lineStationResponse = 지하철_노선에_지하철역_등록_요청(lineId, null, stationId1, 4, 2);

        Long stationId2 = createdStationResponse2.as(StationResponse.class).getId();
        지하철_노선에_지하철역_등록_요청(lineId, stationId1, stationId2, 4, 2);

        Long stationId3 = createdStationResponse3.as(StationResponse.class).getId();

        지하철_노선에_지하철역_등록_요청(lineId, stationId1, stationId3, 4, 2);

        // then
        assertThat(lineStationResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        // when
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(lineId);

        LineResponse lineResponse = response.as(LineResponse.class);
        assertThat(lineResponse).isNotNull();
        assertThat(lineResponse.getStations()).hasSize(3);
        List<Long> stationIds = lineResponse.getStations().stream()
                .map(it -> it.getStation().getId())
                .collect(Collectors.toList());
        assertThat(stationIds).containsExactlyElementsOf(Lists.newArrayList(1L, 3L, 2L));
    }

}
