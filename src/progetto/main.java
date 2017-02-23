package progetto;

import edu.iastate.cs.boa.BoaClient;
import edu.iastate.cs.boa.BoaException;
import edu.iastate.cs.boa.CompileStatus;
import edu.iastate.cs.boa.ExecutionStatus;
import edu.iastate.cs.boa.InputHandle;
import edu.iastate.cs.boa.JobHandle;
import edu.iastate.cs.boa.LoginException;
import edu.iastate.cs.boa.NotLoggedInException;

import java.lang.Thread;
import java.net.URI;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;

import freemarker.template.*; 

public class main {
	
	public static void risCompile(JobHandle query) throws Exception{
		while(true){
			if(query.getCompilerStatus().equals(CompileStatus.ERROR)){
				System.out.println("errore durante compilazione:");
				System.out.println(query.getCompilerErrors());
				return;
			}
			if(query.getCompilerStatus().equals(CompileStatus.FINISHED)){
				System.out.println("compilazione terminata");
				return;
				
			}
			if(query.getCompilerStatus().equals(CompileStatus.RUNNING)){
				System.out.println("compilazione in corso, attendo 2s");
				Thread.sleep(2000);
				
			}
			if(query.getCompilerStatus().equals(CompileStatus.WAITING)){
				System.out.println("compilazione in attesa, attendo 5 s");
				Thread.sleep(5000);
			}
			query.refresh();
		}
	}

	public static String attendiOutputQuery(JobHandle query)throws Exception{
		while(true){
			try{
				if(query.getExecutionStatus().equals(ExecutionStatus.ERROR)){
					System.out.println("errore esecuzione job");
					return null;
				}
				if(query.getExecutionStatus().equals(ExecutionStatus.FINISHED)){
					return "terminato";
				}
				if(query.getExecutionStatus().equals(ExecutionStatus.RUNNING)){
					System.out.println("job is RUNNING; sleep 15s"); 
					Thread.sleep(15000);
	
				}
				if(query.getExecutionStatus().equals(ExecutionStatus.WAITING)){
					System.out.println("job is WAITING; sleep 10s"); 
					Thread.sleep(10000);
				}
			}catch (InterruptedException e) {
				e.printStackTrace();
				return "eccezione attendiOutput";
			}
			query.refresh();
		}
	}
	
	public static void nuovaQuery(BoaClient client) throws Exception{

		//  invio di una query
		JobHandle query = client.query(Queries.QUERY4.testo());
		
		risCompile(query);//controllo stato compilatore
		
		if(attendiOutputQuery(query)!=null){
	//		System.out.println("\nexecutionstate:\t"+query.getExecutionStatus());
			System.out.println("risultato:\t"+query.getOutput());
	//		System.out.println("\nSource:\t"+ query.getSource());
			InputHandle x = query.getDataset();
			System.out.println("Dataset:\t"+x);
			System.out.println("JobHandle:"+query);
		}
	}
	
