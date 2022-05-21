import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.function.library.print;
import org.apache.jena.tdb.sys.SystemTDB;
import org.apache.jena.rdf.model.*;
import org.apache.jena.*;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDFS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Arrays;
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

    private static void addConferences(String[] chairs) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(basePath + "\\processed_data\\conferences.csv"));
        sc.useDelimiter(";");
        sc.nextLine();
        
        while (sc.hasNext()) {
        	
            String confName = sc.nextLine().split(";")[0];
            confName = cleanStrings(confName);

            String[] confTypes = {"Workshop", "Symposium", "ExpertGroup", "RegularConference"};           
            Individual confType =  GetOrCreateIndividual(confTypes[rand.nextInt(4)], confName);
            
            Individual chair =  GetOrCreateIndividual("Chair", chairs[rand.nextInt(5)]);
            
            ObjectProperty handledBy = model.getObjectProperty(URI + "handledby");
            model.add(confType,handledBy,chair);
        }
        
        sc.close();
    }

    private static void addJournals(String[] editors) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(basePath + "\\processed_data\\journals.csv"));
        sc.useDelimiter(";");
        sc.nextLine();
        
        while (sc.hasNext()) {
            String journalName = sc.nextLine().split(";")[0];
            journalName = cleanStrings(journalName);
           
            Individual journal = GetOrCreateIndividual("Journal", journalName);
            
            Individual editor =  GetOrCreateIndividual("Chair", editors[rand.nextInt(5)]);
            
            ObjectProperty handledBy = model.getObjectProperty(URI + "handledby");
            model.add(journal,handledBy,editor);
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
            Individual confProc = model.createIndividual(URI + conference + "_" + year, confProRes);
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
    
    private static void addPapers(String venue) throws FileNotFoundException {
        
    	String fileName = "journal_papers";
    	if (venue != "Journal") {
    		fileName = "conference_papers";
		}
    	
    	Scanner sc = new Scanner(new File(basePath + "\\processed_data\\" + fileName + ".csv"));    	
        sc.useDelimiter(";");
        sc.nextLine();
        
        while (sc.hasNext()) {
            String[] values = sc.nextLine().split(";");
            String key = cleanStrings(values[0]);
            
            String venueName = cleanStrings(values[4]);
            
            String number = cleanStrings(values[5]); 
            
            String reviewers = "";
            
            if (venue != "Journal") {
            	reviewers = cleanStrings(values[7]);
    		}
            else
            {
            	reviewers = cleanStrings(values[8]);
            }

            List<String> paperTypes = Arrays.asList("ShortPaper", "FullPaper", "DemoPaper");
            
            if (venue != "Journal") {
        		paperTypes.add("Poster");
    		}
            
            Individual paperInd = GetOrCreateIndividual(paperTypes.get(rand.nextInt(3)), key);
                        
            // Submission Node
            Individual submissionInd = GetOrCreateIndividual("AcceptedSubmission", "submission_" + key + "_accepted");
            submissionInd.addRDFType(OWL2.NamedIndividual);
            
            // Relation Submission and Paper 
            ObjectProperty includes = model.getObjectProperty(URI + "includes");
            model.add(submissionInd,includes,paperInd);
            
            // Reviewers Node
            if (!reviewers.isEmpty()) {			
	            String[] reviewersValues = reviewers.split("\\|");
	            
	            String  reviewer1 = reviewersValues[0];
	            String  reviewer2 = reviewersValues[1];
	            String  reviewer3 = reviewersValues[2];	            	           
	            
	            Individual reviewersInd1 = GetOrCreateIndividual("Reviewers", reviewer1);
	            Individual reviewersInd2 = GetOrCreateIndividual("Reviewers", reviewer2);
	            Individual reviewersInd3 = GetOrCreateIndividual("Reviewers", reviewer3);
	            
	            Individual reviewInd1 = GetOrCreateIndividual("Review", "review1_" + reviewer1 + "_" + key  );
	            Individual reviewInd2 = GetOrCreateIndividual("Review", "review2_" + reviewer2 + "_" + key  );
	            Individual reviewInd3 = GetOrCreateIndividual("Review", "review3_" + reviewer3 + "_" + key  );
	            
	            ObjectProperty writesReview = model.getObjectProperty(URI + "writesreview");
                
	            model.add(reviewersInd1,writesReview,reviewInd1);
	            model.add(reviewersInd2,writesReview,reviewInd2);
	            model.add(reviewersInd3,writesReview,reviewInd3);
	            
	            ObjectProperty reviewedBy = model.getObjectProperty(URI + "reviewedby");

	            model.add(submissionInd,reviewedBy,reviewInd1);
	            model.add(submissionInd,reviewedBy,reviewInd2);	  
	            model.add(submissionInd,reviewedBy,reviewInd3);
                	            
	            Individual decisionInd1 = GetOrCreateIndividual("Decision", "decision1_" + reviewer1 + "_" + key);
	            Individual decisionInd2 = GetOrCreateIndividual("Decision", "decision2_" + reviewer2 + "_" + key);
	            Individual decisionInd3 = GetOrCreateIndividual("Decision", "decision3_" + reviewer3 + "_" + key);
	            
	            ObjectProperty decides = model.getObjectProperty(URI + "decides");
	            
	            model.add(reviewInd1,decides,decisionInd1);
	            model.add(reviewInd2,decides,decisionInd2);	  
	            model.add(reviewInd3,decides,decisionInd3);
	            
	            ObjectProperty sendTo = model.getObjectProperty(URI + "sendto");
	            
	            Individual venueInd = null;
	            
	            if (venue != "Journal") {
	            	venueInd = GetOrCreateIndividualConference(venueName);
	    		}
	            else {
	            	venueInd = GetOrCreateIndividual(venue, venueName);
				}
	            
	            model.add(submissionInd,sendTo,venueInd);	            

	            Individual numberInd = null;
	            		
	            if (venue != "Journal") {
	            	numberInd = GetOrCreateIndividual("ConferenceProceedings", venueName + "_" + number);
	    		}
	            else {
	            	numberInd = GetOrCreateIndividual("JournalVolume", venueName + "_" + number);
				}
	            	            
	            ObjectProperty publishedIn = model.getObjectProperty(URI + "publishedin");
	            
	            model.add(venueInd,publishedIn,numberInd);	         	            
            }
            
            
            // Add author and writes relationship
            String fileNameAuthors = "lead_authors_journal";
            
            if (venue != "Journal") {
            	fileNameAuthors = "lead_authors_conference";
    		}
            
            Scanner sc2 = new Scanner(new File(basePath + "\\processed_data\\" + fileNameAuthors + ".csv"));
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
    
    public static Individual GetOrCreateIndividual(String resourceName, String individualName)
    {
    	Resource resource = model.getResource(URI + resourceName);        
        Individual individual = model.getIndividual( URI + resource);        
        
        if (individual == null) {
        	individual = model.createIndividual(URI + individualName, resource);
        	individual.addRDFType(OWL2.NamedIndividual);            
        }
        
        return individual;
    }
    
    public static Individual GetOrCreateIndividualConference(String individualName)
    {
        Individual individual = model.getIndividual( URI + individualName);        
        
        if (individual == null) {
        	String[] confTypes = {"Workshop", "Symposium", "ExpertGroup", "RegularConference"};       
            Resource confTypeRes = model.getResource(URI + confTypes[rand.nextInt(4)]);
            individual = model.createIndividual(URI + individualName , confTypeRes);
            individual.addRDFType(OWL2.NamedIndividual);
        }
        
        return individual;
    }

    public static void main(String[] args) throws Exception {
        model.read(basePath + "\\SDM_LAB_3.owl");

        String[] chairs = new String[5];
        String[] editors  = new String[5];
        
        addAuthors(chairs, editors);
        addConferences(chairs);
        addConferenceProcedings();
        addJournals(editors);
        addJournalVolumes();
        addPapers("Journal");
        addPapers("Conference");

        FileWriter myWriter = new FileWriter(basePath + "\\output.rdf");
        model.write(myWriter, "RDF/XML-ABBREV");
        myWriter.close();
    }


	private static void addAuthors(String[] chairs, String[] editors) throws FileNotFoundException {
		
		// Add author and writes relationship
        String[] fileNameAuthors = {"lead_authors_journal", "lead_authors_conference"};              
        Random r = new Random();
        int low = 10;
        int high = 100;
        
        int j = 0;
        int k = 0;
        for (int i = 0; i < fileNameAuthors.length; i++) {		
	        Scanner sc2 = new Scanner(new File(basePath + "\\processed_data\\" + fileNameAuthors[i] + ".csv"));
	        sc2.useDelimiter(";");
	        sc2.nextLine();
	        
	        while (sc2.hasNext()) {
	            
	        	String[] authorValues = sc2.nextLine().split(";");
	        	
	        	//System.out.println(k);
	        	
	        	if (authorValues.length > 1)					
	        	{
		            String authorName = cleanStrings(authorValues[1]);            
		            
		            Resource authorRes = model.getResource(URI + "Author");
		            Individual author = model.createIndividual(URI + authorName, authorRes);
		            author.addRDFType(OWL2.NamedIndividual);
		            
		            Boolean isAuthority = r.nextInt(high-low) + low > 80;
		            
		            if(i == 1 && j <= 4 && isAuthority)
		            {
		            	chairs[j] = authorName;
		            	j++;
		            }
		            
		            if(i == 0 && j <= 4 && isAuthority)
		            {
		            	editors[j] = authorName;
		            	j++;
		            }
		        }
	            
	            k++;
	        }
	        
	        j=0;
        }
	}
}
