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
	

	public static void main(String[] args){
		// TODO Auto-generated method stub
		System.out.println("hello world");

		try(final BoaClient client = new BoaClient() ){
			client.login(args[0], args[1]);
			System.out.println("login succesfull");
			
	//		nuovaQuery(client);
			
	//		elenco job inviati e relativo stato + eventuale risultato
		    System.out.println("hai "+client.getJobCount()+" job inviati");
			
		    for (final JobHandle jh : client.getJobList()){		
				System.out.println(jh);
				if(jh.getExecutionStatus().equals(ExecutionStatus.FINISHED)){
					System.out.println("output:\n"+jh.getOutput()+"\n");
				}
			}
	
	//		System.out.println("\nelenco datasets attualmente disponibili");
	//		for (final InputHandle ih : client.getDatasets())	System.out.println(ih);
		
		}catch (LoginException e){
			System.out.println("errore login");
			e.printStackTrace();
		}catch (BoaException e){
			System.out.println("boaexception");
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("terminato");
	}

}
