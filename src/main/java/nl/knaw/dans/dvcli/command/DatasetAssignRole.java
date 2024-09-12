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

import nl.knaw.dans.dvcli.action.Pair;
import nl.knaw.dans.dvcli.action.ThrowingFunction;
import nl.knaw.dans.lib.dataverse.DatasetApi;
import nl.knaw.dans.lib.dataverse.DataverseException;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Command(name = "assign-role",
         mixinStandardHelpOptions = true,
         description = "Manage role assignments on one or more datasets.")

public class DatasetAssignRole extends AbstractAssignmentRole<DatasetCmd, DatasetApi> {
    @ParentCommand
    DatasetCmd datasetCmd;

    @Override
    protected DatasetApi getItem(String pid) {
        return datasetCmd.dataverseClient.dataset(pid);
    }

    private static class RoleAssignmentAction implements ThrowingFunction<RoleAssignmentParams<DatasetApi>, String, Exception> {
        @Override
        public String apply(RoleAssignmentParams<DatasetApi> roleAssignmentParams) throws IOException, DataverseException {
            if (roleAssignmentParams.roleAssignment().isPresent()) {
                var api = roleAssignmentParams.pid();
                var assignment = roleAssignmentParams.roleAssignment().get();
                return api.assignRole(assignment).getEnvelopeAsString();
            }
            return "There was no assignment-role to assign.";
        }
    }

    @Override
    public void doCall() throws IOException, DataverseException {
        datasetCmd.<RoleAssignmentParams<DatasetApi>> paramsBatchProcessorBuilder()
            .labeledItems(getRoleAssignmentParams(datasetCmd))
            .action(new RoleAssignmentAction())
            .build()
            .process();
    }
}
