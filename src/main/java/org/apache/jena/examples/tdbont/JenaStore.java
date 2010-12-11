/******************************************************************************
 ** This data and information is proprietary to, and a valuable trade secret
 ** of, Basis Technology Corp.  It is given in confidence by Basis Technology
 ** and may only be used as permitted under the license agreement under which
 ** it has been distributed, and in no other way.
 **
 ** Copyright (c) 2010 Basis Technology Corporation All rights reserved.
 **
 ** The technical data and information provided herein are provided with
 ** `limited rights', and the computer software provided herein is provided
 ** with `restricted rights' as those terms are defined in DAR and ASPR
 ** 7-104.9(a).
 ******************************************************************************/

package org.apache.jena.examples.tdbont;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;
import com.hp.hpl.jena.tdb.store.GraphNamedTDB;

/**
 * This class manages a TDB store, accepting new graphs and managing queries.
 */
public class JenaStore {
    private static final String ONTOLOGY_URI = "urn:rex";
    private static final String ONTOLOGY_CLASS_PATH = "/rex.owl";
    private File tdbStoreLocation;
    private Dataset dataset;
    private Model schemaModel;
    private DatasetGraphTDB dsGraph;

    public void addGraph(String graphUri, InputStream rdfStream) {
        Model baseModel = dataset.getNamedModel(graphUri);
        baseModel.removeAll();
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, baseModel);
        m.read(rdfStream, graphUri, "RDF/XML-ABBREV");
        m.commit();
    }

    public void initialize() {
        TDB.getContext().set(TDB.symUnionDefaultGraph, "true");
        dsGraph = TDBFactory.createDatasetGraph(tdbStoreLocation.getAbsolutePath());
        dataset = new DatasetImpl(dsGraph);
        schemaModel = loadOntology();
    }

    private Model loadOntology() {
        addGraph(ONTOLOGY_URI, JenaStore.class.getResourceAsStream(ONTOLOGY_CLASS_PATH));
        GraphNamedTDB graph = new GraphNamedTDB(dsGraph, Quad.unionGraph);
        Model dsModel = ModelFactory.createModelForGraph(graph);
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, dsModel); 
        return m;
    }
    
    public Dataset getDataset() {
        return dataset;
    }

    public List<String> resourcesThatMatchQuery(String queryString, String queryVar) {
        List<String> results = new ArrayList<String>();
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, schemaModel);
        try {
            ResultSet queryResults = qexec.execSelect();
            while (queryResults.hasNext()) {
                QuerySolution soln = queryResults.nextSolution();
                Resource r = soln.getResource(queryVar); // Get a result variable by name.
                results.add(r.getURI());
            }

        } finally {
            qexec.close();
        }
        return results;
    }

    public File getTdbStoreLocation() {
        return tdbStoreLocation;
    }

    public void setTdbStoreLocation(File tdbStoreLocation) {
        this.tdbStoreLocation = tdbStoreLocation;
    }

    public void shutdown() {
        if (dataset != null) {
            dataset.close();
        }
    }

}
