package nextstep.subway.applicaion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import javax.transaction.Transactional;

import nextstep.subway.applicaion.dto.LineRequest;
import nextstep.subway.applicaion.dto.LineResponse;
import nextstep.subway.applicaion.dto.SectionRequest;
import nextstep.subway.applicaion.dto.StationRequest;
import nextstep.subway.applicaion.dto.StationResponse;
import nextstep.subway.domain.LineRepository;
import nextstep.subway.domain.SectionRepository;
import nextstep.subway.domain.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
public class LineServiceTest {
    @Autowired
    private LineRepository lineRepository;
    @Autowired
    private StationService stationService;
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private StationRepository stationRepository;

    private LineService lineService;
    private StationResponse 강남역;
    private StationResponse 역삼역;
    private StationResponse 선릉역;

    @BeforeEach
    void setUp() {
        강남역 = stationService.saveStation(new StationRequest("강남역"));
        역삼역 = stationService.saveStation(new StationRequest("역삼역"));
        선릉역 = stationService.saveStation(new StationRequest("선릉역"));
        lineService = new LineService(lineRepository, stationRepository, sectionRepository);
    }

    @Test
    void addSection() {
        // given
        LineResponse 이호선 = lineService
                .saveLine(new LineRequest("2호선", "green", 강남역.getId(), 역삼역.getId(), (long) 10));

        // when
        lineService.addSection(이호선.getId(), new SectionRequest(역삼역.getId(), 선릉역.getId(), (long) 10));

        // then
        LineResponse lineResponse = lineService.findLine(이호선.getId());
        assertThat(lineResponse.getStations()).containsExactly(강남역, 역삼역, 선릉역);
    }

    @Test
    void saveLine() {
        // given
        String lineName = "2호선";
        String lineColor = "green";
        LineRequest lineRequest = new LineRequest("2호선", "green", 강남역.getId(), 역삼역.getId(), (long) 10);

        // when
        LineResponse LineResponse = lineService.saveLine(lineRequest);

        // then
        LineResponse 이호선 = lineService.findLine(LineResponse.getId());
        assertAll(
                () -> assertThat(이호선.getColor()).isEqualTo(lineColor),
                () -> assertThat(이호선.getName()).isEqualTo(lineName),
                () -> assertThat(이호선.getStations()).containsExactly(강남역, 역삼역)
        );
    }
}