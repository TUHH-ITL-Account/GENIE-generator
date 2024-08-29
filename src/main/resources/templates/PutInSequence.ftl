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
    <#assign i = 0>
    <#list exercise.randomizedOrder>
      <table>
      <#items as element>
        <tr>
          <td style="padding: 5px 0">
            <#if exercise.givenIndexes?seq_contains(i)>
            <p id="ANSWER${i}" name="ANSWER${i}" style="display:inline">${element+1}</p>
            <#else>
            <input type="text" id="ANSWER${i}" name="ANSWER${i}" value="<#if isSolution><#if exercise.multiOccurrences[(element)?c]??><#list exercise.multiOccurrences[(element)?c] as same>${same+1}<#sep>/</#sep></#list><#else>${element+1}</#if></#if>" size ="3">
            </#if>
          </td>
          <td style="padding: 5px 0 5px 5px">
            <label for="ANSWER${i}">${exercise.solMap[element?c]}</label>
          </td>
        </tr>
        <#assign i++>
        </#items>
      </table>
    </#list>
    </fieldset>
  </form>
  <#include "/common/AdditionalElements.ftl">
<#if fullHtml>
</body>
</html>
</#if>