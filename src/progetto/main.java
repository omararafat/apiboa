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
import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;

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
	    System.out.println("hai "+client.getJobCount()+" job inviati");
		
	    for (final JobHandle jh : client.getJobList()){		
			System.out.println(jh);
			if( jh.getExecutionStatus().equals(ExecutionStatus.FINISHED)
					//commenta righe seqguenti per evitare l'output dei job "avarage churn rate" 
					&& jh.getId()!=52384
					&& jh.getId()!=52383
					){ 
				System.out.println("output:\n"+jh.getOutput()+"\n");
			}
		}
	}

//top(X) of string weight int
	public static HashMap parserizzaOutput1(String var, String output){
		StringTokenizer st = new StringTokenizer(output);
		String token;
		int val;
		HashMap ris = new HashMap();	//HashMap<String, int>
		HashMap coppia = new HashMap();	//HashMap<String, int>
		System.out.println("\nparserizzaoutput\n");
		int i = 0;
//mi aspetto un output sottoforma di "counts[] = JavaScript, 1529.0\n"....
	     while (st.hasMoreTokens()) {
//elimino nome variabile output
	    	 token = st.nextToken("=");//"counts[] " oppure "\n counts[] "
//elimino segno uguale e spazio successivo
	    	 token = st.nextToken(" ");//"="
	    	 token = st.nextToken(" ");//"JavaScript,"
//sovrascriviamo la virgola finale e abbiamo ottenuto il linguaggio
	    	 token = token.replace(",", "");//"JavaScript"

/*una volta ottenuto il token contenente il numero,
 * elimino lo spazio iniziale e sovrascrivo la parte decimale.
 * NB: la query in questione restituisce sclusivamente numeri interi con parte decimale nulla */
	    	 val = Integer.parseInt( st.nextToken("\n").trim().replace(".0","") );//" 1529.0"->"1529.0"->"1529"

//inserisco la coppia ottenuta nella HashMap
	    	 coppia.put(token, val);
	    	 ris.put("linguaggio", coppia);
//	    	 ris.put(String.valueOf(i), token);
	    	 i++;
	     }
	     
	     return ris;
	}
	
	public static void freemarker(HashMap dataModel) throws Exception{
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
		
		
		Template temp = cfg.getTemplate("test.ftlh");
		FileWriter out = new FileWriter(new File("C:/progetto tesi/eclipse workspace/progetto/src/progetto/output/output.html"));
		temp.process(dataModel, out);
		
//TO-DO
		//modificare getDataModel in modo da avere chiave: linguaggio e sottochiave: valore numerico
		
	}
	
//copia output per evitare di attendere chiamata boa
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

	public static void main(String[] args){
		System.out.println("hello world");
		
		try{
	//login architettura BOA
/*			final BoaClient client = new BoaClient();
			client.login(args[0], args[1]);
			System.out.println("login succesfull");	
			
			JobHandle job = client.getJob(52389);
			String s= job.getOutput();
			System.out.println(s);
*/			
	
			HashMap<String, Integer> dataModel = parserizzaOutput1("counts",s1);
			
			System.out.println("dimensione mappa: "+dataModel.size() );

			for(String key: dataModel.keySet())
				System.out.println(key +" "+ dataModel.get(key));


			
			
			System.out.println("freemarker start");
			freemarker(dataModel);
					
			
			
			
			
			
			
			
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
