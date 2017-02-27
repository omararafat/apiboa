package progetto;

public enum Queries {
//Example Queries from boa's site
		// Counting the 10 most used programming languages
		QUERY1(" p: Project = input;\n"+
				"counts: output top(10) of string weight int;\n"+
		
				"foreach (i: int; def(p.programming_languages[i]))\n"+
					"counts << p.programming_languages[i] weight 1;"),

		// Counting the number of projects written in more than one languages
		QUERY2("p: Project = input;\n"+
				"counts: output sum of int;\n"+
		
				"if (len(p.programming_languages) > 1)\n"+
					"counts << 1;"),
		// What are the churn rates for all projects
		QUERY3("p: Project = input;\n"+
				"counts: output mean[string] of int;\n"+

				"visit(p, visitor {\n"+
					"before node: Revision -> counts[p.id] << len(node.files);\n"+
				"});"),
		// Counting the number of projects written in more than one languages
		QUERY4("p: Project = input;\n"+
				"counts: output sum of int;\n"+
				
				"if (len(p.programming_languages) > 1)\n"+
					"counts << 1;"),
		//Counting projects using Scheme
		QUERY5("p: Project = input;\n"+
				"counts: output sum of int;\n"+
				
				"foreach (i: int; match(`^scheme$`, lowercase(p.programming_languages[i])))\n"+
					"counts << 1;");
	
	private final String queryCode;
	
	private Queries(final String text){
		this.queryCode = text;
	}
	
	public String text(){
		return queryCode;
	}
}
