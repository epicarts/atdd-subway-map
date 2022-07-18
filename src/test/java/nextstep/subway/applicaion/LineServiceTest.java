package nextstep.subway.applicaion;

import static org.assertj.core.api.Assertions.assertThat;

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
}