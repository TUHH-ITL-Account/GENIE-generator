<#if fullHtml>
<!DOCTYPE html>
<html>
<#include "/common/htmlHead.ftl">
<body>
</#if>
  <h1>${exercise.title}</h1>
  <#list exercise.texts>
    <#items as text>
      <p>${text}</p>
    </#items>
  </#list>
  <form>
    <fieldset>
      <div style="overflow:auto">
      <#assign i = 0>
      <#list exercise.treeParts as element>
        ${element}
        <#sep><input type="text" id="ANSWER${i}" name="ANSWER${i}" value="<#if isSolution>${exercise.answers[i]}</#if>" size ="${exercise.answers[i]?length}"></#sep>
        <#assign i++>
      </#list>
      </div>
    </fieldset>
  </form>
  <#include "/common/AdditionalElements.ftl">
<#if fullHtml>
</body>
</html>
</#if>