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
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class Main {

	private static final String ONTOLOGY_FILE = "onto.owl";
	private static boolean running = true;
	private static OntModel ontoModel;
	
	private static String PREFIXES = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX tcd: <http://www.scss.tcd.ie/pg/ruckr#>\n";
	
	private static String queries[] = new String[]{
		PREFIXES + "SELECT ?person WHERE { }",
		PREFIXES + "SELECT ?person WHERE { }",
	};
	
	public static void main(String args[]) throws IOException {

		System.out.println("Menu:");
		System.out.println("=============");
		System.out.println("1. Load Ontology file");
		System.out.println("2. Do Query 1");
		System.out.println("3. Do Query 2");
		System.out.println("4. Do Query 3");
		System.out.println("5. Do Query 4");
		System.out.println("6. Do Query 5");
		System.out.println("7. Do Query 6");
		System.out.println("8. Do Query 7");
		System.out.println("9. Do Query 8");
		System.out.println("10. Do Query 9");
		System.out.println("11. Do Query 10");
		System.out.println("12. Exit");

		String input = "";
		byte[] buffer = new byte[100];
		while (running) {
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

		switch (inputNumber) {
		case 1:
			loadAllClassesOnt(ONTOLOGY_FILE);
			break;

		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:
		case 11:
			makeQuery(inputNumber);
			break;
		case 12:
			running = false;
			break;
		default:
			break;
		}
	}

	private static List<QuerySolution> makeQuery(int inputNumber) throws FileNotFoundException {
		if (ontoModel == null) {
			System.out.println("Ontology model not loaded yet, loading it befor starting query...");
			loadAllClassesOnt(ONTOLOGY_FILE);
		}

		List<QuerySolution> result = new LinkedList<QuerySolution>();

		System.out.println("Executing Query...");
		Query query = QueryFactory.create(queries[inputNumber - 1]);
		QueryExecution queryExecution = QueryExecutionFactory.create(query, ontoModel);
		ResultSet resultSet = queryExecution.execSelect();

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
