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

import lombok.NonNull;
import nl.knaw.dans.dvcli.action.BatchProcessor;
import nl.knaw.dans.dvcli.action.Pair;
import nl.knaw.dans.dvcli.action.SingleCollectionOrCollectionsFile;
import nl.knaw.dans.lib.dataverse.DataverseApi;
import nl.knaw.dans.lib.dataverse.DataverseClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Command(name = "collection",
         mixinStandardHelpOptions = true,
         description = "Manage Dataverse collections (i.e. 'dataverses')")
public class CollectionCmd extends AbstractSubcommandContainer {
    private static final String DEFAULT_ALIAS = "root";
    private static final long DEFAULT_DELAY = 1000;
    
    @Parameters(index = "0", paramLabel = "alias", description = "The alias of the dataverse collection (default: root), or the name of file containing a list of aliases.",
                defaultValue = DEFAULT_ALIAS)
    private String alias;
    
    @Option(names = {"-d", "--delay"}, description = "Delay in milliseconds between requests to the server (default: ${DEFAULT-VALUE}).", defaultValue = "" + DEFAULT_DELAY)
    private long delay;
    
    List<Pair<String, DataverseApi>> getCollections() throws IOException {
        return new SingleCollectionOrCollectionsFile(alias, dataverseClient).getCollections().collect(Collectors.toList());
    }

    BatchProcessor.BatchProcessorBuilder<DataverseApi, String> batchProcessorBuilder() throws IOException {
        return BatchProcessor.<DataverseApi, String> builder()
            .labeledItems(getCollections())
            .delay(delay);
        
    }    
    
    public CollectionCmd(@NonNull DataverseClient dataverseClient) {
        super(dataverseClient);
    }
}
