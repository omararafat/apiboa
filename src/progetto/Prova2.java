package progetto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import edu.iastate.cs.boa.BoaClient;
import edu.iastate.cs.boa.BoaException;
import edu.iastate.cs.boa.CompileStatus;
import edu.iastate.cs.boa.ExecutionStatus;
import edu.iastate.cs.boa.InputHandle;
import edu.iastate.cs.boa.JobHandle;
import edu.iastate.cs.boa.LoginException;
import edu.iastate.cs.boa.NotLoggedInException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import org.w3c.dom.Node;
import org.w3c.dom.Element;


public class Prova2 {
//array per salvare i tipi di variabile da leggere
	int valueCount;
	String regexValue[];//the character mapping for retrieving values
	String typeValue[];
	String valueName[];
	Pattern patterns[];//for mapping the data into the dataModel for FreeMArker
	HashMap dataModel;
	BoaClient client;
	String outputBoa;
	
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
		this.client = new BoaClient();
		this.outputBoa = "";
	}

	public String getOutputBoa(){
		return this.outputBoa;
	}
	
	public void login(){
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		System.out.println("Enter the boa login data\nusername: ");
		String usr = reader.next(); 
		System.out.println("password: ");
		String pw = reader.next(); 
		try {
			this.client.login(usr, pw);
		} catch (LoginException e) {
			System.out.println("errore login.");
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("login succesfull");
	}

	public String getOutpuBoa(int jobID){
		JobHandle x;
		try {
			x = this.client.getJob(jobID);
		this.outputBoa = x.getOutput()+"\n";
		} catch (NotLoggedInException e) {
			System.out.println("you need to be logged in");
			e.printStackTrace();
		} catch (BoaException e1) {
			System.out.println("Boa exception, check the stackTrace");
			e1.printStackTrace();
		}
		return this.outputBoa;
	} 
	
	public static void waitCompileResult(JobHandle query) throws Exception{
		while(true){
			if(query.getCompilerStatus().equals(CompileStatus.ERROR)){
				System.out.println("compile error:");
				System.out.println(query.getCompilerErrors());
				return;
			}
			if(query.getCompilerStatus().equals(CompileStatus.FINISHED)){
				System.out.println("compile successfull");
				return;
				
			}
			if(query.getCompilerStatus().equals(CompileStatus.RUNNING)){
				System.out.println("compiling, we'll sleep for 10s");
				Thread.sleep(10000);
				
			}
			if(query.getCompilerStatus().equals(CompileStatus.WAITING)){
				System.out.println("compiling, we'll sleep for 5 s");
				Thread.sleep(5000);
			}
			query.refresh();
		}
	}

	public static String waitOutputBoa(JobHandle query)throws Exception{
		while(true){
			try{
				if(query.getExecutionStatus().equals(ExecutionStatus.ERROR)){
					System.out.println("error while executing job");
					return null;
				}
				if(query.getExecutionStatus().equals(ExecutionStatus.FINISHED)){
					return query.getOutput();
				}
				if(query.getExecutionStatus().equals(ExecutionStatus.RUNNING)){
					System.out.println("job is RUNNING; we'll sleep 15s"); 
					Thread.sleep(15000);
	
				}
				if(query.getExecutionStatus().equals(ExecutionStatus.WAITING)){
					System.out.println("job is WAITING; we'll sleep 10s"); 
					Thread.sleep(10000);
				}
			}catch (InterruptedException e) {
				e.printStackTrace();
				return "an exception occured while we were sleeping";
			}
			query.refresh();
		}
	}

	public int execute(File file){
		String s = "";
		try {
			Scanner scan = new Scanner(file);
			scan.useDelimiter("\\Z");  
			s = scan.next(); 
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		return execute(s);
	}
	
	public int execute(String code){
		try {
			JobHandle query = this.client.query(code);
			waitCompileResult(query);//check and wait for compile status
			String s = "";
			if( (s = waitOutputBoa(query)) !=null){
				this.outputBoa = s+"\n";//we add a final new line so the parsing will work for the last value  
				return query.getId();
			}else
				throw new Exception("Boa returned an empty resul for a successful job");
		} catch (NotLoggedInException e1) {
			System.out.println("you need to be logged in");
			e1.printStackTrace();
		} catch (BoaException e1) {
			System.out.println("Boa exception, check the stackTrace");
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public void printJobs() throws Exception{
	    System.out.println("you have sent "+this.client.getJobCount()+" job");
	    Scanner reader = new Scanner(System.in);  // Reading from System.in
		System.out.println("Do you want to print the outputs too? Y/N ");
		String choice = reader.next();
	    for (final JobHandle jh : this.client.getJobList()){		
			System.out.println("job description:\t"+jh);
			if( jh.getExecutionStatus().equals(ExecutionStatus.FINISHED) && choice.equals("Y") ){ 
				System.out.println("output:\n"+jh.getOutput()+"\n");
			}
		}
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

				if( valueList.getLength() != this.valueCount){
					System.out.println("expecting to read "+variablesList.getLength()+" values, but i'm configured to read "+this.valueCount+" different values\n terminating");
					System.exit(-4);					
				}

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
		} catch (Exception e) {
			System.out.println("something went wrong, check the StackTrace");
	    	e.printStackTrace();
	    }
	  }
	
	public HashMap parseOutputBoa(){
		return this.parseOutputBoa(this.outputBoa);
	}
	public HashMap parseOutputBoa(String outputBoa){
		int regexCount = this.valueCount*2; //start/end delimiter for each value
    	int start, end;
    	String outputs[] = new String[this.valueCount];

    	for(int i = 0; i < regexCount; i++){
    		this.patterns[i] = Pattern.compile(this.regexValue[i]);
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
//		    		System.out.println(this.typeValue[i/2]+" "+this.valueName[i/2]+"\t"+outputs[i/2]+"\n");
		    	}
	    	}
	    	value.put(outputs[0], outputs[1]);
	    	this.dataModel.put("key", value);
	//    	for(int k=0; k<this.valueCount; k++){  		System.out.println(k+" "+outputs[k]);		}
	    	match.usePattern(this.patterns[0]);//before restarting the cycle we reset the initial pattern
	    }
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
