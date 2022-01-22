package nextstep.subway.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.acceptance.testenum.TestLine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

import static nextstep.subway.acceptance.testenum.TestLine.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("지하철 노선 관리 기능")
class LineAcceptanceTest extends AcceptanceTest {
    private final String BODY_ELEMENT_NAME = "name";
    private final String BODY_ELEMENT_COLOR = "color";

    private Map<String, String> createRequestBody(TestLine line) {
        Map<String, String> params = new HashMap<>();
        params.put(BODY_ELEMENT_NAME, line.getName());
        params.put(BODY_ELEMENT_COLOR, line.getColor());

        return params;
    }

    private ExtractableResponse<Response> postOneLine(TestLine line) {
        Map<String, String> params = createRequestBody(line);

        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)

                .when()
                .post("/lines")

                .then().log().all()
                .extract();
        return response;
    }

    private void verifyResponseBodyElement(ExtractableResponse<Response> response, String name, String color) {
        assertThat(response.body().jsonPath().getString(BODY_ELEMENT_NAME)).isEqualTo(name);
        assertThat(response.body().jsonPath().getString(BODY_ELEMENT_COLOR)).isEqualTo(color);
    }

    private void verifyResponseStatus(ExtractableResponse<Response> response, HttpStatus status) {
        assertThat(response.statusCode()).isEqualTo(status.value());
    }

    private ExtractableResponse<Response> getLineList() {
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .accept(MediaType.APPLICATION_JSON_VALUE)

                .when()
                .get("/lines")

                .then().log().all()
                .extract();
        return response;
    }

    private ExtractableResponse<Response> getOneLine(Long id) {
        return RestAssured
                .given().log().all()
                .accept(MediaType.APPLICATION_JSON_VALUE)

                .when()
                .get("/lines/" + id)

                .then().log().all()
                .extract();
    }

    private long extractId(ExtractableResponse<Response> responseByPost) {
        return responseByPost.body().jsonPath().getLong("id");
    }

    private ExtractableResponse<Response> patchOneLine(String lineName, String lineColor, Long id) {
        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL_VALUE)
                .body(patchRequestBody(lineName, lineColor))

                .when()
                .patch("/lines/" + id)

                .then().log().all()
                .extract();
    }

    private Map<String, String> patchRequestBody(String lineName, String lineColor) {
        Map<String, String> params = new HashMap<>(1);
        params.put("name", lineName);
        params.put("color", lineColor);
        return params;
    }

    private ExtractableResponse<Response> deleteLine(ExtractableResponse<Response> responseByPost) {
        ExtractableResponse<Response> response = RestAssured
                .given()
                .accept(MediaType.ALL_VALUE)

                .when()
                .delete("/lines/" + extractId(responseByPost))

                .then().log().all()
                .extract();
        return response;
    }

    /**
     * When 지하철 노선 생성을 요청 하면
     * Then 지하철 노선 생성이 성공한다.
     */
    @DisplayName("지하철 노선 생성")
    @Test
    void createLine() {
        // when
        ExtractableResponse<Response> response = postOneLine(LINE_NEW_BOONDANG);

        // then
        verifyResponseStatus(response, HttpStatus.CREATED);
        verifyResponseBodyElement(response, LINE_NEW_BOONDANG.getName(), LINE_NEW_BOONDANG.getColor());
    }

    /**
     * Given 지하철 노선 생성을 요청 하고
     * Given 새로운 지하철 노선 생성을 요청 하고
     * When 지하철 노선 목록 조회를 요청 하면
     * Then 두 노선이 포함된 지하철 노선 목록을 응답받는다
     */
    @DisplayName("지하철 노선 목록 조회")
    @Test
    void getLines() {
        // given
        postOneLine(LINE_NEW_BOONDANG);
        // given
        postOneLine(LINE_TWO);

        // when
        ExtractableResponse<Response> response = getLineList();

        // then
        verifyResponseStatus(response, HttpStatus.OK);

        assertThat(response.body().jsonPath().getList("name"))
                .containsExactly(LINE_NEW_BOONDANG.getName(), LINE_TWO.getName());
        assertThat(response.body().jsonPath().getList("color"))
                .containsExactly(LINE_NEW_BOONDANG.getColor(), LINE_TWO.getColor());
    }

    /**
     * Given 지하철 노선 생성을 요청 하고
     * When 생성한 지하철 노선 조회를 요청 하면
     * Then 생성한 지하철 노선을 응답받는다
     */
    @DisplayName("지하철 노선 조회")
    @Test
    void getLine() {
        // given
        ExtractableResponse<Response> responseByPost = postOneLine(LINE_NEW_BOONDANG);

        // when
        ExtractableResponse<Response> response = getOneLine(extractId(responseByPost));

        // then
        verifyResponseStatus(response, HttpStatus.OK);
        verifyResponseBodyElement(response, LINE_NEW_BOONDANG.getName(), LINE_NEW_BOONDANG.getColor());
    }

    /**
     * Given 지하철 노선 생성을 요청 하고
     * When 지하철 노선의 정보 수정을 요청 하면
     * Then 지하철 노선의 정보 수정은 성공한다.
     */
    @DisplayName("지하철 노선 수정")
    @Test
    void updateLine() {
        String modifyName = "구분당선";
        String modifyColor = "bg-blue-600";

        // given
        ExtractableResponse<Response> responseByPost = postOneLine(LINE_NEW_BOONDANG);

        // when
        ExtractableResponse<Response> response =
                patchOneLine(modifyName, modifyColor, extractId(responseByPost));

        // then
        verifyResponseStatus(response, HttpStatus.OK);
        verifyResponseBodyElement(getOneLine(extractId(responseByPost)), modifyName, modifyColor);
    }



    /**
     * Given 지하철 노선 생성을 요청 하고
     * When 생성한 지하철 노선 삭제를 요청 하면
     * Then 생성한 지하철 노선 삭제가 성공한다.
     */
    @DisplayName("지하철 노선 삭제")
    @Test
    void deleteLine() {
        // given
        ExtractableResponse<Response> responseByPost = postOneLine(LINE_NEW_BOONDANG);

        // when
        ExtractableResponse<Response> response = deleteLine(responseByPost);

        // then
        verifyResponseStatus(response, HttpStatus.NO_CONTENT);
    }
}
