package progetto;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import org.w3c.dom.Node;
import org.w3c.dom.Element;


public class Prova2 {
//array per salvare i tipi di variabile da leggere
	int valueCount;
	String regexValue[];
	String typeValue[];
	String valueName[];
	Pattern patterns[];
	HashMap dataModel;
	
//	int variableNumber;
//	arrayList<HashMap<valuePresiDaArray>
	
	public Prova2(int x) {
		// TODO Auto-generated constructor stub
		this.valueCount = x;
		this.typeValue = new String[x];
		this.valueName = new String[x];
		this.regexValue = new String[x*2];
		this.patterns = new Pattern[x*2];
		this.dataModel = new HashMap(); 
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

				if( valueList.getLength() != this.valueCount) System.out.println(variablesList.getLength()+"fsdsdsdsdsdsd");
//TODO lanciare errore

				for (int k = 0 ; k < valueList.getLength(); k++){//in here we read all the parameter we need
					Node valueNode = valueList.item(k);
					Element valueElement = (Element) valueNode;
		            NodeList list = valueElement.getElementsByTagName("*");
			        this.typeValue[k] = valueElement.getElementsByTagName("type").item(0).getTextContent();
			        this.valueName[k] = valueElement.getAttribute("name");
			        this.regexValue[count++]=valueElement.getElementsByTagName("start").item(0).getTextContent();
			        this.regexValue[count++]=valueElement.getElementsByTagName("end").item(0).getTextContent();
				}
				
			}
	/*		for (int i = 0; i<this.valueCount*2; i++) System.out.println(i+"regexvalue "+this.regexValue[i]);
			System.out.println();
			for (int i = 0; i<this.valueCount; i++) System.out.println(i+"typevalue "+this.typeValue[i]);*/
			
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


	public HashMap parseOutputBoa(String outputBoa){
		int regexCount = this.valueCount*2; //start/end delimiter for each value
    	int start, end;
    	String outputs[] = new String[this.valueCount];

    	for(int i = 0; i < regexCount; i++){
    		System.out.println("patter: "+i);
    		this.patterns[i] = Pattern.compile(this.regexValue[i]);
//    		System.out.println("patter: "+this.patterns[i]);
    	}

    	Matcher match = this.patterns[0].matcher(outputBoa);//inizializzo il matching
int count = 0;
	    	HashMap value = new HashMap();
	    while(match.find()){//right here we "parse" the variable name of the boa's output 
    		start = match.start();
	    	end = match.end();
	    	for (int i = 1; i < regexCount; i++){
		    	match.usePattern(this.patterns[i]);
		    	match.find();
		    	start = end;
		    	end = match.end();
		    	if( i%2 == 1){    	
		    		outputs[i/2] = outputBoa.substring(start, end-1);//right here we save the wanted value
//		HashMap< HashMap<String, linguaggio>, HashMap<int, value> >	????		    		
		   // 		System.out.println(this.typeValue[i/2]+" "+this.valueName[i/2]+"\t"+outputs[i/2]+"\n");
		    		
		    	}
	    	}
	    	value.put(outputs[0], outputs[1]);
	 //   	System.out.println("\t"+value);
	    	this.dataModel.put("key", value);
	//    	for(int k=0; k<this.valueCount; k++){  		System.out.println(k+" "+outputs[k]);		}
	    	match.usePattern(this.patterns[0]);//before restarting the cycle we reset the initial pattern
	    }
	    
	    System.out.println("stampa risultato:");
	//    System.out.println(result);
	    return this.dataModel;
	}
	
	
	public void freemarker(String template, String outputFile) throws Exception{
		// Create your Configuration instance, and specify if up to what FreeMarker
		// version (here 2.3.25) do you want to apply the fixes that are not 100%
		// backward-compatible. See the Configuration JavaDoc for details.
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);

		// Specify the source where the template files come from. Here I set a
		// plain directory for it, but non-file-system sources are possible too:
		cfg.setDirectoryForTemplateLoading(new File("C:/progetto tesi/eclipse workspace/progetto/src/progetto/templates"));

		// Set the preferred charset template files are stored in. UTF-8 is
		// a good choice in most applications:
		cfg.setDefaultEncoding("UTF-8");

		// Sets how errors will appear.
		// During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
//		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);

		// Don't log exceptions inside FreeMarker that it will thrown at you anyway:
		cfg.setLogTemplateExceptions(false);

		
//------fine creazione configurazione per freemarker--------
		
		
	//	OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream("C:/progetto tesi/eclipse workspace/progetto/src/progetto/output/output2.html", true), "UTF-8");
		Template temp = cfg.getTemplate(template);
		File x = new File("C:/progetto tesi/eclipse workspace/progetto/src/progetto/output/"+outputFile);
		FileWriter out = new FileWriter(x);
		temp.process(this.dataModel, out);
//lancio browser per visualizzare output
	//	Desktop.getDesktop().browse( new URI("file:///"+pathFile+ outputFile) );
		
	}
	
	

//TODO: rendere valueCount, attributo della classe e settarlo dentro parseJson()	
public static void parseOutputBoa2(String outputBoa, int valueCount, String[] regexs){
		int regexCount = valueCount*2; //start/end delimiter for each value
    	int start, end;
    	Pattern patterns[] = new Pattern[regexCount];
    	String outputs[] = new String[regexCount/2];
    	
    	for(int i = 0; i < regexCount; i++){
    		patterns[i] = Pattern.compile(regexs[i]);
    		System.out.println("patter: "+patterns[i]);
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
