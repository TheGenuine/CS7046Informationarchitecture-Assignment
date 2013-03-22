package ie.tcd.scss.ubicom.informationarchitecture;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class Main {

	private static final String ONTOLOGY_FILE = "onto.owl";
	private static boolean running = true;
	private static OntModel ontoModel;
	
	private static String PREFIXES = "" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
			"PREFIX tcd: <http://www.scss.tcd.ie/pg/ubicom/ruckr#>\n";
	
	private static String queries[] = new String[]{
		PREFIXES + "SELECT ?name?id " +
				"WHERE {" +
				"?person tcd:isWearing ?tag." +
				"?person tcd:hasName ?name." +
				"?tag tcd:hasId ?id" +
				"}",
				
		PREFIXES + "SELECT ?sensor?sId " +
				"WHERE {" +
				"?zone tcd:hasId ?id." +
				"?zone tcd:hasSensors ?sensor." +
				"?sensor tcd:hasId ?sId." +
				"FILTER (?id = 32)}",
				
		PREFIXES + "SELECT ?value " +
				"WHERE {" +
				"?zone tcd:hasId ?id." +
				"?reading a tcd:Sensor_Reading." +
				"?sensorType a tcd:Temperature_Sensor." +
				"?zone tcd:hasSensors ?sensor." +
				"?reading tcd:hasSensorType ?sensorType." +
				"?reading tcd:hasValue ?value." +
				"FILTER(?sensorType = ?sensor)" +
				"FILTER (?id = 32)}" +
				"ORDER BY ASC(?reading)"
	};
	
	public static void main(String args[]) throws IOException {


		String input = "";
		byte[] buffer = new byte[100];
		while (running) {
			printMenu();
			try {
				System.in.read(buffer);
				handleInput(cleanInput(new String(buffer)));
				clearBuffer(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Shutting down...");
		System.out.println("Goodby");
	}

	private static void printMenu() {
		System.out.println("Menu:");
		System.out.println("=============");
		System.out.println("1. Load Ontology file");
		System.out.println("2. Names of all Staff members and their RFID Tag number");
		System.out.println("3. Sensor and Id of sensor for a given room");
		System.out.println("4. Average Temperature in zone x");
		System.out.println("5. Do Query 4");
		System.out.println("6. Do Query 5");
		System.out.println("7. Do Query 6");
		System.out.println("8. Do Query 7");
		System.out.println("9. Do Query 8");
		System.out.println("10. Do Query 9");
		System.out.println("11. Do Query 10");
		System.out.println("12. Exit");
	}

	private static String cleanInput(String input) {
		input.replace("\\n", " ");
		input.replace("\\r", " ");
		return input.trim();
	}

	private static void clearBuffer(byte[] buffer) {
		for (int i = 0; i < buffer.length - 1; i++) {
			buffer[i] = 0;
		}
	}

	private static void handleInput(String input) throws FileNotFoundException {
		int inputNumber = 0;

		try {
			inputNumber = Integer.valueOf(input);
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<QuerySolution> querySolutions = new LinkedList<>();
		
		if(inputNumber >=2 && inputNumber < 12){
			querySolutions = makeQuery(inputNumber);
		}
		
		switch (inputNumber) {
			case 1:
				loadAllClassesOnt(ONTOLOGY_FILE);
				break;
	
			case 2:
				for (QuerySolution solution : querySolutions) {
					RDFNode name = solution.get("sensor");
					RDFNode tag = solution.get("id");
					System.out.println("Name:" + cleanStringOutput(name) + "| Tag ID: " + cleanStringOutput(tag));
				}
				break;
			case 3:
				for (QuerySolution solution : querySolutions) {
					RDFNode sensor = solution.get("sensor");
					RDFNode sensorId = solution.get("sId");
					System.out.println("Sensor: " + sensor.asResource().getLocalName() + "| SensorId: " + cleanStringOutput(sensorId));
				}
				break;
			case 4:
				double sum = 0;
				for (QuerySolution solution : querySolutions) {
					RDFNode sensor = solution.get("value");
					String output = cleanStringOutput(sensor);
					sum += Double.valueOf(output);
					System.out.println("Temperature Value: " + output);
				}
				System.out.println("Average Value: " + (sum/querySolutions.size()));
				break;
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
	
				break;
			case 12:
				running = false;
				break;
			default:
				break;
		}
	}

	private static String cleanStringOutput(RDFNode node) {
		if(node != null && node instanceof Literal) {
			return node.asLiteral().getLexicalForm();
		}
		return "NULL";
	}

	private static List<QuerySolution> makeQuery(int inputNumber) throws FileNotFoundException {
		if (ontoModel == null) {
			System.out.println("Ontology model not loaded yet, loading it befor starting query...");
			loadAllClassesOnt(ONTOLOGY_FILE);
		}

		List<QuerySolution> result = new LinkedList<QuerySolution>();

		System.out.println("Executing Query...");
		String queryString = queries[inputNumber - 2];
		Query query = QueryFactory.create(queryString);
		
		QueryExecution queryExecution = QueryExecutionFactory.create(query, ontoModel);
		ResultSet resultSet = queryExecution.execSelect();
		
		System.out.println("Found " + result.size() + " results");
		
		while (resultSet.hasNext()) {
			result.add(resultSet.next());
		}
		return result;
	}

	private static void loadAllClassesOnt(String localSource) throws FileNotFoundException {
		System.out.println("Loading " + localSource);
		ontoModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM, null);
		ontoModel.read(new FileInputStream(localSource), null);
	}

}
