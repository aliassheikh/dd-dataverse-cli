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

import nl.knaw.dans.dvcli.command.AbstractCmd;
import nl.knaw.dans.lib.dataverse.DataverseException;
import nl.knaw.dans.lib.dataverse.model.dataset.UpdateType;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;

@Command(name = "publish",
         mixinStandardHelpOptions = true,
         description = "Dataset publication result.")
public class DatasetPublish extends AbstractCmd {
    @ParentCommand
    private DatasetCmd datasetCmd;

    @ArgGroup(exclusive = true)
    VersionUpdateType versionUpdateType;

    static class VersionUpdateType {
        @Option(names = "--major", description = "Version update type: major")
        boolean major;
        @Option(names = "--minor", description = "Version update type: minor (default)")
        boolean minor;
    }

    @Option(names = {"--skip-assure-indexed" }, paramLabel = "skip-assure-indexed", description = "Do not attempt to assure that the dataset is indexed.")
    private boolean skipAssureIsIndexed;

    private UpdateType getUpdateType() {
        if (versionUpdateType != null && versionUpdateType.major)
            return UpdateType.major;
        return UpdateType.minor;
    }

    @Override
    public void doCall() throws IOException, DataverseException {
        datasetCmd.batchProcessor(dataset -> dataset
            .publish(this.getUpdateType(), skipAssureIsIndexed)
            .getEnvelopeAsString()
        ).process();
    }

}
