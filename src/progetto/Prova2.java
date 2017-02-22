package progetto;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;


public class Prova2 {
//array per salvare i tipi di variabile da leggere
	int valueCount;
	String regex[];
	String typeValue[];
	String valueName[];
//	int variableNumber;
//	arrayList<HashMap<valuePresiDaArray>
	
	public Prova2(int x) {
		// TODO Auto-generated constructor stub
		this.valueCount = x;
		this.regex = new String[x*2];
		this.typeValue = new String[x];
		this.valueName = new String[x];
	}

	
	public void parseXml(File file){
		try {
		
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);	
			doc.getDocumentElement().normalize();//optional, but recommended
			NodeList variablesList = doc.getElementsByTagName("values");

			int count = 0;
			for (int i = 0 ; i < variablesList.getLength(); i++){//number of different variables we'll expect to read
				Element x = (Element) variablesList.item(i);
				NodeList valueList = x.getElementsByTagName("value");

				for (int k = 0 ; k < valueList.getLength(); k++){//in here we read all the parameter we need
					Node valueNode = valueList.item(k);
					Element valueElement = (Element) valueNode;
		            NodeList list = valueElement.getElementsByTagName("*");
			        this.typeValue[k] = valueElement.getElementsByTagName("type").item(0).getTextContent();
			        this.valueName[k] = valueElement.getAttribute("name");
			        this.regex[count++]=valueElement.getElementsByTagName("start").item(0).getTextContent();
			        this.regex[count++]=valueElement.getElementsByTagName("end").item(0).getTextContent();
/*TODO here: 	this.valueCount =
	this.arrayTipiVAriabile =
	this.regex[]
*/			        
				}
				
			}
			for (int i = 0; i<this.valueCount*2; i++) System.out.println(i+" "+this.regex[i]);
			System.out.println();
			for (int i = 0; i<this.valueCount; i++) System.out.println(i+" "+this.typeValue[i]);
			
		} catch (Exception e) {
	    	e.printStackTrace();
	    }
	  }
	
	
	public void parseJson(String filePath){
/*		FileReader reader = new FileReader(new File("filename.json"));


	    JSONParser jsonParser = new JSONParser();
	    JSONArray jsonArray = (JSONArray) jsonParser.parse(reader);
	    JSONObject object = (JSONObject) jsonArray.get(0);
	    long elementaryProductId = (Long) object.get("elementaryProductId");
*/


/*TODO here: 	this.valueCount =
 				this.arrayTipiVAriabile =
 		StringReader reader = new StringReader("[]");
		JsonParser parser = Json.createParser(reader);
*//*		FileReader reader = new FileReader(filePath);
		
		JsonReader jsonParser  = Json.createReader(reader)) {
		JsonObject jsonObject = jsonParser.readObject();
		JsonObject object;
		
		JsonArray results = jsonObject.getJsonArray("data");
		
		JsonObject jsonObject = (Jsonbject) jsonParser.parse(reader);
	// get a String from the JSON object
		String firstName = (String) jsonObject.get("firstname");
		System.out.println("The first name is: " + firstName);
*/
		
		
		
	}
//TODO: rendere valueCount, attributo della classe e settarlo dentro parseJson()	
	public static void parseOutputBoa(String outputBoa, int valueCount, String[] regexs){
		int regexCount = valueCount*2; //start/end delimiter for each value
    	int start, end;
    	Pattern patterns[] = new Pattern[regexCount];
    	String outputs[] = new String[regexCount/2];
    	
    	for(int i = 0; i < regexCount; i++){
    		patterns[i] = Pattern.compile(regexs[i]);
    	}

    	Matcher match = patterns[0].matcher(outputBoa);//inizializzo il matching

	    while(match.find()){//right here we "parse" the variable name of the boa's output 
    		start = match.start();
	    	end = match.end();
	    	for (int i = 1; i < regexCount; i++){
		    	match.usePattern(patterns[i]);
		    	match.find();
		    	start = end;
		    	end = match.end();
		    	if( i%2 == 1){    	
		    		outputs[i/2] = outputBoa.substring(start, end-1);//right here we save the wanted value
		    	}

	    	}
	    	for(int k=0; k<valueCount; k++){ 		System.out.println(k+" "+outputs[k]);  	}
//TODO here: riempire this.ArrayList
	    	match.usePattern(patterns[0]);//before restarting the cycle we reset the initial pattern
	    }
	}


	
}
