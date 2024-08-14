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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Command(name = "role-assignment",
         mixinStandardHelpOptions = true,
         description = "Manage role assignments on one or more datasets.")
public class DatasetRoleAssignment extends AbstractCmd {
    @ParentCommand
    private DatasetCmd datasetCmd;

    static class AllArgs {
        @ArgGroup(exclusive = false)
        AssignmentCommand assignmentCommand;

        @Option(names = "list", description = "List role assignments for specified dataset")
        boolean list;
    }

    @ArgGroup(multiplicity = "1")
    private AllArgs allArgs;

    static class AssignmentCommand {
        static class AssignmentAction {
            @Option(names = "add", description = "add role assignment to specified dataset(s)'")
            boolean add;

            @Option(names = "remove", description = "remove role assignment from specified dataset(s)")
            boolean remove;
        }

        @ArgGroup(multiplicity = "1")
        AssignmentAction assignmentAction;

        static class AssignmentPhrase {
            @Parameters(description = "alias and role assignee (example: @dataverseAdmin=contributor)")
            String assignment = "";

            @Option(names = { "-f",
                "--parameters-file" }, description = "CSV file to read parameters from. The file should have a header row with columns 'PID', 'ROLE', and 'ASSIGNMENT'.")
            Path parametersFile;
        }

        @ArgGroup(multiplicity = "1")
        AssignmentPhrase assignmentPhrase;

        private Optional<AssignmentActionType> getAssignmentAction() {
            if (assignmentAction != null) {
                if (assignmentAction.add)
                    return Optional.of(AssignmentActionType.ADD);

                return Optional.of(AssignmentActionType.REMOVE);
            }
            return Optional.empty();
        }

        private Optional<RoleAssignment> readFromCommandLine() {
            if (!this.assignmentPhrase.assignment.isEmpty() && this.assignmentPhrase.assignment.contains("=")) {
                String[] assigneeRole = this.assignmentPhrase.assignment.split("=");

                RoleAssignment roleAssignment = new RoleAssignment();
                roleAssignment.setAssignee(assigneeRole[0]);
                roleAssignment.setRole(assigneeRole[1]);
                return Optional.of(roleAssignment);
            }
            return Optional.empty();
        }

        private List<Pair<String, RoleAssignmentParams>> readFromFile(DatasetCmd datasetCmd) throws IOException {
            try (BufferedReader reader = Files.newBufferedReader(assignmentPhrase.parametersFile);
                CSVParser csvParser = new CSVParser(reader, CSVFormat.Builder.create(CSVFormat.DEFAULT)
                    .setHeader("PID", "ASSIGNEE", "ROLE")
                    .setSkipHeaderRecord(true)
                    .build())) {

                List<Pair<String, RoleAssignmentParams>> result = new ArrayList<>();

                for (CSVRecord csvRecord : csvParser) {
                    var pid = csvRecord.get("PID");
                    DatasetApi datasetApi = datasetCmd.dataverseClient.dataset(pid);
                    RoleAssignment roleAssignment = new RoleAssignment();
                    roleAssignment.setRole(csvRecord.get("ROLE"));
                    roleAssignment.setAssignee(csvRecord.get("ASSIGNEE"));

                    RoleAssignmentParams params = new RoleAssignmentParams(datasetApi, Optional.of(roleAssignment), getAssignmentAction(), CommandAction.ASSIGNMENT);
                    result.add(new Pair<>(pid, params));
                }

                return result;
            }
        }

        private List<Pair<String, RoleAssignmentParams>> getAssignment(DatasetCmd datasetCmd) throws IOException {
            if (assignmentPhrase.parametersFile != null) {
                return readFromFile(datasetCmd);
            }
            else if (assignmentPhrase.assignment != null) {
                return datasetCmd.getItems().stream()
                    .map(p -> new Pair<>(p.getFirst(), new RoleAssignmentParams(p.getSecond(), readFromCommandLine(), getAssignmentAction(), CommandAction.ASSIGNMENT)))
                    .toList();
            }
            return List.of();
        }

    }

    enum AssignmentActionType {
        ADD, REMOVE
    }

    enum CommandAction {
        ASSIGNMENT, LIST
    }

    private record RoleAssignmentParams(DatasetApi pid, Optional<RoleAssignment> roleAssignment, Optional<AssignmentActionType> assignmentActionType, CommandAction commandAction) {
    }

    private static class RoleAssignmentAction implements ThrowingFunction<RoleAssignmentParams, String, Exception> {
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        @Override
        public String apply(RoleAssignmentParams roleAssignmentParams) throws IOException, DataverseException {
            switch (roleAssignmentParams.commandAction()) {
                case LIST: {
                    var r = roleAssignmentParams.pid().listRoleAssignments();
                    return r.getEnvelopeAsString();
                }

                case ASSIGNMENT: {
                    if (roleAssignmentParams.assignmentActionType().isPresent()
                        && roleAssignmentParams.roleAssignment().isPresent()) {
                        switch (roleAssignmentParams.assignmentActionType.get()) {
                            case ADD: {
                                var r = roleAssignmentParams.pid().assignRole(roleAssignmentParams.roleAssignment.get());
                                return r.getEnvelopeAsString();
                            }
                            case REMOVE: {
                                // TODO in DatasetApi: https://guides.dataverse.org/en/latest/api/native-api.html#delete-role-assignment-from-a-dataset
                                return "Couldn't process 'Remove RoleAssignment'";
                            }
                        }
                    }
                }
            }
            return "Nothing to Do";
        }
    }

    private List<Pair<String, RoleAssignmentParams>> getRoleAssignmentParams() throws IOException {
        if (allArgs.assignmentCommand != null) {
            return allArgs.assignmentCommand.getAssignment(datasetCmd);
        }
        else if (allArgs.list) {
            return datasetCmd.getItems().stream()
                .map(p -> new Pair<>(p.getFirst(), new RoleAssignmentParams(p.getSecond(), Optional.empty(), Optional.empty(), CommandAction.LIST))).toList();
        }

        return List.of();
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
