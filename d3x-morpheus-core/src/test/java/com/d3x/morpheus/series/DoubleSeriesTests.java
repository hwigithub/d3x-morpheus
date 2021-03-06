/*
 * Copyright (C) 2018-2019 D3X Systems - All Rights Reserved
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
package com.d3x.morpheus.series;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import com.d3x.core.json.JsonEngine;
import com.d3x.morpheus.util.IO;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for data series
 *
 * @author Xavier Witdouck
 */
public class DoubleSeriesTests {


    private static JsonEngine jsonEngine = new JsonEngine();

    static {
        DataSeriesJson.registerDefaults(jsonEngine);
        DoubleSeriesJson.registerDefaults(jsonEngine);
    }


    private IntFunction ofIntFunction(IntFunction function) {
        return function;
    }


    @DataProvider(name="types")
    public Object[][] types() {
        var now = LocalDate.now();
        return new Object[][] {
            { Integer.class, ofIntFunction(i -> i) },
            { LocalDate.class, ofIntFunction(now::plusDays)},
            { String.class, ofIntFunction(i -> "X" + i) }
        };
    }


    @Test()
    public void csvRead() {
        var path = "/csv/aapl.csv";
        var series = DoubleSeries.<LocalDate>read(path).csv("Date", "Adj Close");
        Assert.assertEquals(series.valueClass(), Double.class);
        Assert.assertEquals(series.keyClass(), LocalDate.class);
        Assert.assertEquals(series.size(), 8503);
        Assert.assertEquals(series.firstKey().orNull(), LocalDate.parse("1980-12-12"));
        Assert.assertEquals(series.lastKey().orNull(), LocalDate.parse("2014-08-29"));
        Assert.assertEquals(series.getDouble(LocalDate.parse("1980-12-12")), 0.44203d, 0.000001d);
        Assert.assertEquals(series.getDouble(LocalDate.parse("2014-08-29")), 101.65627d, 0.000001d);
        Assert.assertEquals(series.stats().min(), 0.16913d, 0.000001d);
        Assert.assertEquals(series.stats().max(), 101.65627d, 0.000001d);
    }


    @Test()
    @SuppressWarnings("unchecked")
    public void jsonIO() {
        var path = "/csv/aapl.csv";
        var series = DoubleSeries.<LocalDate>read(path).csv("Date", "Adj Close");
        var jsonIO = jsonEngine.io(DoubleSeries.ofType(LocalDate.class));
        var jsonString = jsonIO.toString(series);
        IO.println(jsonString);
        var result = ((DoubleSeries<LocalDate>)jsonIO.fromString(jsonString));
        Assert.assertEquals(result.size(), series.size());
        Assert.assertEquals(result.valueClass(), series.valueClass());
        Assert.assertEquals(result.keyClass(), series.keyClass());
        series.keys().forEach(key -> {
            var v1 = series.getDouble(key);
            var v2 = result.getDouble(key);
            Assert.assertEquals(v2, v1, 0.000001d);
        });
    }


    @Test()
    public void mapKeys() {
        var path = "/csv/aapl.csv";
        var series = DoubleSeries.<LocalDate>read(path).csv("Date", "Adj Close");
        var result = series.mapKeys(LocalDate.class, v -> v.minusDays(1));
        Assert.assertEquals(result.size(), series.size());
        Assert.assertEquals(result.valueClass(), series.valueClass());
        Assert.assertEquals(result.keyClass(), series.keyClass());
        for (int i=0; i<series.size(); ++i) {
            var mapped = result.getKey(i);
            var expected = series.getKey(i).minusDays(1);
            Assert.assertEquals(mapped, expected);
            Assert.assertEquals(result.getDoubleAt(i), series.getDoubleAt(i), 0.000001d);
        }
    }


    @Test()
    public void filterKeys() {
        var path = "/csv/aapl.csv";
        var series = DoubleSeries.<LocalDate>read(path).csv("Date", "Adj Close");
        var result1 = series.filter(v -> v.getDayOfWeek() == DayOfWeek.MONDAY);
        Assert.assertTrue(result1.size() > 0);
        Assert.assertTrue(result1.size() < series.size());
        Assert.assertEquals(result1.valueClass(), series.valueClass());
        Assert.assertEquals(result1.keyClass(), series.keyClass());
        result1.keys().forEach(key -> {
            Assert.assertEquals(key.getDayOfWeek(), DayOfWeek.MONDAY);
            Assert.assertEquals(result1.getDouble(key), series.getDouble(key));
        });
    }


    @Test()
    public void sorting1() {
        var path = "/csv/aapl.csv";
        var series = DoubleSeries.<LocalDate>read(path).csv("Date", "Adj Close");
        var sorted = series.copy();
        sorted.sort((i1, i2) -> {
            var v1 = sorted.getDoubleAt(i1);
            var v2 = sorted.getDoubleAt(i2);
            return Double.compare(v1, v2);
        });
        Assert.assertTrue(sorted.size() > 0);
        Assert.assertEquals(sorted.size(),  series.size());
        Assert.assertNotEquals(sorted.firstKey(), series.firstKey());
        Assert.assertEquals(sorted.lastKey(), series.lastKey());
        Assert.assertEquals(series.firstKey().orNull(), LocalDate.parse("1980-12-12"));
        Assert.assertEquals(series.lastKey().orNull(), LocalDate.parse("2014-08-29"));
        Assert.assertEquals(sorted.firstKey().orNull(), LocalDate.parse("1982-07-08"));
        Assert.assertEquals(sorted.lastKey().orNull(), LocalDate.parse("2014-08-29"));
        DoubleSeries.assertAscending(sorted);
    }


    @Test(dataProvider="types")
    public <K> void sorting(Class<K> keyType, IntFunction<K> keyGen) {
        var builder = DoubleSeries.builder(keyType).capacity(100);
        IntStream.range(0, 1000).forEach(i -> builder.putDouble(keyGen.apply(i), Math.random() * 100d));
        var series = builder.build();
        var sorted = series.copy();
        sorted.sort((i1, i2) -> {
            var v1 = sorted.getDoubleAt(i1);
            var v2 = sorted.getDoubleAt(i2);
            return Double.compare(v1, v2);
        });
        Assert.assertTrue(sorted.size() > 0);
        Assert.assertEquals(sorted.size(), series.size());
        Assert.assertNotEquals(sorted.firstKey(), series.firstKey());
        Assert.assertNotEquals(sorted.lastKey(), series.lastKey());
        DoubleSeries.assertAscending(sorted);
    }


    /*
    @Test()
    @SuppressWarnings("unchecked")
    public void strings() {
        var series = DataSeries.builder(Integer.class, String.class).putValue(1, "Hello").putValue(2, "World").build();
        Assert.assertTrue(series.size() > 0);
        Assert.assertEquals(series.valueClass(), String.class);
        Assert.assertEquals(series.keyClass(), Integer.class);
        var jsonIO = jsonEngine.io(series.type());
        var jsonString = jsonIO.toString(series);
        IO.println(jsonString);
        var result = ((DataSeries<Integer,String>)jsonIO.fromString(jsonString));
        Assert.assertEquals(result.size(), series.size());
        Assert.assertEquals(result.valueClass(), series.valueClass());
        Assert.assertEquals(result.valueType(), series.valueType());
        Assert.assertEquals(result.keyClass(), series.keyClass());
        Assert.assertEquals(result.keyType(), series.keyType());
        series.getKeys().forEach(key -> {
            var v1 = series.getValue(key);
            var v2 = result.getValue(key);
            Assert.assertEquals(v2, v1);
        });
    }

    */

}
