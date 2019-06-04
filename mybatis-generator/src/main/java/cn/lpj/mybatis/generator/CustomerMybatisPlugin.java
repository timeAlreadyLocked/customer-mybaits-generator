package cn.lpj.mybatis.generator;

import com.itfsw.mybatis.generator.plugins.utils.JavaElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author LiPengJu
 * @date 2019/3/21
 */
public class CustomerMybatisPlugin extends PluginAdapter {

    private String item = "item";

    /**
     * 验证插件的配置是否正确
     */
    public boolean validate(List<String> warnings) {
        return true;
    }


    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        String classAnnotation = "@ApiModel(value=\"\")";
        if(!topLevelClass.getAnnotations().contains(classAnnotation)) {
            topLevelClass.addAnnotation(classAnnotation);
        }
        String apiModelAnnotationPackage =  properties.getProperty("apiModelAnnotationPackage");
        String apiModelPropertyAnnotationPackage = properties.getProperty("apiModelPropertyAnnotationPackage");
        if(null == apiModelAnnotationPackage) apiModelAnnotationPackage = "io.swagger.annotations.ApiModel";
        if(null == apiModelPropertyAnnotationPackage) apiModelPropertyAnnotationPackage = "io.swagger.annotations.ApiModelProperty";

        topLevelClass.addImportedType(apiModelAnnotationPackage);
        topLevelClass.addImportedType(apiModelPropertyAnnotationPackage);

        field.addAnnotation("@ApiModelProperty(value=\"" + introspectedColumn.getJavaProperty() + introspectedColumn.getRemarks() + "\")");
        return super.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
    }

    /**
     * 在接口中添加方法
     */
    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String objectName = introspectedTable.getTableConfiguration().getDomainObjectName();//对象名称
        FullyQualifiedJavaType modelTypeWithoutBLOBs = JavaElementGeneratorTools.getModelTypeWithoutBLOBs(introspectedTable);
        if (objectName == null)
            objectName = modelTypeWithoutBLOBs.getFullyQualifiedName();
        interfaze.addImportedType(new FullyQualifiedJavaType("java.util.List"));

        Method method = new Method();//
