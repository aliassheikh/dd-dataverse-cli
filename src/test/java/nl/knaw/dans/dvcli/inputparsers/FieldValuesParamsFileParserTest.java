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

import nl.knaw.dans.dvcli.AbstractTestWithTestDir;
import nl.knaw.dans.dvcli.action.Pair;
import nl.knaw.dans.lib.dataverse.CompoundFieldBuilder;
import nl.knaw.dans.lib.dataverse.model.dataset.PrimitiveMultiValueField;
import nl.knaw.dans.lib.dataverse.model.dataset.PrimitiveSingleValueField;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class FieldValuesParamsFileParserTest extends AbstractTestWithTestDir {

    @Test
    public void parse_should_parse_csv_file_with_pid_and_one_field() throws Exception {
        FileUtils.writeStringToFile(testDir.resolve("params.csv").toFile(), """
            PID,field1
            doi:10.5072/dans-2a3-4b5,foo
            """, StandardCharsets.UTF_8);

        var result = new FieldValuesParamsFileParser(testDir.resolve("params.csv")).parse();
        assertThat(result).containsExactly(
            new Pair<>("doi:10.5072/dans-2a3-4b5", Set.of(new PrimitiveSingleValueField("field1", "foo")))
        );
    }

    @Test
    public void parse_should_parse_csv_file_with_pid_and_two_fields() throws Exception {
        FileUtils.writeStringToFile(testDir.resolve("params.csv").toFile(), """
            PID,field1,field2
            doi:10.5072/dans-2a3-4b5,foo,bar
            """, StandardCharsets.UTF_8);

        var result = new FieldValuesParamsFileParser(testDir.resolve("params.csv")).parse();
        assertThat(result).containsExactly(
            new Pair<>("doi:10.5072/dans-2a3-4b5", Set.of(
                new PrimitiveSingleValueField("field1", "foo"),
                new PrimitiveSingleValueField("field2", "bar")
            ))
        );
    }

    @Test
    public void parse_should_parse_csv_file_with_pid_and_two_fields_and_one_repeated_field() throws Exception {
        FileUtils.writeStringToFile(testDir.resolve("params.csv").toFile(), """
            PID,field1,field2,field3*
            doi:10.5072/dans-2a3-4b5,foo,bar,baz
            """, StandardCharsets.UTF_8);

        var result = new FieldValuesParamsFileParser(testDir.resolve("params.csv")).parse();
        assertThat(result).containsExactly(
            new Pair<>("doi:10.5072/dans-2a3-4b5", Set.of(
                new PrimitiveSingleValueField("field1", "foo"),
                new PrimitiveSingleValueField("field2", "bar"),
                new PrimitiveMultiValueField("field3", List.of("baz"))
            ))
        );
    }

    @Test
    public void parse_should_parse_csv_file_with_pid_and_compound_field() throws Exception {
        FileUtils.writeStringToFile(testDir.resolve("params.csv").toFile(), """
            PID,field1,parentField1*.subfieldA,parentField1*.subfieldB
            doi:10.5072/dans-2a3-4b5,foo,bar,baz
            """, StandardCharsets.UTF_8);

        var result = new FieldValuesParamsFileParser(testDir.resolve("params.csv")).parse();
        assertThat(result).containsExactly(
            new Pair<>("doi:10.5072/dans-2a3-4b5", Set.of(
                new PrimitiveSingleValueField("field1", "foo"),
                new CompoundFieldBuilder("parentField1", true)
                    .addSubfield("subfieldA", "bar")
                    .addSubfield("subfieldB", "baz")
                    .build()
            ))
        );
    }

}
