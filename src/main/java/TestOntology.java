import java.io.FileOutputStream;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class TestOntology {

    public static void main(String[] args) throws Exception {
        OntModel model = ModelFactory.createOntologyModel();
        String OWLPath = "DataProp.owl";
        try{
            String NS = "http://www.example.org/ontology.owl#";
            //Create Ontology
            model.createClass(NS+"Test");
            Resource r = model.createResource(NS+"Test");
            model.createIndividual(NS+"Indi1", r);
            r = model.createResource(NS+"Indi1");
            model.createDatatypeProperty(NS+"Name");
            model.createDatatypeProperty(NS+"Date");
            //Add Data Properties
            Property p = model.getProperty(NS+"Name");
            model.add(r, p, ResourceFactory.createTypedLiteral("MyName", XSDDatatype.XSDstring));
            p = model.getProperty(NS+"Date");
            model.add(r, p, ResourceFactory.createTypedLiteral("2017-08-12T09:03:40", XSDDatatype.XSDdateTime));
            //Store the ontology
            FileOutputStream output = null;
            output = new FileOutputStream(OWLPath);
            model.write(System.out);

        }catch (Exception e) {
            System.out.println("Error occured: " + e);
            throw new Exception(e.getMessage());
        }
    }
}