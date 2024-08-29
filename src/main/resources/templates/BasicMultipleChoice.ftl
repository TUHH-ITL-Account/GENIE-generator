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
    <#list exercise.options>
      <table>
        <#items as opt>
        <tr>
          <td style="padding: 5px 0">
            <input type="checkbox" name="ANSWER${i}" id="ANSWER${i}" value="${opt}" <#if isSolution && exercise.correctOptions?seq_contains(opt)>checked</#if>>
          </td>
          <td style="padding: 5px 0 5px 5px">
            <label for="ANSWER${i}">${opt}</label>
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