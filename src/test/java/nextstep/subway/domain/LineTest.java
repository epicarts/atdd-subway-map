package nextstep.subway.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import javax.transaction.Transactional;
import nextstep.subway.acceptance.utils.DatabaseCleanup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    private Station 강남역;
    private Station 신논현역;
    private Station 양재역;
    private Line 신분당선;

    @BeforeEach
    public void setUp() {
        databaseCleanup.execute();

        강남역 = Station.builder().name("강남역").build();
        신논현역 = Station.builder().name("신논현역").build();
        양재역 = Station.builder().name("양재역").build();
    }

    @Test
    void updateNameAndColor() {
        // given
        신분당선 = Line.createLine("신분당선", "red", Section.createSection(강남역, 신논현역, (long) 15));
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
        신분당선 = Line.createLine("신분당선", "red", Section.createSection(강남역, 신논현역, (long) 15));
        Section newSection = Section.createSection(신논현역, 양재역, (long) 15);

        // when
        신분당선.addSection(newSection);

        // then
        assertThat(신분당선.getStations()).containsExactly(강남역, 신논현역, 양재역);
    }
}