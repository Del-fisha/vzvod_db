package practice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Getter
@Setter
public class EventDto implements Comparable<EventDto> {
    private String place;
    private String name;
    private LocalDate date;
    private LocalTime time;

    public EventDto() {}

    public EventDto(String place, String name, LocalDate date, LocalTime time) {
        this.place = place;
        this.name = name;
        this.date = date;
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EventDto eventDto = (EventDto) o;
        return Objects.equals(place, eventDto.place) && Objects.equals(name, eventDto.name) && Objects.equals(date, eventDto.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(place, name, date);
    }


    @Override
    public int compareTo(EventDto o) {
        if (o == null) return 1;

        int cmp = o.getDate().compareTo(this.date);
        if (cmp != 0) return cmp;

        cmp = safeCompare(this.place, o.place);
        if (cmp != 0) return cmp;

        return safeCompare(this.name, o.name);
    }

    private int safeCompare(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareToIgnoreCase(b);
    }

    @Override
    public String toString() {
        return String.format("Место: %s, Название: %s, Дата: %s, Время: %s", place, name, date, time);
    }
}
