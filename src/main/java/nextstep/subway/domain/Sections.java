package nextstep.subway.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sections {
    private static final int MIN_SECTION_SIZE = 2;

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Section> sections = new ArrayList<>();

    public Sections(Line line, Section section) {
        section.setLine(line);
        sections.add(section);
    }

    protected void addSection(Line line, Section newSection) {
        checkDuplicationSectionOrStation(newSection);

        if (isNotTerminalDownStation(newSection)) {
            throw new IllegalArgumentException("새로운 구간은 기존 노선의 하행 종점역이어야 합니다.");
        }
        sections.add(newSection);
        newSection.setLine(line);
    }

    private void checkDuplicationSectionOrStation(Section newSection) {
        this.sections.stream()
                .filter(section -> section.isSameSection(newSection)
                        || isNewSectionDownStationInLine(newSection, section))
                .findFirst()
                .ifPresent(section -> {
                    throw new IllegalArgumentException("구간이나 역이 중복되어 있으면 등록할 수 없습니다.");
                });
    }

    protected Section deleteSection(Station station) {
        if (this.sections.size() < MIN_SECTION_SIZE) {
            throw new IllegalArgumentException("지하철 구간이 적어도 2개 이상은 있어야 삭제할 수 있습니다.");
        }
        if (this.getDownStationTerminal() != station) {
            throw new IllegalArgumentException("하행종점역만 삭제할 수 있습니다.");
        }
        Section section = getSectionByDownStation(station);

        sections.remove(section);
        return section;
    }

    protected List<Station> getStations() {
        if (this.sections.isEmpty()) {
            return Collections.emptyList();
        }

        List<Station> stations = this.sections.stream()
                .map(Section::getDownStation)
                .collect(Collectors.toList());
        stations.add(0, this.getUpStationTerminal());

        return stations;
    }

    protected Station getUpStationTerminal() {
        return getUpSectionTerminal().getUpStation();
    }

    protected Station getDownStationTerminal() {
        return getDownSectionTerminal().getDownStation();
    }

    private Section getDownSectionTerminal() {
        List<Station> upStations = getUpStations();
        List<Station> downStations = getDownStations();

        downStations.removeAll(upStations);
        Station downStation = downStations.get(0);
        return sections.stream()
                .filter(section -> section.getDownStation().equals(downStation))
                .collect(Collectors.toList())
                .get(0);
    }

    private Section getUpSectionTerminal() {
        List<Station> upStations = getUpStations();
        List<Station> downStations = getDownStations();

        upStations.removeAll(downStations);
        Station upStation = upStations.get(0);
        return sections.stream()
                .filter(section -> section.getUpStation().equals(upStation))
                .collect(Collectors.toList())
                .get(0);
    }

    private List<Station> getUpStations() {
        return sections.stream()
                .map(Section::getUpStation)
                .collect(Collectors.toList());
    }

    private List<Station> getDownStations() {
        return sections.stream()
                .map(Section::getDownStation)
                .collect(Collectors.toList());
    }

    private Section getSectionByDownStation(Station downStation) {
        return sections.stream()
                .filter(e -> e.getDownStation().equals(downStation))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("해당되는 구간을 찾을 수 없습니다."));
    }

    private boolean isNewSectionDownStationInLine(Section newSection, Section section) {
        return section.getUpStation() == newSection.getDownStation()
                || section.getDownStation() == newSection.getDownStation();
    }

    private boolean isNotTerminalDownStation(Section newSection) {
        return getDownStationTerminal() != newSection.getUpStation();
    }
}
