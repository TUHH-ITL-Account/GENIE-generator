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
    <input type="text" id="ANSWER0" name="ANSWER0" size="15"<#if isSolution>value="${exercise.answer}"</#if>>
  </form>
  <#include "/common/AdditionalElements.ftl">
<#if fullHtml>
</body>
</html>
</#if>