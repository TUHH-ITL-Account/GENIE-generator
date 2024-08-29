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
    <div style="overflow:auto; margin-bottom: 10px;">
      <canvas id="myCanvas" width="${(exercise.imgWidth+100)?string.computer}" height="${(exercise.imgHeight+100)?string.computer}" style="border:1px solid #000000;"></canvas>
    </div>
    <script>
        var ctx = document.getElementById('myCanvas').getContext('2d');
        ctx.font = "22px Arial";
        var img = new Image();
        img.src = "${exercise.imageSource}";
        img.onload = function() {
          if(${exercise.origImgWidth?string.computer} > 960) {
            ctx.drawImage(img, 50, 50, 900, (900/${exercise.imgWidth?string.computer}) * ${exercise.imgHeight?string.computer});
          } else {
            ctx.drawImage(img, 50, 50);
          }
          <#assign i = 0>
          <#assign fac = exercise.imgWidth / exercise.origImgWidth>
          <#list exercise.top as key, value>
            ctx.fillText("${i+1}.", ${value[0]?string.computer}, ${value[1]?string.computer});
            ctx.moveTo(${(value[0]+8)?string.computer}, ${(value[1]+5)?string.computer});
            <#if fac == 1>
            ctx.lineTo(${(exercise.labelMap[key][0]+50)?string.computer}, ${(exercise.labelMap[key][1]+50)?string.computer});
            <#else>
            ctx.lineTo(${(fac*exercise.labelMap[key][0]+50)?string.computer}, ${(fac*exercise.labelMap[key][1]+50)?string.computer});
            </#if>
            ctx.stroke();
            <#assign i++>
          </#list>
          <#list exercise.left as key, value>
            ctx.fillText("${i+1}.", ${value[0]?string.computer}, ${value[1]?string.computer});
            ctx.moveTo(${(value[0]+20)?string.computer}, ${(value[1]-5)?string.computer});
            <#if fac == 1>
            ctx.lineTo(${(exercise.labelMap[key][0]+50)?string.computer}, ${(exercise.labelMap[key][1]+50)?string.computer});
            <#else>
            ctx.lineTo(${(fac*exercise.labelMap[key][0]+50)?string.computer}, ${(fac*exercise.labelMap[key][1]+50)?string.computer});
            </#if>
            ctx.stroke();
            <#assign i++>
          </#list>
          <#list exercise.right as key, value>
            ctx.fillText("${i+1}.", ${value[0]?string.computer}, ${value[1]?string.computer});
            ctx.moveTo(${(value[0]-3)?string.computer}, ${(value[1]-7)?string.computer});
            <#if fac == 1>
            ctx.lineTo(${(exercise.labelMap[key][0]+50)?string.computer}, ${(exercise.labelMap[key][1]+50)?string.computer});
            <#else>
            ctx.lineTo(${(fac*exercise.labelMap[key][0]+50)?string.computer}, ${(fac*exercise.labelMap[key][1]+50)?string.computer});
            </#if>
            ctx.stroke();
            <#assign i++>
          </#list>
          <#list exercise.bottom as key, value>
            ctx.fillText("${i+1}.", ${value[0]?string.computer}, ${value[1]?string.computer});
            ctx.moveTo(${(value[0]+10)?string.computer}, ${(value[1]-17)?string.computer});
            <#if fac == 1>
            ctx.lineTo(${(exercise.labelMap[key][0]+50)?string.computer}, ${(exercise.labelMap[key][1]+50)?string.computer});
            <#else>
            ctx.lineTo(${(fac*exercise.labelMap[key][0]+50)?string.computer}, ${(fac*exercise.labelMap[key][1]+50)?string.computer});
            </#if>
            ctx.stroke();
            <#assign i++>
          </#list>
        };
      </script>
    <fieldset>
      <#assign i = 0>
      <#list exercise.top as key, value>
        <p>${i+1}. <input type="text" id="ANSWER${i}" name="ANSWER${i}" size="15"<#if isSolution>value="${key}"</#if>></p>
        <#assign i++>
      </#list>
      <#list exercise.left as key, value>
        <p>${i+1}. <input type="text" id="ANSWER${i}" name="ANSWER${i}" size="15"<#if isSolution>value="${key}"</#if>></p>
        <#assign i++>
      </#list>
      <#list exercise.right as key, value>
        <p>${i+1}. <input type="text" id="ANSWER${i}" name="ANSWER${i}" size="15"<#if isSolution>value="${key}"</#if>></p>
        <#assign i++>
      </#list>
      <#list exercise.bottom as key, value>
        <p>${i+1}. <input type="text" id="ANSWER${i}" name="ANSWER${i}" size="15"<#if isSolution>value="${key}"</#if>></p>
        <#assign i++>
      </#list>
    </fieldset>
  </form>
  <#include "/common/AdditionalElements.ftl">
<#if fullHtml>
</body>
</html>
</#if>