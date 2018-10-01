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
package com.d3x.morpheus.reference;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Consumer;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameRead;
import com.d3x.morpheus.frame.DataFrameSource;
import com.d3x.morpheus.source.*;

/**
 * The default implementation of the DataFrame read interface
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
class XDataFrameRead implements DataFrameRead {

    /*
     * Static initializer
     */
    static {
        DataFrameSource.register(new CsvSource<>());
    }

    /**
     * Constructor
     */
    XDataFrameRead() {
        super();
    }

    @Override
    public <R> DataFrame<R, String> csv(File file) {
        return csv(options -> options.setFile(file));
    }

    @Override
    public <R> DataFrame<R, String> csv(URL url) {
        return csv(options -> options.setURL(url));
    }

    @Override
    public <R> DataFrame<R, String> csv(InputStream is) {
        return csv(options -> options.setInputStream(is));
    }

    @Override
    public <R> DataFrame<R,String> csv(String resource) {
        return csv(options -> options.setResource(resource));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> DataFrame<R,String> csv(Consumer<CsvSourceOptions<R>> configurator) {
        return DataFrameSource.lookup(CsvSource.class).read(configurator);
    }
}