	public static void stampaJobs(BoaClient client) throws Exception{
	    System.out.println("hai inviato "+client.getJobCount()+" job");
		
	    for (final JobHandle jh : client.getJobList()){		
			System.out.println(jh);
			if( jh.getExecutionStatus().equals(ExecutionStatus.FINISHED)
					//decommenta righe seqguenti per avere l'output dei job "avarage churn rate": 
					//&& jh.getId()!=52384
					//&& jh.getId()!=52383
					){ 
				System.out.println("output:\n"+jh.getOutput()+"\n");
			}
		}
	}

//counts: output top(10) of string weight int;
//output sottoforma di "counts[] = JavaScript, 1529.0\n"....
	public static HashMap parserizzaOutput1(String output){
		StringTokenizer st = new StringTokenizer(output);
		String token;
		double val;
		HashMap ris = new HashMap();	//HashMap<String, int>
		HashMap coppia = new HashMap();	//HashMap<String, int>
		System.out.println("\nparserizzaoutput\n");
	     while (st.hasMoreTokens()) {
	    	 token = st.nextToken("=");			//"counts[] " oppure "\n counts[] "
	    	 token = st.nextToken(" ");			//"="
	    	 token = st.nextToken(" ");			//"JavaScript,"
	    	 token = token.replace(",", "");	//"JavaScript"
	    	 val = Double.parseDouble( st.nextToken("\n").trim() );//" 1529.0"->"1529.0"->1529.0
//inserisco la coppia ottenuta nella HashMap
	    	 coppia.put(token, val);
	    	 ris.put("key", coppia);
	     }
	     return ris;
	}
	
//counts: output mean[string] of int;
//output sottoforma di "counts[10084859] = 16.5 \n"....
	public static HashMap parserizzaOutput2(String output){
		StringTokenizer st = new StringTokenizer(output);
		String token;
		int val; 	//id progetto
		double val2;//risultato
		HashMap ris = new HashMap();
		HashMap ris2 = new HashMap();
		System.out.println("\nparserizzaoutpu2t\n");
	     while (st.hasMoreTokens()) {
	 		HashMap coppia1 = new HashMap();	//HashMap<String, int> per l'id progetto
	 		HashMap coppia2 = new HashMap();	//HashMap<String, double> per il ris
//recupero id
	    	 token = st.nextToken("[");			//"counts"
	    	 token = st.nextToken("]");			//"[10084859"
	    	 token = token.replace("[", "");	//"10084859"
	    //	 val = Integer.parseInt(token);
	    	 coppia1.put("id", token);
//recupero valore
	    	 token = st.nextToken("\n");		//"] = 16.5 "
	    	 token = token.replace("] = ", "");	//=16.5
	    	 val2 = Double.parseDouble(token);
//inserisco la coppia ottenuta nella HashMap
	    	 coppia2.put("val", val2);
	    	 ris.put(coppia1, coppia2);
	    	 ris2.put("key", ris);
	     }
	     return ris2;
	}

static String pathFile = "C:/progetto%20tesi/eclipse%20workspace/progetto/src/progetto/output/";
	public static void freemarker(HashMap dataModel,String test, String outputFile) throws Exception{
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
		Template temp = cfg.getTemplate(test);
		File x = new File("C:/progetto tesi/eclipse workspace/progetto/src/progetto/output/"+outputFile);
		FileWriter out = new FileWriter(x);
		temp.process(dataModel, out);
//lancio browser per visualizzare output
	//	Desktop.getDesktop().browse( new URI("file:///"+pathFile+ outputFile) );
		
	}
	
//copie output per evitare di attendere chiamata boa
//conteggio linguaggi dataset small	
static String s1="counts[] = JavaScript, 1529.0 \n"+
			"counts[] = Ruby, 942.0 \n"+
			"counts[] = Shell, 708.0 \n"+
			"counts[] = Python, 657.0 \n"+
			"counts[] = Java, 584.0 \n"+
			"counts[] = PHP, 490.0 \n"+
			"counts[] = C, 397.0 \n"+
			"counts[] = CSS, 371.0 \n"+
			"counts[] = C++, 315.0 \n"+
			"counts[] = Perl, 277.0 ";
	
//conteggio linguaggi dataset intero
static String s2 = "counts[] = JavaScript, 1473096.0 \n"+
"counts[] = Ruby, 889738.0 \n"+
"counts[] = Shell, 700831.0 \n"+
"counts[] = Python, 620213.0 \n"+
"counts[] = Java, 554864.0 \n"+
"counts[] = PHP, 489082.0 \n"+
"counts[] = C, 419044.0 \n"+
"counts[] = CSS, 354715.0 \n"+
"counts[] = C++, 333877.0 \n"+
"counts[] = Perl, 274174.0";

//inizio output churn rate
static String s3 = "counts[10084859] = 16.5 \n"+
"counts[10096336] = 6.5 \n"+
"counts[10187710] = 22.0 \n"+
"counts[10267115] = 4.578947368421052 \n"+
"counts[10278695] = 16.647058823529413 \n"+
"counts[10366506] = 4.428571428571429";
	
static String s4 = "counts[10084859] = asd, 88.5 \n"+
		"counts[10096336] =asd,  6.5 \n"+
		"counts[10187710] = asd, 22.0 \n"+
		"counts[10267115] = asd, 4.578947368421052 \n"+
		"counts[10278695] = asd, 16.647058823529413 \n"+
		"counts[10366506] = asd, 4.428571428571429";

