package nextstep.subway.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import javax.transaction.Transactional;
import nextstep.subway.acceptance.utils.DatabaseCleanup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

    @Test
    public void addSection() {
        // given
        Line 신분당선 = createLine("신분당선", "red", createSection(강남역, 신논현역, (long) 15));
        Section newSection = createSection(신논현역, 양재역, (long) 15);

        // when
        신분당선.addSection(newSection);

        // then
        assertThat(신분당선.getStations()).containsExactly(강남역, 신논현역, 양재역);
    }

    @Test
    void getTotalDistance() {
        // given
        long firstSectionDistance = 110;
        long secondSectionDistance = 200;
        Line 신분당선 = createLine("신분당선", "red", createSection(강남역, 신논현역, firstSectionDistance));
        신분당선.addSection(createSection(신논현역, 양재역, secondSectionDistance));

        // when
        long totalDistance = 신분당선.getTotalDistance();

        assertThat(totalDistance).isEqualTo(firstSectionDistance + secondSectionDistance);
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