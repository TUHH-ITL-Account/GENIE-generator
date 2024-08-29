<#if exercise.additionalElements??>
  <#list exercise.additionalElements>
    <#items as add>
      ${add}
    </#items>
  </#list>
</#if>