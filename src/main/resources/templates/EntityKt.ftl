<#-- @ftlvariable name="m" type="cn.cloudself.query.util.TemplateModel" -->
package ${m.packagePath}

<#if m.swaggerSupport>
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
</#if>
<#if m.hasBigDecimal>
import java.math.BigDecimal
</#if>
<#if m.hasDate>
import java.util.Date
</#if>import javax.persistence.*

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
data class ${m._ClassName}(
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
    var ${field.propertyName}: ${field.ktTypeStr}?<#if !m.noArgMode> = null</#if>,

</#list>
)
