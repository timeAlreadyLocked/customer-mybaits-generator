#自定义Mybatis代码生成器
mybatis-generator-core模块是修改后的源码，修改点：添加javaExampleGenerator标签用于修改example实体类的位置
原理：修改源码增加javaExampleGenerator标签的读取和生成逻辑，修改dtd文件添加javaExampleGenerator节点

#拓展的插件
1.自动生成swigger2注解
2.自动生成@mapper注解
3.增加批量插入，批量更新，查询单条方法
4.增加可选择批量插入，批量更新
5.根据数据库类型自动生成不同的sql

#使用方法
1.项目拉到本地
2.编译工具配置好环境
3.分别打包mybatis-generator,mybatis-generator模块
4.修改mybatis-generator-boot模块下的generatorConfig.properties文件和generatorConfig.xml对应的表
5.使用maven可视化插件generate运行或定位到mybatis-generator-boot目录下使用mvn mybatis-generator:generate运行
