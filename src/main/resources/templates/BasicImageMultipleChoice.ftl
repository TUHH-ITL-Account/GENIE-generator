<#if fullHtml>
<!DOCTYPE html>
<html>
<#include "/common/htmlHead.ftl">
<body>
</#if>
  <style>
    img {
      max-height: 200px;
      width: auto;
      max-width:100%;
      /*display: inline-block;*/
    }
    input {
      margin-right: 10px;
    }
    li {
      display:flex;
      align-items:center;
      margin: 10px 0;
    }
  </style>
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
      <ul>
        <#items as opt>
          <li>
            <input type="checkbox" name="ANSWER${i}" id="ANSWER${i}" value="${opt}" <#if isSolution && exercise.correctOptions?seq_contains(opt)>checked</#if>>
            <label for="ANSWER${i}"><img src='${exercise.imageSourceDir}${opt}' /></label>
          </li>
          <#assign i++>
        </#items>
      </ul>
    </#list>
  </fieldset>
  </form>
  <#include "/common/AdditionalElements.ftl">
<#if fullHtml>
</body>
</html>
</#if>