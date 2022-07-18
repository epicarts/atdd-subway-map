package nextstep.subway.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import javax.transaction.Transactional;
import nextstep.subway.acceptance.utils.DatabaseCleanup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@DisplayName("Line 도메인 객체 테스트")
@SpringBootTest
@Transactional
class LineTest {
    @Autowired
    DatabaseCleanup databaseCleanup;

    @Autowired
    LineRepository lineRepository;

    @Autowired
    StationRepository stationRepository;

    @Autowired
    SectionRepository sectionRepository;

    private Station 강남역;
    private Station 신논현역;
    private Station 양재역;

    @BeforeEach
    public void setUp() {
        databaseCleanup.execute();

        강남역 = createStation("강남역");
        신논현역 = createStation("신논현역");
        양재역 = createStation("양재역");
    }

    @DisplayName("이름과 색깔 수정")
    @Test
    void updateNameAndColor() {
        // given
        Line 신분당선 = createLine("신분당선", "red", createSection(강남역, 신논현역, (long) 15));
        lineRepository.save(신분당선);

        String newColor = "blue";
        String newName = "1호선";

        // when
        신분당선.updateNameAndColor(newName, newColor);

        // then
        Line updatedLine = lineRepository.findById(신분당선.getId()).orElseThrow(RuntimeException::new);

        assertAll(
                () -> assertThat(updatedLine.getColor()).isEqualTo(newColor),
                () -> assertThat(updatedLine.getName()).isEqualTo(newName)
        );
    }

    @DisplayName("구간 추가")
    @Test
    public void addSection() {
        // given
        Line 신분당선 = createLine("신분당선", "red", createSection(강남역, 신논현역, (long) 15));
        Section newSection = createSection(신논현역, 양재역, (long) 15);

        // when
        신분당선.addSection(newSection);

        // then
        assertAll(
                () -> assertThat(신분당선.getStations()).hasSize(3),
                () -> assertThat(신분당선.getStations()).containsExactly(강남역, 신논현역, 양재역)
        );
    }

    @DisplayName("중복 구간 추가")
    @Test
    public void addSectionWithDuplicateSection() {
        // given
        Line 신분당선 = createLine("신분당선", "red", createSection(강남역, 신논현역, (long) 15));
        Section newSection = createSection(강남역, 신논현역, (long) 15);

        // when
        // then
        assertThatThrownBy(() -> 신분당선.addSection(newSection))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("상행 종점역 가져오기")
    @Test
    void getUpStationTerminal() {
        // given
        Station 상행종점역 = createStation("상행종점역");
        Line 신분당선 = createLine("신분당선", "red", createSection(상행종점역, 신논현역, (long) 10));
        신분당선.addSection(createSection(신논현역, 양재역, (long) 15));

        // when
        Station updatedUpStationTerminal = lineRepository.findById(신분당선.getId())
                .orElseThrow(RuntimeException::new)
                .getUpStationTerminal();

        // then
        assertAll(
                () -> assertThat(updatedUpStationTerminal.getId()).isEqualTo(상행종점역.getId()),
                () -> assertThat(updatedUpStationTerminal.getName()).isEqualTo(상행종점역.getName())
        );
    }

    @DisplayName("하행 종점역 가져오기")
    @Test
    void getDownStationTerminal() {
        // given
        Station 하행종점역 = createStation("하행종점역");
        Line 신분당선 = createLine("신분당선", "red", createSection(강남역, 신논현역, (long) 10));
        신분당선.addSection(createSection(신논현역, 하행종점역, (long) 15));

        // when
        Station downStationTerminal = lineRepository.findById(신분당선.getId())
                .orElseThrow(RuntimeException::new)
                .getDownStationTerminal();

        // then
        assertAll(
                () -> assertThat(downStationTerminal.getId()).isEqualTo(하행종점역.getId()),
                () -> assertThat(downStationTerminal.getName()).isEqualTo(하행종점역.getName())
        );
    }

    @DisplayName("구간 삭제")
    @Test
    void deleteSection() {
        // given
        Line 신분당선 = createLine("신분당선", "red", createSection(강남역, 신논현역, (long) 10));
        신분당선.addSection(createSection(신논현역, 양재역, (long) 15));

        // when
        신분당선.deleteSection(양재역);

        // then
        assertAll(
                () -> assertThat(신분당선.getStations()).hasSize(2),
                () -> assertThat(신분당선.getStations()).containsExactly(강남역, 신논현역)
        );
    }

    private Station createStation(String name) {
        Station station = Station.builder().name(name).build();
        stationRepository.save(station);
        return station;
    }

    private Line createLine(String name, String color, Section section) {
        Line line = Line.createLine(name, color, section);
        lineRepository.save(line);
        return line;
    }

    private Section createSection(Station upStation, Station downStation, Long distance) {
        Section section = Section.createSection(upStation, downStation, distance);
        sectionRepository.save(section);
        return section;
    }
}