//        method.addJavaDocLine("/**");
//        method.addJavaDocLine(" * Batch update or insert. Parameters can not be more than 2100");
//        method.addJavaDocLine(" * list of size not greater than 1000");
//        method.addJavaDocLine(" */");
        method.setName("updateBySelectiveBatch");
        method.addParameter(new Parameter(new FullyQualifiedJavaType("java.util.List<" + objectName + ">"), "list"));
        method.setReturnType(new FullyQualifiedJavaType("void"));

		/*该行代码的作用：当commentGenerator配置为false时，接口可以生成注释代码。
	              没有意义，所以注释，其他新加的方法已经删除*/
        //context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        interfaze.addMethod(method);
        method = new Method();//
        method.setName("updateBatch");
        method.addParameter(new Parameter(new FullyQualifiedJavaType("java.util.List<" + objectName + ">"), "list"));
        method.setReturnType(new FullyQualifiedJavaType("void"));
        interfaze.addMethod(method);
        method = new Method();//
        method.setName("insertBatch");
        method.addParameter(new Parameter(new FullyQualifiedJavaType("java.util.List<" + objectName + ">"), "list"));
        method.setReturnType(new FullyQualifiedJavaType("void"));
        interfaze.addMethod(method);
        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper"));
        interfaze.addAnnotation("@Mapper");
        return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
    }

    /**
     * 在xml文件中添加需要的元素
     */
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        XmlElement parentElement = document.getRootElement();
        String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();//数据库表名

        parentElement.addElement(getUpdateBatchBySelectiveElement(introspectedTable, tableName));

        parentElement.addElement(getUpdateBatchElement(introspectedTable, tableName));//批量更新

        parentElement.addElement(getInsertBatchElement(introspectedTable, tableName));//批量插入

        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    /**
     * 批量修改BySelective
     *
     * @param introspectedTable
     * @param tableName
     * @return
     */
    public XmlElement getUpdateBatchBySelectiveElement(IntrospectedTable introspectedTable, String tableName) {
        XmlElement updateBatchElement = new XmlElement("update");
        updateBatchElement.addAttribute(new Attribute("id", "updateBySelectiveBatch"));

        XmlElement foreachElement = NewForeachElement();
        XmlElement ifElement = NewIfElement(introspectedTable.getPrimaryKeyColumns());

		/*该行代码的作用：当commentGenerator配置为false时，sql可以生成注释代码。
		     没有意义，所以注释，其他新加的方法已经删除*/
        //context.getCommentGenerator().addComment(updateBatchElement);

        StringBuilder sb = new StringBuilder();
        sb.append("update ").append(tableName);
        ifElement.addElement(new TextElement(sb.toString()));

        XmlElement dynamicElement = new XmlElement("set");
        ifElement.addElement(dynamicElement);

        for (IntrospectedColumn introspectedColumn : introspectedTable.getNonPrimaryKeyColumns()) {
            XmlElement isNotNullElement = new XmlElement("if");
            isNotNullElement.addAttribute(new Attribute("test", introspectedColumn.getJavaProperty(item + ".") + " != null"));
            dynamicElement.addElement(isNotNullElement);

            sb.setLength(0);
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, item + "."));
            sb.append(',');

            isNotNullElement.addElement(new TextElement(sb.toString()));
        }

        boolean and = false;
        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
            sb.setLength(0);
            if (and) {
                sb.append("  and ");
            } else {
                sb.append("where ");
                and = true;
            }

            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, item + "."));
            ifElement.addElement(new TextElement(sb.toString()));
        }

        foreachElement.addElement(ifElement);
        updateBatchElement.addElement(foreachElement);

        return updateBatchElement;
    }

    /**
     * 批量修改
     *
     * @param introspectedTable
     * @param tableName
     * @return
     */
    public XmlElement getUpdateBatchElement(IntrospectedTable introspectedTable, String tableName) {
        XmlElement updateBatchElement = new XmlElement("update");
        updateBatchElement.addAttribute(new Attribute("id", "updateBatch"));

        XmlElement foreachElement = NewForeachElement();
        XmlElement ifElement = NewIfElement(introspectedTable.getPrimaryKeyColumns());

        StringBuilder sb = new StringBuilder();
        sb.append("update ").append(tableName);
        ifElement.addElement(new TextElement(sb.toString()));

        // set up for first column
        sb.setLength(0);
        sb.append("set ");

        Iterator<IntrospectedColumn> iter = introspectedTable.getNonPrimaryKeyColumns().iterator();
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();

            sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, item + "."));

            if (iter.hasNext()) {
                sb.append(',');
            }

            ifElement.addElement(new TextElement(sb.toString()));

            // set up for the next column
            if (iter.hasNext()) {
                sb.setLength(0);
                OutputUtilities.xmlIndent(sb, 1);
            }
        }

        boolean and = false;
        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
            sb.setLength(0);
            if (and) {
                sb.append("  and ");
            } else {
                sb.append("where ");
                and = true;
            }

            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, item + "."));
            ifElement.addElement(new TextElement(sb.toString()));
        }

        foreachElement.addElement(ifElement);
        updateBatchElement.addElement(foreachElement);

        return updateBatchElement;
    }

    /**
     * 批量添加
     *
     * @param introspectedTable
     * @param tableName
     * @return
     */
    public XmlElement getInsertBatchElement(IntrospectedTable introspectedTable, String tableName) {
        XmlElement insertBatchElement = new XmlElement("insert");
        insertBatchElement.addAttribute(new Attribute("id", "insertBatch"));

        XmlElement foreachElement = NewForeachElement();

        StringBuilder insertClause = new StringBuilder();
        StringBuilder valuesClause = new StringBuilder();

        insertClause.append("insert into ");
        insertClause.append(tableName);
        insertClause.append(" (");

        valuesClause.append("values (");

        List<String> valuesClauses = new ArrayList<String>();
        Iterator<IntrospectedColumn> iter = introspectedTable.getAllColumns().iterator();
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();
            if (introspectedColumn.isIdentity()) {
                // cannot set values on identity fields
                continue;
            }

            insertClause.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            valuesClause.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, item + "."));
            if (iter.hasNext()) {
                insertClause.append(", ");
                valuesClause.append(", ");
            }

            if (valuesClause.length() > 80) {
                foreachElement.addElement(new TextElement(insertClause.toString()));
                insertClause.setLength(0);
                OutputUtilities.xmlIndent(insertClause, 1);

                valuesClauses.add(valuesClause.toString());
                valuesClause.setLength(0);
                OutputUtilities.xmlIndent(valuesClause, 1);
            }
        }

        insertClause.append(')');
        foreachElement.addElement(new TextElement(insertClause.toString()));

        valuesClause.append(')');
        valuesClauses.add(valuesClause.toString());

        for (String clause : valuesClauses) {
            foreachElement.addElement(new TextElement(clause));
        }

        insertBatchElement.addElement(foreachElement);

        return insertBatchElement;
    }

    /**
     * @return
     */
    public XmlElement NewForeachElement() {
        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "list"));
        foreachElement.addAttribute(new Attribute("item", item));
        foreachElement.addAttribute(new Attribute("index", "index"));
        foreachElement.addAttribute(new Attribute("separator", ";"));
        return foreachElement;
    }

    /**
     * @param primaryKeyColumns
     * @return
     */
    public XmlElement NewIfElement(List<IntrospectedColumn> primaryKeyColumns) {
        StringBuilder sb = new StringBuilder();
        boolean flag = false;
        for (IntrospectedColumn introspectedColumn : primaryKeyColumns) {
            if (flag) {
                sb.append(" and ");
                sb.append(item).append(".");
                sb.append(introspectedColumn.getJavaProperty());
                sb.append(" != null");
            } else {
                sb.append(item).append(".");
                sb.append(introspectedColumn.getJavaProperty());
                sb.append(" != null");
                flag = true;
            }
        }
        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", sb.toString()));
        return ifElement;
    }
}
