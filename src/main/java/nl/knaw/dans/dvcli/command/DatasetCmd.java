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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.knaw.dans.lib.dataverse.DatasetApi;
import nl.knaw.dans.lib.dataverse.DataverseClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;

@Command(name = "dataset",
         mixinStandardHelpOptions = true,
         description = "Manage Dataverse datasets")
@Slf4j
public class DatasetCmd extends AbstractSubcommandContainer {
    @Parameters(index = "0", paramLabel = "id", description = "The id or PID of the dataset")
    private String id;

    public DatasetCmd(DataverseClient dataverseClient) {
        super(dataverseClient);
    }
    
    

    List<DatasetApi> getDatasets() {
        // If id is a number convert it to an integer
        try {
            var databaseId = Integer.parseInt(id);
            log.debug("ID {} is a number, assuming it is a database ID", id);
            return List.of(dataverseClient.dataset(databaseId));
        }
        catch (NumberFormatException e) {
            // Do nothing
        }
        log.debug("ID {} is not a number, assuming it is a PID", id);
        return List.of(dataverseClient.dataset(id));
    }

}