	public static void parseOutputBoa(String outputBoa, int valueCount, String[] regexs){
		int regexCount = valueCount*2; //start/end delimiter for each value
    	int start, end;
    	//String regexs[] = new String[regexCount];
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
		    	match.usePattern(patterns[i%regexCount]);
		    	match.find();
		    	if (!match.hitEnd()){
			    	start = end;
			    	end = match.end();
			    	if( i%2 == 1){    	
			    		outputs[i/2] = outputBoa.substring(start, end-1);//right here we save the wanted value
			    	}
		    	}else{
		    		outputs[i/2] = outputBoa.substring(start, end-1);//here we save the final saved value
		    	}
	    	}
	    	for(int k=0; k<valueCount; k++){ 		System.out.println(k+" "+outputs[k]);  	}
	    	match.usePattern(patterns[0]);//before restarting the cycle we reset the initial pattern
	    }
	}


public static void main(String[] args){
		System.out.println("hello world");
		try{
			String regexs[] = new String[4];	
 			regexs[0] = "=";
	    	regexs[1] = ",";
	    	regexs[2] = " ";
	    	regexs[3] = "\n";
   	Prova2 x1 = new Prova2(2);
   	
   			String input= "";
			
			input=s1+"\n";	    
   			x1.parseXml(new File("C:/progetto tesi/eclipse workspace/progetto/src/progetto/prova.xml") );
	    	x1.parseOutputBoa(input);
	    	x1.freemarker("test.ftlh", "prova");
	    	
	    	input=s2+"\n";
	    	x1.parseOutputBoa(input);
	    	x1.freemarker("test.ftlh", "prova2");
	    	
	    	input=s3+"\n";	    
   			x1.parseXml(new File("C:/progetto tesi/eclipse workspace/progetto/src/progetto/prova2.xml") );
	    	x1.parseOutputBoa(input);
	    	x1.freemarker("test.ftlh", "prova3");
	    	
	    	
	    	//x1.parseOutputBoa2(input, 2, regexs);
/*TODO: dentro pareseOutputBoa2 far restituire Arraylist<<T><T>> da dare in pasto a freemarker()
 * 		+ vedere se necessarie modifiche al template; facile dato che si aspettava mappa di mappe :'(
 * */	    
			
			
			
	//login architettura BOA
		/*	final BoaClient client = new BoaClient();
			client.login(args[0], args[1]);
			System.out.println("login succesfull");	
			
			JobHandle job = client.getJob(52383);
			String s= job.getOutput();
			System.out.println(s);*/
			
	/*
			HashMap<String, Integer> dataModel = parserizzaOutput1(s1);//job 52389, 52655
		//	HashMap<String, Integer> dataModel = parserizzaOutput2(s3);//per job 52383, 52384
			
			System.out.println("dimensione mappa: "+dataModel.size() );			
			
		//debug: stampo mappa ottenuta per controllo interno dei valori
	 		for( Map.Entry<String, Integer> entry : dataModel.entrySet()) {
				System.out.println(entry+" : "+entry.getValue());
			}
			
			System.out.println("freemarker start");
//USAGE (data model, template, nomefileoutput)
			freemarker(dataModel,"test.ftlh", "prova");
		
			
	//		nuovaQuery(client);
			
	//		elenco job inviati e relativo stato + eventuale risultato
	// 		stampaJobs(client);
	
	//		System.out.println("\nelenco datasets attualmente disponibili");
	//		for (final InputHandle ih : client.getDatasets())	System.out.println(ih);
		
/*		}catch (LoginException e){
			System.out.println("errore login");
			e.printStackTrace();
		}catch (BoaException e){
			System.out.println("boaexception");
			e.printStackTrace();*/
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("\nterminato");
	}

}
