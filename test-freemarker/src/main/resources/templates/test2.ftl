<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>钱包</td>
    </tr> <#list list as stu>
        <tr>
            <td>${stu_index + 1}</td>
            <td>${stu.name}</td>
            <td>${stu.age}</td>
            <td>${stu.money}</td>
        </tr> </#list>
    -----------------------------------------------

    <#list stuMap.list as stu>
        <tr>
            <td>${stu_index + 1}</td>
            <td
                <#if stu.name=="stu0">
                    style="background: red;"
                </#if>>${stu.name}
            </td>

            <td>${stu.age}</td>
            <td>${stu.money}</td>
        </tr>
    </#list>
</table>