<html>
 <#list linguaggio as l, val>
		<b>${l}</b>:${val}<br>
	  <#else>None
	  </#list>
	  <br>
	  <br>
	  <#list linguaggio as l, val>['${l}', ${val}], <#else></#list>
	  
</body>
</html>