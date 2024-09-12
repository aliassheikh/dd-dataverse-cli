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
import nl.knaw.dans.dvcli.action.ThrowingFunction;
import nl.knaw.dans.lib.dataverse.DataverseApi;
import nl.knaw.dans.lib.dataverse.DataverseException;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;

@Command(name = "role-assignment",
         mixinStandardHelpOptions = true,
         description = "Manage role assignments.",
         subcommands = { CollectionRoleAssignment.CollectionListRoleAssignments.class, CollectionRoleAssignment.CollectionAssignRole.class })
public class CollectionRoleAssignment extends AbstractCmd {
    @ParentCommand
    static private CollectionCmd collectionCmd;

    @Command(name = "list", mixinStandardHelpOptions = true, description = "List the role assignments of a Dataverse collection.")
    static class CollectionListRoleAssignments extends AbstractCmd {

        @Override
        public void doCall() throws IOException, DataverseException {
            collectionCmd.batchProcessor(d -> d.listRoleAssignments().getEnvelopeAsString()).process();
        }
    }

    @Command(name = "add",
             mixinStandardHelpOptions = true,
             description = "Assign a role to a user in a Dataverse collection.")
    static class CollectionAssignRole extends AbstractAssignmentRole<CollectionCmd, DataverseApi> {
        @Override
        protected DataverseApi getItem(String pid) {
            return collectionCmd.dataverseClient.dataverse(pid);
        }

        private static class RoleAssignmentAction implements ThrowingFunction<RoleAssignmentParams<DataverseApi>, String, Exception> {
            @Override
            public String apply(RoleAssignmentParams<DataverseApi> roleAssignmentParams) throws IOException, DataverseException {
                if (roleAssignmentParams.roleAssignment().isPresent()) {
                    var r = roleAssignmentParams.pid().assignRole(roleAssignmentParams.roleAssignment().get());
                    return r.getEnvelopeAsString();
                }
                return "There was no assignment-role to assign.";
            }
        }

        @Override
        public void doCall() throws IOException, DataverseException {
            collectionCmd.<RoleAssignmentParams<DataverseApi>> paramsBatchProcessorBuilder()
                .labeledItems(getRoleAssignmentParams(collectionCmd))
                .action(new RoleAssignmentAction())
                .report(new ConsoleReport<>())
                .build()
                .process();
        }
    }

    @Override
    public void doCall() throws IOException, DataverseException {

    }
}
