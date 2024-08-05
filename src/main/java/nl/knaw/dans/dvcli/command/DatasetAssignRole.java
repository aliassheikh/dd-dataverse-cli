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
package nl.knaw.dans.dvcli.command;

import nl.knaw.dans.dvcli.action.ConsoleReport;
import nl.knaw.dans.dvcli.action.Pair;
import nl.knaw.dans.dvcli.action.ThrowingFunction;
import nl.knaw.dans.lib.dataverse.DatasetApi;
import nl.knaw.dans.lib.dataverse.DataverseException;
import nl.knaw.dans.lib.dataverse.model.RoleAssignment;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Command(name = "role-assignment",
         mixinStandardHelpOptions = true,
         description = "Manage role assignments on one or more datasets.")
public class DatasetAssignRole extends AbstractCmd {
    @ParentCommand
    private DatasetCmd datasetCmd;

    static class SingleAssignment {
        @Parameters(index = "0", paramLabel = "role", description = "The role to assign")
        String role;

        @Parameters(index = "1", paramLabel = "assigsment", description = "The identifier of the user to assign the role to")
        String assigsment;
    }

    static class AllArgs {
        @ArgGroup(exclusive = false)
        SingleAssignment singleAssignment;

        @Option(names = { "-f", "--parameters-file" }, paramLabel = "parameter-file", description = "CSV file to read parameters from. The file should have a header row with columns 'PID', 'ROLE', and 'ASSIGNMENT'.")
        Path paramsFile;
    }

    @ArgGroup(exclusive = true, multiplicity = "1")
    private AllArgs allArgs;

    private record RoleAssignmentParams(DatasetApi collection, String role, String assignee) {
    }

    private static class RoleAssignmentAction implements ThrowingFunction<RoleAssignmentParams, String, Exception> {
        @Override
        public String apply(RoleAssignmentParams roleAssignmentParams) throws IOException, DataverseException {
            var assignment = new RoleAssignment();
            assignment.setAssignee(roleAssignmentParams.assignee());
            assignment.setRole(roleAssignmentParams.role());
            var r = roleAssignmentParams.collection.assignRole(assignment);
            return r.getEnvelopeAsString();
        }
    }

    private List<Pair<String, RoleAssignmentParams>> readFromFile(Path file) throws IOException {
        try (Reader reader = Files.newBufferedReader(file);
            CSVParser csvParser = new CSVParser(reader, CSVFormat.Builder.create(CSVFormat.DEFAULT)
                .setHeader("PID", "ROLE", "ASSIGNEE")
                .setSkipHeaderRecord(true)
                .build())) {

            List<Pair<String, RoleAssignmentParams>> result = new ArrayList<>();

            for (CSVRecord csvRecord : csvParser) {
                var pid = csvRecord.get("PID");
                DatasetApi collection = datasetCmd.dataverseClient.dataset(pid);
                String role = csvRecord.get("ROLE");
                String assignee = csvRecord.get("ASSIGNEE");

                RoleAssignmentParams params = new RoleAssignmentParams(collection, role, assignee);
                result.add(new Pair<>(pid, params));
            }

            return result;
        }
    }

    private List<Pair<String, RoleAssignmentParams>> getRoleAssignmentParams() throws IOException {
        if (allArgs.paramsFile != null) {
            return readFromFile(allArgs.paramsFile);
        }
        else {
            return datasetCmd.getItems().stream()
                .map(p -> new Pair<>(p.getFirst(), new RoleAssignmentParams(p.getSecond(), allArgs.singleAssignment.role, allArgs.singleAssignment.assigsment)))
                .toList();
        }
    }

    @Override
    public void doCall() throws IOException, DataverseException {
        datasetCmd.<RoleAssignmentParams> paramsBatchProcessorBuilder()
            .labeledItems(getRoleAssignmentParams())
            .action(new RoleAssignmentAction())
            .report(new ConsoleReport<>())
            .build()
            .process();
    }
}
