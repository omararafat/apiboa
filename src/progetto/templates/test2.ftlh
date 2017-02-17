<html>
<head></head>
<body>
elenco id progetti+churn rate; prova visualizzazione output boa "output mean[string] of int;" <br>
<#setting number_format="computer">
eg: [id progetto: valore] <br>
<#list key as k, value>
	<#list k as k1, val>
		<#list value as k2, val2>
			[${val}: ${val2}] <#else></#list>
	<#else></#list><#sep><br>
<#else></#list>
</body>
</html>
