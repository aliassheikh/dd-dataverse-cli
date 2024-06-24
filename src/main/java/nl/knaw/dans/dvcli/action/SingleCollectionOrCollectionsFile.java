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

import nl.knaw.dans.lib.dataverse.DataverseApi;
import nl.knaw.dans.lib.dataverse.DataverseClient;

import java.io.IOException;
import java.util.stream.Stream;

public class SingleCollectionOrCollectionsFile {
    private final SingleIdOrIdsFile singleIdOrIdsFile;
    private final DataverseClient dataverseClient;

    public SingleCollectionOrCollectionsFile(String singleCollectionOrCollectionsFile, DataverseClient dataverseClient) {
        this.singleIdOrIdsFile = new SingleIdOrIdsFile(singleCollectionOrCollectionsFile, "root");
        this.dataverseClient = dataverseClient;
    }

    public Stream<Pair<String, DataverseApi>> getCollections() throws IOException {
        return singleIdOrIdsFile.getPids().map(alias -> new Pair<>(alias, dataverseClient.dataverse(alias)));
    }
}
