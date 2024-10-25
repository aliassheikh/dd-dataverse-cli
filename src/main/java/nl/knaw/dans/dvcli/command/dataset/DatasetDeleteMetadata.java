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
package nl.knaw.dans.dvcli.command.dataset;

import lombok.Value;
import nl.knaw.dans.dvcli.action.Pair;
import nl.knaw.dans.dvcli.action.ThrowingFunction;
import nl.knaw.dans.dvcli.command.AbstractCmd;
import nl.knaw.dans.dvcli.inputparsers.FieldValuesParamsFileParser;
import nl.knaw.dans.dvcli.inputparsers.FieldValuesParser;
import nl.knaw.dans.lib.dataverse.DatasetApi;
import nl.knaw.dans.lib.dataverse.model.dataset.FieldList;
import nl.knaw.dans.lib.dataverse.model.dataset.MetadataField;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Command(name = "delete-metadata",
         mixinStandardHelpOptions = true,
         description = """
             Delete metadata fields from a dataset. The fields to delete can be specified as command line options or in a CSV file. The dataset will be in draft state after the operation.
             """)
public class DatasetDeleteMetadata extends AbstractCmd {
    @ParentCommand
    private DatasetCmd datasetCmd;

    static class FieldValueOrParameterFile {
        @Option(names = { "-f",
            "--field-value" }, description = "Field name and value to delete. If the field is a compound field, multiple field-values specified together will be treated as a single compound field. "
            + "If you need to delete multiple values from the same field, you have to call this command multiple times. "
            + "The format is: field-name=field-value. For example, to delete a field named 'alternativeTitle' with value 'Some title', use --field-value 'alternativeTitle=Some title'. "
            + "For compound fields, the field name must be prefixed with the field name of the parent field e.g., 'author.authorName' for the subfield 'authorName' of the compound field 'author'. "
            + "If the field is repeatable, you must add an asterisk (*) at the end of the field name.")
        private List<String> fieldValues;

        @Option(names = { "-p", "--parameters-file" }, description = "Path to a CSV file containing the field names and values to delete. The file must have a header row with the field names. "
            + "Each subsequent row must contain the field values. There must be a column 'PID' containing the dataset persistent identifier. The other column headers must match field names in "
            + "the dataset metadata. Compound fields must be specified as 'parentField.childField'. If you need to delete multiple fields from one dataset, use multiple rows in the CSV file.")
        private Path parametersFile;
    }

    @ArgGroup(multiplicity = "1")
    private FieldValueOrParameterFile fieldValueOrParameterFile;

    private static class DeleteMetadataAction implements ThrowingFunction<DeleteMetadataParams, String, Exception> {
        @Override
        public String apply(DeleteMetadataParams deleteMetadataParams) throws Exception {
            var fieldList = new FieldList(deleteMetadataParams.fieldValues.stream().toList());
            deleteMetadataParams.api.deleteMetadata(fieldList, Collections.emptyMap());
            return "Delete metadata";
        }
    }

    @Value
    private static class DeleteMetadataParams {
        DatasetApi api;
        Set<MetadataField> fieldValues;
    }

    @Override
    public void doCall() throws Exception {
        datasetCmd.<DeleteMetadataParams> paramsBatchProcessorBuilder()
            .labeledItems(getLabeledItems())
            .action(new DeleteMetadataAction())
            .build()
            .process();
    }

    private Stream<Pair<String, DeleteMetadataParams>> getLabeledItems() {
        try {
            if (fieldValueOrParameterFile.fieldValues != null) {
                var keyValues = new HashMap<String, String>();
                for (var fieldValue : fieldValueOrParameterFile.fieldValues) {
                    var split = fieldValue.split("=", 2);
                    keyValues.put(split[0], split[1]);
                }
                return datasetCmd.getItems().stream()
                    .map(p -> new Pair<>(p.getFirst(), new FieldValuesParser(keyValues).parse()))
                    .map(p -> new Pair<>(p.getFirst(), new DeleteMetadataParams(datasetCmd.getDataverseClient().dataset(p.getFirst()), p.getSecond())));

            }
            else if (fieldValueOrParameterFile.parametersFile != null) {
                return new FieldValuesParamsFileParser(fieldValueOrParameterFile.parametersFile)
                    .parse()
                    .map(p -> new Pair<>(p.getFirst(), new DeleteMetadataParams(datasetCmd.getDataverseClient().dataset(p.getFirst()), p.getSecond())));
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Error parsing field values or parameter file.", e);
        }
        throw new IllegalArgumentException("No field values or parameter file specified.");
    }
}
