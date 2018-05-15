import java.time.*;
import java.time.format.*;

final class Turkey {
    public static void main(String[] args) {
        String ts = "2017-01-02T14:00:00";
        ZoneId region = ZoneId.of("Turkey");
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        ZonedDateTime departureTime = LocalDateTime
                .parse(ts)
                .atZone(region);

        StringBuilder buf = new StringBuilder(500);
        buf.append("- Celebrate NY eve 2016-2017 in Turkey\n");
        buf.append("- Buy tickets in summer 2016\n");
        buf.append("- Return trip jan 2 2017\n");
        buf.append("- Store times as UTC, \"heard it was a good idea\"\n");
        buf.append('\n');
        buf.append("Turkey offsets, expected per July 2016\n");
        buf.append("\t2016-01-02: UTC+2\n");
        buf.append("\t2016-07-02: UTC+3 (DST)\n");
        buf.append("\t2017-01-02: UTC+2\n");
        buf.append('\n');

        buf.append("Time of departure, Atatürk Airport:\t");
        buf.append(formatter.format(departureTime));
        buf.append('\n');

        // Emulate expected historical offset calculation.
        ZonedDateTime departureTimeUtc = departureTime
                .minusHours(2)
                .withZoneSameLocal(ZoneOffset.UTC);
        buf.append("In UTC:\t\t\t\t\t");
        buf.append(formatter.format(departureTimeUtc));
        buf.append('\n');

        ZonedDateTime departureTimeDerived =
                departureTimeUtc.withZoneSameInstant(region);
        buf.append("On departure day, calendar says:\t");
        buf.append(formatter.format(departureTimeDerived));
        buf.append('\n');

        buf.append('\n');
        buf.append("Turkey offsets, actual per August 2016\n");
        buf.append("\t2016-01-02: UTC+2\n");
        buf.append("\t2016-07-02: UTC+3 (DST)\n");
        buf.append("\t2017-01-02: UTC+3 (DST)\n");

        System.out.print(buf.toString());
    }
}
