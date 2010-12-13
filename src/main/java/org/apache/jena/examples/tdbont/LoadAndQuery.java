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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;

/**
 *
 */
public final class LoadAndQuery {
    public static final String STORE = "target/tdb";

    public static final String UNION_GRAPH = "urn:x-arq:UnionGraph";

    public static void main(String[] args) {
        // the base dataset
        TDB.getContext().set( TDB.symUnionDefaultGraph, true );
        Dataset dataset = TDBFactory.createDataset( STORE ) ;

        // now create a reasoning model using this base
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF, dataset.getNamedModel( UNION_GRAPH ) );

        String query = MessageFormat.format( "SELECT ?x  where '{' ?x <{0}> \"{1}\" '}'",
                                             Rex.hasOriginalText,
                                             "Fredrick Chopin");

        // Report results
        System.out.println(query);
        System.out.println("----");

        List<Resource> results = resourcesThatMatchQuery( query, "x", m );
        for (Resource r: results) {
            System.out.println( r );
        }

        System.out.println("----");
    }

    /**
     * Execute the given query against the given model, and return a list of the resources
     * which are the values of the bound variable <code>queryVar</code>
     *
     * @param queryString SPARQL query string
     * @param queryVar Result variable to extract
     * @param m Model to query
     * @return List of resources matching the query. Non-null, but may be empty
     */
    public static List<Resource> resourcesThatMatchQuery( String queryString, String queryVar, Model m ) {
        List<Resource> results = new ArrayList<Resource>();

        QueryExecution qexec = QueryExecutionFactory.create( queryString, m );
        try {
            ResultSet queryResults = qexec.execSelect();
            while (queryResults.hasNext()) {
                QuerySolution soln = queryResults.nextSolution();
                results.add( soln.getResource(queryVar) );
            }
        }
        finally {
            qexec.close();
        }
        return results;
    }


}
