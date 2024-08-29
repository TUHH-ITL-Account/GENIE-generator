<#if fullHtml>
<!DOCTYPE html>
<html>
<#include "/common/htmlHead.ftl">
<body>
</#if>
  <h1>${exercise.title}</h1>
  <#assign i = 0>
  <#list exercise.partExercises as subEx>
    <#list subEx.texts>
      <#items as text>
        <p>${text}</p>
      </#items>
    </#list>
    <input type="text" id="ANSWER${i}" name="ANSWER${i}" size="15"<#if isSolution>value="${subEx.answer}"</#if>>
    <#assign i++>
  </#list>
  <#include "/common/AdditionalElements.ftl">
<#if fullHtml>
</body>
</html>
</#if>