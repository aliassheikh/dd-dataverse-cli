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

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

@AllArgsConstructor
public class SingleIdOrIdsFile {
    private final String singleIdOrIdFile;

    public Stream<String> getPids() throws IOException {
        var pidFile = Paths.get(singleIdOrIdFile);
        if (Files.exists(pidFile)) {
            return Files.readAllLines(pidFile).stream().map(String::trim);
        }
        return Stream.of(singleIdOrIdFile);
    }
}
