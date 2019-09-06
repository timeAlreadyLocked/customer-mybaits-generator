package cn.lpj.mybatis.generator;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * @author LiPengJu
 * @date: 2019/9/6
 */
public class InnerTypeFullyQualifiedJavaType2 extends FullyQualifiedJavaType {
    private static final Logger logger = LoggerFactory.getLogger(InnerTypeFullyQualifiedJavaType2.class);
    private String outerType;

    public InnerTypeFullyQualifiedJavaType2(String fullTypeSpecification) {
        super(fullTypeSpecification);
        try {
            Field packageName = this.getClass().getSuperclass().getDeclaredField("packageName");
            packageName.setAccessible(true);
            String oldPackageName = this.getPackageName();
            packageName.set(this, oldPackageName.substring(0, oldPackageName.lastIndexOf(".")));
            this.outerType = oldPackageName.substring(oldPackageName.lastIndexOf(".") + 1);
        } catch (Exception var4) {
            logger.error("InnerTypeFullyQualifiedJavaType 赋值失败！", var4);
        }
    }

    public String getFullyQualifiedName() {
        String fullyQualifiedName = super.getFullyQualifiedName();
        return fullyQualifiedName;
    }

    public String getShortName() {
        return this.outerType + "." + super.getShortName();
    }
}
