import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.*;
import org.apache.jena.*;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDFS;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class OntologyCreator {

    static OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
    static String basePath = new File("").getAbsolutePath();
    static Random rand = new Random();


    private static void createConference(String confName) {
        String[] confTypes = {"Workshop", "Symposium", "ExpertGroup", "RegularConference"};

        Resource confTypeRes = model.getResource("http://www.gra.fo/schema/untitled-ekg#" + confTypes[rand.nextInt(4)]);
        Individual confType = model.createIndividual("http://www.gra.fo/schema/untitled-ekg#" + confName , confTypeRes);
        confType.addRDFType(OWL2.NamedIndividual);
    }

    private static void addConferences() throws FileNotFoundException {
        Scanner sc = new Scanner(new File(basePath + "\\processed_data\\conferences.csv"));
        sc.useDelimiter(";");
        sc.nextLine();
        while (sc.hasNext()) {
            String confName = sc.nextLine().split(";")[0];
            confName = confName.replace(" ", "_");

//            Resource venueRes = model.getResource("http://www.gra.fo/schema/untitled-ekg#Venue");
//            Individual venue = model.createIndividual("http://www.gra.fo/schema/untitled-ekg#" + confName , venueRes);
//            venue.addRDFType(OWL2.NamedIndividual);
//
//            Resource conferenceRes = model.getResource("http://www.gra.fo/schema/untitled-ekg#Conference");
//            Individual conference = model.createIndividual("http://www.gra.fo/schema/untitled-ekg#" + confName , conferenceRes);
//            conference.addRDFType(OWL2.NamedIndividual);

            String[] confTypes = {"Workshop", "Symposium", "ExpertGroup", "RegularConference"};

            Resource confTypeRes = model.getResource("http://www.gra.fo/schema/untitled-ekg#" + confTypes[rand.nextInt(4)]);
            Individual confType = model.createIndividual("http://www.gra.fo/schema/untitled-ekg#" + confName , confTypeRes);
            confType.addRDFType(OWL2.NamedIndividual);
        }
        sc.close();
    }

    private static void addJournals() throws FileNotFoundException {
        Scanner sc = new Scanner(new File(basePath + "\\processed_data\\journals.csv"));
        sc.useDelimiter(";");
        sc.nextLine();
        while (sc.hasNext()) {
            String journalName = sc.nextLine().split(";")[0];
            journalName = journalName.replace(" ", "_");

//            Resource venueRes = model.getResource("http://www.gra.fo/schema/untitled-ekg#Venue");
//            Individual venue = model.createIndividual("http://www.gra.fo/schema/untitled-ekg#" + confName , venueRes);
//            venue.addRDFType(OWL2.NamedIndividual);

            Resource journalRes = model.getResource("http://www.gra.fo/schema/untitled-ekg#Journal");
            Individual journal = model.createIndividual("http://www.gra.fo/schema/untitled-ekg#" + journalName , journalRes);
            journal.addRDFType(OWL2.NamedIndividual);
        }
        sc.close();
    }

    private static void addConferenceProcedings() throws FileNotFoundException {
        Scanner sc = new Scanner(new File(basePath + "\\processed_data\\conference_editions.csv"));
        sc.useDelimiter(";");
        sc.nextLine();
        while (sc.hasNext()) {
            String[] values = sc.nextLine().split(";");
            String conference = values[0];
            String year = values[1];
            String city = values[2];
            conference = conference.replace(" ", "_");


            Resource confProRes = model.getResource("http://www.gra.fo/schema/untitled-ekg#ConferenceProceedings");
            Individual confProc = model.createIndividual("http://www.gra.fo/schema/untitled-ekg#" + conference + "_" + year + "_" + city, confProRes);
            ObjectProperty p = model.createObjectProperty("http://www.gra.fo/schema/untitled-ekg#" + "Conference");
            Individual i = model.getIndividual( "http://www.gra.fo/schema/untitled-ekg#" + conference);
            if (i == null) createConference(conference);
            confProc.addProperty(p,  model.getIndividual( "http://www.gra.fo/schema/untitled-ekg#" + conference));
            System.out.println(confProc);
            confProc.addRDFType(OWL2.NamedIndividual);
        }
        sc.close();
    }

    public static void main(String[] args) throws Exception {
        model.read(basePath + "\\SDM_LAB_3.owl");

        // Example of 'ns': String NS = "http://www.example.org/ontology.owl#";

        Resource areaResource = model.getResource("http://www.gra.fo/schema/untitled-ekg#Area");
        model.createDatatypeProperty("http://www.gra.fo/schema/untitled-ekg#" + "Subarea");

//        Scanner sc = new Scanner(new File("C:\\Users\\marcm\\OneDrive\\Desktop\\processed_data\\topics.csv"));
//        sc.useDelimiter(",");
//        sc.nextLine();
//        while (sc.hasNext()) {
//            String area = sc.nextLine();
//            area = area.replace(" ", "_");
//            Individual IndividualArea = model.createIndividual("http://www.gra.fo/schema/untitled-ekg#" + area , areaResource);
//            IndividualArea.addRDFType(OWL2.NamedIndividual);
//            model.createDatatypeProperty("http://www.gra.fo/schema/untitled-ekg#" + "JournalType");
//            Property p = model.getDatatypeProperty("http://www.gra.fo/schema/untitled-ekg#" + "JournalType");
//            confType.addProperty(p,  model.createLiteral( "CS" ));
//        }
//        sc.close();

        addConferences();
        addConferenceProcedings();
        //addJournals();

        model.write(System.out);
    }
}
