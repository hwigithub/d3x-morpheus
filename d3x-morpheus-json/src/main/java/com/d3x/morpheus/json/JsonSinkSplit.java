/*
 * Copyright (C) 2014-2018 D3X Systems - All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.d3x.morpheus.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameColumn;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.frame.DataFrameRow;
import com.d3x.morpheus.frame.DataFrameValue;
import com.d3x.morpheus.util.IO;
import com.d3x.morpheus.util.text.Formats;
import com.d3x.morpheus.util.text.printer.Printer;
import com.google.gson.stream.JsonWriter;

/**
 * A JsonSink.Handler that outputs a format compatible with Pandas "split" orientation
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author Xavier Witdouck
 */
class JsonSinkSplit<R,C> implements JsonSink<R,C> {

    private JsonWriter writer;
    private JsonSink.Options options;


    @Override
    public synchronized void write(DataFrame<R,C> frame, Options options) {
        try {
            final String encoding = options.getEncoding();
            final OutputStream os = options.getResource().toOutputStream();
            this.options = options;
            this.writer = new JsonWriter(new OutputStreamWriter(os, encoding));
            this.writer.setIndent("  ");
            this.writer.beginObject();
            this.writer.name("columns");
            this.writer.beginArray();
            writeColKeys(frame);
            this.writer.endArray();
            this.writer.name("index");
            this.writer.beginArray();
            writeRowKeys(frame);
            this.writer.endArray();
            this.writer.name("data");
            this.writer.beginArray();
            writeRows(frame);
            this.writer.endArray();
            this.writer.endObject();
        } catch (Exception ex) {
            throw new DataFrameException("Failed to write DataFrame to JSON output", ex);
        } finally {
            IO.close(writer);
        }
    }


    /**
     * Writes the column keys for the frame
     * @param frame     the frame context
     * @throws IOException  if there is an I/O error
     */
    private void writeColKeys(DataFrame<?,?> frame) throws IOException {
        final Formats formats = options.getFormats();
        final Class<?> keyType = frame.cols().keyType();
        final Printer<Object> printer = formats.getPrinterOrFail(keyType);
        for (DataFrameColumn<?,?> column : frame.cols()) {
            writer.value(printer.apply(column.key()));
        }
    }


    /**
     * Writes out the row keys as a JSON array
     * @param frame     the frame context
     * @throws IOException  if there is an I/O error
     */
    private void writeRowKeys(DataFrame<?,?> frame) throws IOException {
        final Class<?> type = frame.rows().keyType();
        if (type.equals(Integer.class)) {
            for (DataFrameRow<?,?> row : frame.rows()) {
                writer.value((Integer)row.key());
            }
        } else if (type.equals(Long.class)) {
            for (DataFrameRow<?,?> row : frame.rows()) {
                writer.value((Long)row.key());
            }
        } else if (type.equals(Double.class)) {
            for (DataFrameRow<?,?> row : frame.rows()) {
                writer.value((Double)row.key());
            }
        } else if (type.equals(Number.class)) {
            for (DataFrameRow<?,?> row : frame.rows()) {
                writer.value((Number)row.key());
            }
        } else {
            final Formats formats = options.getFormats();
            final Printer<Object> printer = formats.getPrinterOrFail(type);
            for (DataFrameRow<?,?> row : frame.rows()) {
                final Object rowKey = row.key();
                final String value = printer.apply(rowKey);
                writer.value(value);
            }
        }
    }


    /**
     * Writes out the data columns for the frame
     * @param frame     the frame context
     * @throws IOException  if there is an I/O error
     */
    private void writeRows(DataFrame<?,?> frame) throws IOException {
        final Formats formats = options.getFormats();
        for (DataFrameRow<?,?> row : frame.rows()) {
            writer.beginArray();
            for (DataFrameValue<?,?> v : row) {
                final Object value = v.getValue();
                if (value instanceof Boolean) {
                    writer.value((Boolean)value);
                } else if (value instanceof Integer) {
                    writer.value((Integer)value);
                } else if (value instanceof Long) {
                    writer.value((Long)value);
                } else if (value instanceof Number) {
                    writer.value((Number)value);
                } else if (value == null) {
                    writer.value((String)null);
                } else {
                    final Class<?> dataType = value.getClass();
                    final Printer<Object> printer = formats.getPrinterOrFail(dataType);
                    writer.value(printer.apply(value));
                }
            }
            writer.endArray();
        }
    }

}
