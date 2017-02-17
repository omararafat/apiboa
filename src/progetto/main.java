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
	    	 val = Integer.parseInt(token);
//recupero valore
	    	 token = st.nextToken("\n");		//"] = 16.5 "
	    	 token = token.replace("] = ", "");	//=16.5
	    	 val2 = Double.parseDouble(token);
//inserisco la coppia ottenuta nella HashMap
	    	 coppia1.put("id", val);
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
		Desktop.getDesktop().browse( new URI("file:///"+pathFile+ outputFile) );
		
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
	
	public static void main(String[] args){
		System.out.println("hello world");
		
		try{
	//login architettura BOA
			final BoaClient client = new BoaClient();
			client.login(args[0], args[1]);
			System.out.println("login succesfull");	
			
			JobHandle job = client.getJob(52383);
			String s= job.getOutput();
			System.out.println(s);
			
	
		//	HashMap<String, Integer> dataModel = parserizzaOutput1(s);//job 52389, 52655
			HashMap<String, Integer> dataModel = parserizzaOutput2(s);//per job 52383, 52384
			
		//	System.out.println("dimensione mappa: "+dataModel.size() );			
			
	/*	//debug: stampo mappa ottenuta per controllo interno dei valori
	 		for( Map.Entry<String, Integer> entry : dataModel.entrySet()) {
				System.out.println(entry+" : "+entry.getValue());
			}*/
			

			
			System.out.println("freemarker start");
			freemarker(dataModel,"test2.ftlh", "outputJob52383.html");
					
			
			
			
			
			
			
			
	//...		Desktop.getDesktop().browse(new URI("https://www.google.it"));
			
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
		
		System.out.println("terminato");
	}

}
