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
      <p>
      <#assign i = 0>
      <#list exercise.textSplices as splice>
        <#if exercise.blankSplices?seq_contains(splice?index)>
          <input type="text" id="ANSWER${i}" name="ANSWER${i}" size="15"<#if isSolution>value="${splice}"</#if>>
          <#assign i++>
        <#else>
          ${splice}<#t>
        </#if>
      </#list>
      </p>
    </fieldset>
  </form>
  <#include "/common/AdditionalElements.ftl">
<#if fullHtml>
</body>
</html>
</#if>