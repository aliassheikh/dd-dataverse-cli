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

import lombok.AllArgsConstructor;
import nl.knaw.dans.dvcli.action.Pair;
import nl.knaw.dans.lib.dataverse.model.dataset.MetadataField;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@AllArgsConstructor
public class FieldValuesParamsFileParser {
    private final Path csvFile;

    /**
     * Parse the parameters file and return a stream of pairs of PID and a list of MetadataFields. The client is expected to process the list of MetadataFields for the given PID. Note that the
     * MetadataField class actually represents a metadata field value, not merely field definition (although it does contain the field definition, such as repeatability).
     *
     * The parameters file must have the following format:
     *
     * <pre>
     * PID,field1,parentField1*.subfieldA,parentField1*.subfieldB <-- the header
     * doi:10.5072/dans-2a3-4b5,foo,bar,baz <-- a row
     * doi:10.5072/dans-2a3-4b5,foo,bar,baz <-- another row
     * </pre>
     *
     * The asterisk (*) is used to indicate that the field is multi-value, i.e. repeatable.
     *
     * @return a stream of pairs of PID and a list of MetadataFields
     */
    public Stream<Pair<String, Set<MetadataField>>> parse() {
        try {
            CSVParser parser = CSVParser.parse(csvFile, StandardCharsets.UTF_8,
                CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true).build());
            return StreamSupport.stream(parser.spliterator(), false).onClose(() -> {
                try {
                    parser.close();
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).map(record -> parseRecord(record, new HashSet<>(parser.getHeaderMap().keySet())));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Pair<String, Set<MetadataField>> parseRecord(CSVRecord record, Set<String> headers) {
        String pid = record.get("PID");
        if (pid == null || pid.isBlank()) {
            throw new IllegalArgumentException("PID is missing in the parameters file");
        }

        Map<String, String> keyValues = new HashMap<>();
        for (String header : headers) {
            if (header.equals("PID")) {
                continue;
            }
            keyValues.put(header, record.get(header));
        }

        return new Pair<>(pid, new FieldValuesParser(keyValues).parse());
    }
}
