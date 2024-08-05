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

import nl.knaw.dans.dvcli.action.BatchProcessor;
import nl.knaw.dans.dvcli.action.ConsoleReport;
import nl.knaw.dans.lib.dataverse.DatasetApi;
import nl.knaw.dans.lib.dataverse.DataverseException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;
import java.util.EnumSet;

@Command(name = "publish",
         mixinStandardHelpOptions = true,
         description = "Dataset publication result.")
public class DatasetPublish extends AbstractCmd {
    @ParentCommand
    private DatasetCmd datasetCmd;

    @Option(names={"-u", "--update-type"}, type =UpdateType.class, description ="'major' or 'minor' version update.")
    private EnumSet<UpdateType> updateType = EnumSet.of(UpdateType.major);

    @Option(names={"-a", "--assure-indexed"}, paramLabel = "assure-indexed", type = Boolean.class, description = "To make sure that indexing has already happened and it is set to 'true'.")
    private boolean assureIsIndexed = true;


    @Override
    public void doCall() throws IOException, DataverseException {
        BatchProcessor.<DatasetApi, String> builder()
            .labeledItems(datasetCmd.getItems())
            .action(d -> {
                var r = d.publish();
                return r.getEnvelopeAsString();
            })
            .report(new ConsoleReport<>())
            .delay(1000L)
            .build()
            .process();
    }

    enum UpdateType {major, minor}

}
