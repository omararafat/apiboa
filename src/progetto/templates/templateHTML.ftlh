<!--
<html>
<head>
</head>
<body>
 <#list key as k, val>
		${k}:${val}<br>
	  <#else>None
	  </#list>
	  <br>
	  <br>

	  
</body>
</html>
-->



<#setting number_format="computer">

<!DOCTYPE html>
<meta charset="utf-8">
<style>
	body {
	  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
	  width: 960px;
	  height: 500px;
	  position: relative;
	}
	path.slice{
		stroke-width:2px;
	}
	polyline{
		opacity: .3;
		stroke: black;
		stroke-width: 2px;
		fill: none;
	} 
	svg text.percent{
		fill:white;
		text-anchor:middle;
		font-size:12px;
	}
	
</style>

<body>
Output query: <br>
<#list key as k, val>['${k}', ${val}]<#sep>, <#else></#list>
<br>
<script src="http://d3js.org/d3.v3.min.js"></script>
<script src="Donut3D.js"></script>
	<script>
	  var resp = [
	  	<#list key as k, val>{key:'${k}', value: ${val}}<#sep>, <#else></#list>
	  ];	 

function getRandomColor() {
    var letters = '0123456789ABCDEF';
    var color = '#';
    for (var i = 0; i < 6; i++ ) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}

var svg = d3.select("body").append("svg").attr("width",700).attr("height",300);
svg.append("g").attr("id","queryResult");

Donut3D.draw("queryResult", mapData(), 450, 150, 130, 100, 30, 0);

function mapData(){
	return resp.map(function(resp){ 
		return {label:resp.key, value:resp.value, color:getRandomColor()};});
}
</script>

</body>