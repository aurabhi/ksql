/*
 * Copyright 2020 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.confluent.ksql.schema.ksql;

import io.confluent.ksql.util.KsqlConstants;
import io.confluent.ksql.util.timestamp.PartialStringToTimestampParser;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Helpers for working with SQL time types.
 */
public final class SqlTimeTypes {

  private static PartialStringToTimestampParser PARSER = new PartialStringToTimestampParser();

  private SqlTimeTypes() {
  }

  /**
   * Parse a SQL timestamp from a string.
   *
   * <p>Rejects {@code Infinity} and {@code Nan} as invalid.
   *
   * @param str the string to parse.
   * @return the double value.
   */
  public static Timestamp parseTimestamp(final String str) {
    return PARSER.parseToTimestamp(str);
  }

  public static String formatTimestamp(final Timestamp timestamp) {
    return DateTimeFormatter
        .ofPattern(KsqlConstants.DATE_TIME_PATTERN)
        .withZone(ZoneId.of("Z"))
        .format(timestamp.toInstant());
  }

  public static String formatTime(final Time time) {
    return LocalTime.ofSecondOfDay(time.getTime() / 1000).toString();
  }

  public static String formatDate(final Date date) {
    return LocalDate.ofEpochDay(TimeUnit.MILLISECONDS.toDays(date.getTime())).toString();
  }
}
