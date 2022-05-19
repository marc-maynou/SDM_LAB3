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
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class OntologyCreator {

    static OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
    static String basePath = new File("").getAbsolutePath();
    static Random rand = new Random();
    static String URI = "http://www.gra.fo/schema/untitled-ekg#";

    private static String cleanStrings(String name) {
        name = name.replace(" ", "_");
        name = name.replace("&", "_");
        name = name.replace("(", "_");
        name = name.replace(")", "_");
        name = name.replace("/", "_");
        name = name.replace("'", "_");
        name = name.replace("\"", "_");
        return name;
    }


    private static void createConference(String confName) {
        confName = cleanStrings(confName);
        String[] confTypes = {"Workshop", "Symposium", "ExpertGroup", "RegularConference"};

        Resource confTypeRes = model.getResource(URI + confTypes[rand.nextInt(4)]);
        Individual confType = model.createIndividual(URI + confName , confTypeRes);
        confType.addRDFType(OWL2.NamedIndividual);
    }

    private static void createJournal(String journalName) {
        journalName = cleanStrings(journalName);
        Resource journalRes = model.getResource(URI + "Journal");
        Individual journal = model.createIndividual(URI + journalName , journalRes);
        journal.addRDFType(OWL2.NamedIndividual);
    }

    private static void addConferences() throws FileNotFoundException {
        Scanner sc = new Scanner(new File(basePath + "\\processed_data\\conferences.csv"));
        sc.useDelimiter(";");
        sc.nextLine();
        while (sc.hasNext()) {
            String confName = sc.nextLine().split(";")[0];
            confName = cleanStrings(confName);

//            Resource venueRes = model.getResource("http://www.gra.fo/schema/untitled-ekg#Venue");
//            Individual venue = model.createIndividual("http://www.gra.fo/schema/untitled-ekg#" + confName , venueRes);
//            venue.addRDFType(OWL2.NamedIndividual);
//
//            Resource conferenceRes = model.getResource("http://www.gra.fo/schema/untitled-ekg#Conference");
//            Individual conference = model.createIndividual("http://www.gra.fo/schema/untitled-ekg#" + confName , conferenceRes);
//            conference.addRDFType(OWL2.NamedIndividual);

            String[] confTypes = {"Workshop", "Symposium", "ExpertGroup", "RegularConference"};

            Resource confTypeRes = model.getResource(URI + confTypes[rand.nextInt(4)]);
            Individual confType = model.createIndividual(URI + confName , confTypeRes);
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
            journalName = cleanStrings(journalName);

//            Resource venueRes = model.getResource("http://www.gra.fo/schema/untitled-ekg#Venue");
//            Individual venue = model.createIndividual("http://www.gra.fo/schema/untitled-ekg#" + confName , venueRes);
//            venue.addRDFType(OWL2.NamedIndividual);

            Resource journalRes = model.getResource(URI + "Journal");
            Individual journal = model.createIndividual(URI + journalName , journalRes);
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
            conference = cleanStrings(conference);
            city = cleanStrings(city);


            Resource confProRes = model.getResource(URI + "ConferenceProceedings");
            Individual confProc = model.createIndividual(URI + conference + "_" + year + "_" + city, confProRes);
            confProc.addRDFType(OWL2.NamedIndividual);

            //Add proceedingsOf
            ObjectProperty p = model.getObjectProperty(URI + "proceedingsof");
            Individual conferenceInd = model.getIndividual( URI + conference);
            if (conferenceInd == null) {
                createConference(conference);
                conferenceInd = model.getIndividual( URI + conference);
            }
            model.add(confProc,p,conferenceInd);
        }
        sc.close();
    }

    private static void addJournalVolumes() throws FileNotFoundException {
        Scanner sc = new Scanner(new File(basePath + "\\processed_data\\volumes.csv"));
        sc.useDelimiter(";");
        sc.nextLine();
        while (sc.hasNext()) {
            String[] values = sc.nextLine().split(";");
            String journal = values[0];
            String volume = values[1];
            journal = cleanStrings(journal);
            volume = cleanStrings(volume);

            Resource volumeRes = model.getResource(URI + "JournalVolume");
            Individual volumeInd = model.createIndividual(URI + journal + "_" + volume, volumeRes);
            volumeInd.addRDFType(OWL2.NamedIndividual);

            //Add volumeof
            ObjectProperty p = model.getObjectProperty(URI + "volumeof");
            Individual journalInd = model.getIndividual( URI + journal);
            if (journalInd == null) {
                createJournal(journal);
                journalInd = model.getIndividual( URI + journal);
            }
            model.add(volumeInd,p,journalInd);
        }
        sc.close();
    }

    private static void addPapers() throws FileNotFoundException {
        Scanner sc = new Scanner(new File(basePath + "\\processed_data\\journal_papers.csv"));
        sc.useDelimiter(";");
        sc.nextLine();
        while (sc.hasNext()) {
            String[] values = sc.nextLine().split(";");
            String key = cleanStrings(values[0]);
            String title = cleanStrings(values[1]);
            String journal = cleanStrings(values[3]);
            String volume = cleanStrings(values[4]);
            String reviewers = cleanStrings(values[7]);

            String[] paperTypes = {"ShortPaper", "FullPaper", "DemoPaper"};

            Resource paperRes = model.getResource(URI + paperTypes[rand.nextInt(3)]);
            Individual paperInd = model.createIndividual(URI + key, paperRes);
            paperInd.addRDFType(OWL2.NamedIndividual);

            // Add author and writes relationship
            Scanner sc2 = new Scanner(new File(basePath + "\\processed_data\\lead_authors_journal.csv"));
            sc2.useDelimiter(";");
            sc2.nextLine();
            while (sc2.hasNext()) {
                String[] authorValues = sc2.nextLine().split(";");
                String keyAuthor = cleanStrings(authorValues[0]);
                String authorName = cleanStrings(authorValues[1]);
                System.out.println(key + " " + keyAuthor);
                if (keyAuthor.equals(key)) {
                    Resource authorRes = model.getResource(URI + "Author");
                    Individual author = model.createIndividual(URI + authorName, authorRes);
                    author.addRDFType(OWL2.NamedIndividual);

                    ObjectProperty writes = model.getObjectProperty(URI + "writes");
                    model.add(author,writes,paperInd);
                }

            }
        }
        sc.close();

    }

    public static void main(String[] args) throws Exception {
        model.read(basePath + "\\SDM_LAB_3.owl");

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
        addJournals();
        addJournalVolumes();
        addPapers();

        FileWriter myWriter = new FileWriter(basePath + "\\output.rdf");
        model.write(myWriter, "RDF/XML-ABBREV");
        myWriter.close();
    }
}
