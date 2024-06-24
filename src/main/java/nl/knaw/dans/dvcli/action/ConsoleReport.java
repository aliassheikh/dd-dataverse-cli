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

/**
 * Implements a report to the console.
 *
 * @param <I> the type of the item that was processed
 * @param <R> the type of the result of the action
 */
public class ConsoleReport<I, R> implements Report<I, R> {

    @Override
    public void reportSuccess(String label, I i, R r) {
        System.err.println(label + ": OK. " + r);
    }

    @Override
    public void reportFailure(String label, I i, Exception e) {
        System.err.println(label + ": FAILED: Exception type = " + e.getClass().getSimpleName() + ", message = " + e.getMessage());
    }
}
