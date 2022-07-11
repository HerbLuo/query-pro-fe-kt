<#-- @ftlvariable name="m" type="cn.cloudself.query.util.TemplateModel" -->
<#assign ClassName = m._ClassName/>
<#assign className = m._ClassName?uncap_first/>
package ${m.packagePath};

<#if m.hasBigDecimal>
import java.math.BigDecimal;
</#if>
<#if m.hasDate>
import java.util.Date;
</#if>
<#if m.swaggerSupport>
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
</#if>
import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * ${m.remark}
 */
<#if m.id??>
@Entity
</#if>
<#if m.swaggerSupport>
@ApiModel(description = "${m.remark}")
</#if>
@Table(name = "${m.db_name}")
public class ${ClassName} implements Serializable {
    private static final long serialVersionUID = 1L;

<#list m.columns as field>
    /** ${field.remark} */
<#if m.id?? && m.id.column == field.db_name>
    @Id<#if m.id.autoIncrement>
    @GeneratedValue(strategy = GenerationType.IDENTITY)</#if>
</#if>
<#if m.swaggerSupport>
    @ApiModelProperty("${field.remark}")
</#if>
    @Column(name = "${field.db_name}")
    private ${field.javaTypeStr} ${field.propertyName};

</#list>
<#list m.columns as field>
    public ${field.javaTypeStr} get${field.propertyName?cap_first}() {
        return ${field.propertyName};
    }

    public <#if m.chainForModel>${ClassName}</#if><#if !m.chainForModel>void</#if> set${field.propertyName?cap_first}(${field.javaTypeStr} ${field.propertyName}) {
        this.${field.propertyName} = ${field.propertyName};
    <#if m.chainForModel>
        return this;
    </#if>
    }

</#list>
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ${ClassName} ${className} = (${ClassName}) o;
        return <#list m.columns as field>Objects.equals(${field.propertyName}, ${className}.${field.propertyName})<#sep> && </#list>;
    }

    @Override
    public int hashCode() {
        return Objects.hash(<#list m.columns as field>${field.propertyName}<#sep>, </#list>);
    }

    @Override
    public String toString() {
        return "${ClassName}{" +
            <#list m.columns as field>
                ", ${field.propertyName}=<#if field.javaTypeStr == "String">'</#if>" + ${field.propertyName} +<#if field.javaTypeStr == "String"> '\'' +</#if>
            </#list>
                '}';
    }

    <#--noinspection FtlReferencesInspection-->
    <@m.entityExCodes?interpret />
}
