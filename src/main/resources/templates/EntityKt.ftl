<#-- @ftlvariable name="m" type="cn.cloudself.query.util.TemplateModel" -->
package ${m.packagePath}

<#if m.hasBigDecimal>import java.math.BigDecimal
</#if><#if m.hasDate>import java.util.Date
</#if>import javax.persistence.*

/**
 * ${m.remark}
 */
<#if m.id??>@Entity</#if>
@Table(name = "${m.db_name}")
data class ${m._ClassName}(
<#list m.columns as field>
    /** ${field.remark} */
<#if m.id?? && m.id.column == field.db_name>
    @Id<#if m.id.autoIncrement>
    @GeneratedValue(strategy = GenerationType.IDENTITY)</#if>
</#if>
    @Column(name = "${field.db_name}")
    var ${field.propertyName}: ${field.ktTypeStr}?<#if !m.noArgMode> = null</#if>,

</#list>
)
