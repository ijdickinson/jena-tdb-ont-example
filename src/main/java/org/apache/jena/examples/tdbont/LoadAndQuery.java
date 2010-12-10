/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.jena.examples.tdbont;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * 
 */
public final class LoadAndQuery {
    private LoadAndQuery() {
        //
    }

    public static void main(String[] args) {
        JenaStore store = new JenaStore();
        File storeFile = new File("target/store");
        try {
            FileUtils.deleteDirectory(storeFile);
        } catch (IOException e) {
            System.err.println("Failed to delete existing store " + storeFile.getAbsolutePath());
            return;
        }
        store.setTdbStoreLocation(storeFile);
        store.initialize();
        InputStream someRdf = LoadAndQuery.class.getResourceAsStream("/misc.rdf");
        store.addGraph("urn:test-graph", someRdf);
        IOUtils.closeQuietly(someRdf);
        String query = MessageFormat.format("SELECT '?x "
                                            + " from <urn:x-arq:UnionGraph>"
                                            + " where "
                                            + "{ ?x <'{0}'>' \"{1}\"' }'",
                                            Rex.hasOriginalText,
                                            "Fredrick Chopin");
        System.out.println(query);
        List<String> urns = store.resourcesThatMatchQuery(query, "x");
        for (String urn : urns) {
            System.out.println(urn);
        }
        store.shutdown();
    }

}
