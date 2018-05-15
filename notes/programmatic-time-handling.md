---
title: Programmatic Time Handling
sansfont: DejaVu Sans
slide-numbers: true
links-as-notes: true
biblatex: true
biblatex-chicago: true
biblatexoptions: [notes, noibid]
bibliography: ../sources.bib
---

## Setting the stage

- Celebrate New Year's Eve 2016-2017 in Turkey
- Buy tickets in Summer 2016
- Return trip January 2 2017
- Store times as UTC, "heard it was a good idea"

## Setting the stage

Date       DST Offset
---------- --- ------
2016-01-02 No  UTC+02
2016-07-02 Yes UTC+03
2017-01-02 No  UTC+02

Table: Turkey zone offsets, expected per July 2016

## Setting the stage

Homebound plane:

> - Time of departure, Atatürk Airport: 2017-01-02T**14:00**
> - In UTC: 2017-01-02T**12:00**
> - On departure day, calendar says: 2017-01-02T**15:00**

\note{
- Calendar knows we're in Turkey
- Calendar has new tz database with +03 offset
}

## Setting the stage

Date       DST Offset
---------- --- ------
2016-01-02 No  UTC+02
2016-07-02 Yes UTC+03
2017-01-02 Yes UTC+03

Table: Turkey zone offsets, actual per August 2016

## "That never happens"

Year Count (approximate)
---- -------------------
2018 3 and counting
2017 6
2016 16
2015 9
2014 11
 ... ...

Table: Permanent offset changes\footnote{\cite{timeanddate:news}}

## "Well, it never happens *here*"

- It may:

> _The [European Parliament] approved a resolution to assess the merits of the
> yearly clock changes every spring and fall. The resolution passed the vote
> after 384 Members of the European Parliament (MEP) voted in favor, while 153
> MEPs voted against._\footnote{\cite{timeanddate:eu}}

- Even if it doesn't, are all your clients in the same time zone?

## Concepts

- **Instant:** A single point in time on the global time scale
    - Neither zoned nor local
- **Region**: Name of zone (usually) in the form *area/location*.
    - `Europe/Copenhagen`
    - Not `UTC`, `CEST`
- **UTC offset:** Time offset of region relative to UTC
    - `+01:30`
- **Local time**: Time-stamp without geographical anchor
    - `12:34:56`
- **Wall-clock time**: The *time* shown by a *clock* hanging on a *wall* at the
  location of interest.
    - `2018-04-01T12:34:56` in `Europe/Copenhagen`

## Simple rules...

1. Transmit all future-times in wall-clock time
1. Transmit past times in any known time zone\footnote{UTC is an easy,
   convenient choice with many advantages, but not a requirement.}

## ... hard to follow

- People don't know, so they do the wrong thing
- People did the wrong thing, so now you can't do the right thing
- No standard serialization format for wall-clock time

## Implementations

- java.time, NodaTime: `ZonedDateTime`
- PHP: `new DateTimeImmutable('now', new DateTimeZone('Europe/Copenhagen'))`

## I'm going to tell a lie

> - This advice will work ~99.97% of the time\footnote{If you grossly abuse
>   statistics, anyway.}
>     - Ambiguity in hour-of-DST transition requires three pieces of
>       information: date-time, region, _and UTC offset_
>     - Offset omitted for simplicity, will get back to it

## Practical advice: serialization

- Wall-clock time as (local time, region) tuple:
    - `('2018-04-01T12:13:14', 'Europe/Copenhagen')`

## Practical advice: storage

```sql
CREATE TABLE store_event (
    d DATE NOT NULL,
    t TIME NOT NULL,
    region VARCHAR(100) NOT NULL
);
INSERT INTO store_event(d, t, region)
VALUES ('2018-04-01', '12:13:14', 'Europe/Copenhagen');
```

- Works in PostgreSQL\footnote{\cite{postgres:datetime}}, SQLite, MS SQL, and
  MySQL derivatives.

## Practical advice: storage pitfalls

- SQL standard does time zones incorrectly\footnote{As of SQL:2016; don't
  expect this to change.}
