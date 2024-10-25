/*
 * Copyright (C) 2024 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
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
package nl.knaw.dans.dvcli.inputparsers;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CsvStream {
    private final Path csvFile;

    public CsvStream(Path csvFile) {
        this.csvFile = csvFile;
    }

    public Stream<CSVRecord> stream() throws IOException {
        CSVParser parser = CSVParser.parse(csvFile, StandardCharsets.UTF_8, CSVFormat.DEFAULT.builder().setSkipHeaderRecord(true).build());
        return StreamSupport.stream(parser.spliterator(), false).onClose(() -> {
            try {
                parser.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}