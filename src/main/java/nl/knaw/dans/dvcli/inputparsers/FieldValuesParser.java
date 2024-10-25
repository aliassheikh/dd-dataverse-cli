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
package nl.knaw.dans.dvcli.inputparsers;

import lombok.AllArgsConstructor;
import nl.knaw.dans.lib.dataverse.CompoundFieldBuilder;
import nl.knaw.dans.lib.dataverse.model.dataset.MetadataField;
import nl.knaw.dans.lib.dataverse.model.dataset.PrimitiveMultiValueField;
import nl.knaw.dans.lib.dataverse.model.dataset.PrimitiveSingleValueField;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
public class FieldValuesParser {
    private final Map<String, String> keyValues;

    public Set<MetadataField> parse() {
        for (var key : keyValues.keySet()) {
            checkValidName(key);
        }

        Map<String, Map<String, String>> compoundFields = new HashMap<>();
        List<String> keysToRemove = new ArrayList<>();
        for (var key : keyValues.keySet()) {
            if (key.contains(".")) {
                String[] split = key.split("\\.", 2);
                String parent = split[0];
                String child = split[1];
                if (!compoundFields.containsKey(parent)) {
                    compoundFields.put(parent, new HashMap<>());
                }
                compoundFields.get(parent).put(child, keyValues.get(key));
                keysToRemove.add(key);
            }
        }
        for (var key : keysToRemove) {
            keyValues.remove(key);
        }

        Set<MetadataField> result = new HashSet<>();

        for (var key : keyValues.keySet()) {
            if (StringUtils.isNotBlank(keyValues.get(key))) {
                if (key.endsWith("*")) {
                    result.add(new PrimitiveMultiValueField(key.substring(0, key.length() - 1), List.of(keyValues.get(key))));
                }
                else {
                    result.add(new PrimitiveSingleValueField(key, keyValues.get(key)));
                }
            }
        }

        for (var parent : compoundFields.keySet()) {
            Map<String, String> subfields = compoundFields.get(parent);
            if (parent.endsWith("*")) {
                var builder = new CompoundFieldBuilder(parent.substring(0, parent.length() - 1), true);
                boolean hasValues = false;
                for (var subfield : subfields.keySet()) {
                    if (StringUtils.isNotBlank(subfields.get(subfield))) {
                        builder.addSubfield(subfield, subfields.get(subfield));
                        hasValues = true;
                    }
                }
                if (hasValues) {
                    result.add(builder.build());
                }
            }
            else {
                var builder = new CompoundFieldBuilder(parent, false);
                boolean hasValues = false;
                for (var subfield : subfields.keySet()) {
                    if (StringUtils.isNotBlank(subfields.get(subfield))) {
                        builder.addSubfield(subfield, subfields.get(subfield));
                        hasValues = true;
                    }
                }
                if (hasValues) {
                    result.add(builder.build());
                }
            }
        }

        return result;
    }

    private void checkValidName(String name) {
        if (!name.matches("[a-zA-Z0-9]+\\*?(\\.[a-zA-Z0-9]+)?")) {
            throw new IllegalArgumentException("Invalid field name: " + name);
        }
    }
}
