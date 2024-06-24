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
package nl.knaw.dans.dvcli.action;

import nl.knaw.dans.lib.dataverse.DatasetApi;
import nl.knaw.dans.lib.dataverse.DataverseClient;

import java.io.IOException;
import java.util.stream.Stream;

public class SingleDatasetOrDatasetsFile {
    private final SingleIdOrIdsFile singleIdOrIdsFile;
    private final DataverseClient dataverseClient;

    public SingleDatasetOrDatasetsFile(String singleDatasetOrDatasetsFile, DataverseClient dataverseClient) {
        this.singleIdOrIdsFile = new SingleIdOrIdsFile(singleDatasetOrDatasetsFile, "-"); 
        this.dataverseClient = dataverseClient;
    }

    public Stream<Pair<String, DatasetApi>> getDatasets() throws IOException {
        return singleIdOrIdsFile.getPids().map(
            id -> {
                try {
                    var dbId = Integer.parseInt(id);
                    return new Pair<>(id, dataverseClient.dataset(dbId));
                }
                catch (NumberFormatException e) {
                    // Assume it is a PID
                }
                return new Pair<>(id, dataverseClient.dataset(id));
            });
    }
}
