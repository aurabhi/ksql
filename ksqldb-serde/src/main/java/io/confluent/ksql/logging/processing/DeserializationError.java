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

package io.confluent.ksql.logging.processing;

import static java.util.Objects.requireNonNull;

import io.confluent.ksql.logging.processing.ProcessingLogMessageSchema.MessageType;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.data.Struct;

public class DeserializationError implements ProcessingLogger.ErrorMessage {

  private final Throwable exception;
  private final Optional<byte[]> record;
  private final String topic;
  private final boolean isKey;

  public DeserializationError(
      final Throwable exception,
      final Optional<byte[]> record,
      final String topic,
      final boolean isKey
  ) {
    this.exception = requireNonNull(exception, "exception");
    this.record = requireNonNull(record, "record");
    this.topic = requireNonNull(topic, "topic");
    this.isKey = isKey;
  }

  @Override
  public SchemaAndValue get(final ProcessingLogConfig config) {
    final Struct struct = new Struct(ProcessingLogMessageSchema.PROCESSING_LOG_SCHEMA)
        .put(ProcessingLogMessageSchema.TYPE, MessageType.DESERIALIZATION_ERROR.getTypeId())
        .put(ProcessingLogMessageSchema.DESERIALIZATION_ERROR, deserializationError(config));

    return new SchemaAndValue(ProcessingLogMessageSchema.PROCESSING_LOG_SCHEMA, struct);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final DeserializationError that = (DeserializationError) o;
    return Objects.equals(exception, that.exception)
        && Objects.equals(record, that.record)
        && Objects.equals(topic, that.topic)
        && isKey == that.isKey;
  }

  @Override
  public int hashCode() {
    return Objects.hash(exception, record, topic, isKey);
  }

  private Struct deserializationError(final ProcessingLogConfig config) {
    final Struct deserializationError = new Struct(MessageType.DESERIALIZATION_ERROR.getSchema())
        .put(
            ProcessingLogMessageSchema.DESERIALIZATION_ERROR_FIELD_TARGET,
            LoggingSerdeUtil.getRecordComponent(isKey))
        .put(
            ProcessingLogMessageSchema.DESERIALIZATION_ERROR_FIELD_MESSAGE,
            exception.getMessage())
        .put(
            ProcessingLogMessageSchema.DESERIALIZATION_ERROR_FIELD_CAUSE,
            LoggingSerdeUtil.getCause(exception))
        .put(
            ProcessingLogMessageSchema.DESERIALIZATION_ERROR_FIELD_TOPIC,
            topic);

    if (config.getBoolean(ProcessingLogConfig.INCLUDE_ROWS)) {
      deserializationError.put(
          ProcessingLogMessageSchema.DESERIALIZATION_ERROR_FIELD_RECORD_B64,
          record.map(Base64.getEncoder()::encodeToString).orElse(null)
      );
    }

    return deserializationError;
  }
}
