<#if fullHtml>
<!DOCTYPE html>
<html>
<#include "/common/htmlHead.ftl">
<body>
</#if>
  <h1>${exercise.title}</h1>

  <#list exercise.exerciseText>
    <#items as txt>
      <p>${txt}</p>
    </#items>
  </#list>

  <#assign i = 0>
  <#list exercise.solutionMap as key, value>
    <p>${exercise.aliasMap[key]} \(${exercise.mathjaxIdMap[key]}\)<#if exercise.usedUnits[key]?has_content> in ${exercise.usedUnits[key]}</#if>:
    <input type="text" id="ANSWER${i}" name="ANSWER${i}" size="15"<#if isSolution>value="${value}"</#if>></p>
    <#assign i++>
  </#list>

  <#include "/common/AdditionalElements.ftl">
<#if fullHtml>
</body>
</html>
</#if>