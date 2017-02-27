package progetto;

import java.io.File;

public class main {
	
public static void main(String[] args){
		System.out.println("hello world");
		try{	    	
	    	Test x = new Test(2);
	    	x.login();
	    	int jobID = x.execute(Queries.QUERY1.text());//10 most used programs
//execute() will set the class internal value to the output of the program if everything goes fine and return the jobID	    	
   			String input= x.getOutpuBoa(52141);
   			x.parseXml(new File("C:/progetto tesi/eclipse workspace/progetto/src/progetto/output1.xml") );
	    	x.parseOutputBoa(input);
	    	x.freemarker("templateCsv.ftlh", "csv.csv");
	    	x.freemarker("templateHTML.ftlh", "html.html");

	    //job churn rate, i need to change the parsing xml;
	    	jobID = 52384;
	    	input = x.getOutpuBoa(jobID);
	    	x.parseXml(new File("C:/progetto tesi/eclipse workspace/progetto/src/progetto/output2.xml") );
	    	x.parseOutputBoa();
	    	x.freemarker("templateCsv.ftlh", "csv2.csv");
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("\nI'm done; goodbye cruel world");
	}

}