- Avoid `... WITH TIME ZONE`
    - Automagic conversions between time zones, usually between
      "local"\footnote{Generally the DB server's time zone.} and UTC
    - Actually stores offset, never *wall-clock time*
- MySQL's `TIMESTAMP` is converted to UTC (not standards-compliant)
    - MySQL's `DATETIME` ≈ standard's `TIMESTAMP`
    - In other databases, `TIMESTAMP` is preferable

## Practical advice: load

```sql
SELECT d || 'T' || t as ts, region from store_event;
-- '2018-04-01T12:13:14', 'Europe/Copenhagen'
```

## Practical advice: transmit

```json
GET /store_events
{
  "timestamp": "2018-04-01T12:13:14",
  "region": "Europe/Copenhagen"
}
```

- ISO-8601 has *many* optional components so include example
  output\footnote{Preferably accurate examples.} in documentation
    - Anecdotally, most defaults expect presence of optional `T`

## Practical advice: read

```java
String ts = ...
String region = ...
ZonedDateTime timestamp = LocalDateTime
        .parse(ts)
        .atZone(ZoneId.of(region));
```

```php
$timestamp = new DateTimeImmutable(
        $ts,
        new DateTimeZone($region));
```

## Practical advice: store

1. Convert wall-clock time to (local time, region) tuple:
```java
LocalDateTime ts = timestamp.toLocalDateTime();
ZoneId zone = timestamp.getZone();
```
1. Serialize tuple:
```java
String region = zone.getId();
String dt =
        DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(ts);
String d =
        DateTimeFormatter.ISO_LOCAL_DATE.format(ts);
String t =
        DateTimeFormatter.ISO_LOCAL_TIME.format(ts);
```

## Practical advice: legacy store

1. Convert wall-clock time to instant:
```java
Instant i = timestamp.toInstant();
// Instant can be outside Date's range!
Date ts = Date.from(i);
```
```php
$timestamp = $timestamp->setTimezone(
        new DateTimeZone('UTC'));
$ts = $timestamp->format(...);
```
1. Pray

## Practical advice: legacy read

- Convert instant to wall-clock time:
```java
Date ts = ...
// Have to get a region from somewhere.
String region = ...
ZonedDateTime timestamp = ZonedDateTime.ofInstant(
        ts.toInstant(),
        ZoneId.of(region));
```
```php
$timestamp = new DateTimeImmutable(
        $ts,
        new DateTimeZone('UTC'));
$timestamp = $timestamp->setTimezone(
        new DateTimeZone($region));
```

## Practical advice: system

- Run server in UTC
    - Wall-clock time is convenient for human-beings...
        - Human-beings *where*?
    - ... inconvenient for systems
        - `cron` + DST = :(
    - But don't ask for "server time" or rely on "system time"
        - Globals outside your control
- UTC's imperfections are well understood and rarely critical

## How about that lie?

> - Tuple should actually be (local time, _offset_, region)
>     - ```sql
>     offset_seconds INTEGER NOT NULL
>     COMMENT 'Total UTC offset in seconds'
>     ```
> - Java: see
> ```java
> ZoneOffset::ofTotalSeconds(int)
> ZonedDateTime::ofInstant(
>         LocalDateTime, ZoneOffset, ZoneId)
> ZonedDateTime::ofStrict(
>         LocalDateTime, ZoneOffset, ZoneId)
> ZonedDateTime::withEarlierOffsetAtOverlap()
> ZonedDateTime::withLaterOffsetAtOverlap()
> ```
> - PHP: you're screwed?
> - No possible fix for ambiguous hours caused by offset changes

## Recap

- Future-times in wall-clock time
    - Represent as (local time, offset, region) tuple
    - ... or compromise on (local time, region) tuple
- Past times in whatever
    - Keep *Year 2038 problem* in mind
- System, diagnostics times in stable time zone
    - UTC, optionally with offset

## Resources

\begin{description}
    \item[\href{https://en.wikipedia.org/wiki/Tz_database}{tz ("Olson")
        database Wikipedia article}] \hfill \\
        How regions turn into offsets
    \item[\href{https://www.creativedeletion.com/}{Creative Deletion}] \hfill \\
        Blog with focus on time handling; where I learned about wall-clock time
    \item[\href{https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html}{java.time
        package description}] \hfill \\
        Entry point to lots of concept details, even more implementation
        details
    \item[\href{http://standards.iso.org/ittf/PubliclyAvailableStandards/c060394_ISO_IEC_TR_19075-2_2015.zip}{ISO/IEC TR 19075-2:2015}] \hfill \\
        SQL:2016 date and time information
\end{description}
