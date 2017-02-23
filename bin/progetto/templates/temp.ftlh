<#setting number_format="computer">
ID,churnRate
<#list key as k, value>

	.
		<#list value as k2, val2>
${val},${val2} <#else></#list>
	<#else></#list>
<#else></#list>

<#list key as k, value>
	<#list k as k1, val>
		<#list value as k2, val2>
{
    "item": {
        "id": "${val}",
		"chrunRate": "${val2}"
		}
}
		<#else></#list>
	<#else></#list>
<#else></#list>
