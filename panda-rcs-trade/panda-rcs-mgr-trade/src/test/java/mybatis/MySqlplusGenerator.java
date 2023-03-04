/*
package mybatis;

import java.util.ArrayList;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.TemplateConfig;
import com.baomidou.mybatisplus.generator.config.po.TableFill;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

*/
/**
 * @ClassName:
 * @Description: mybatis-plus 连接mysql生成增删改查代码
 *//*

public class MySqlplusGenerator {
    public static void main(String[] args) {

        String moduleName = scanner("输入all 不区分路径生成，否则按照模块包名生成");//模块名可根据实际情况是否需要
        //指定表生成，不指定则全部生成
        //sc.setInclude("sys_parameter");
        String tables = scanner("输入all 全库生成 ，输入表名单个或多个，多个英文逗号分割");

        AutoGenerator ag = new AutoGenerator();
        //全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = System.getProperty("user.dir");//获取当前项目的路径
        projectPath = "C:";
        String projectName = "panda-rcs-common";//模块名可根据实际情况是否需要
        gc.setOutputDir(projectPath + "/" + projectName + "/src/main/java");//配置生成的代码目录
        //gc.setSwagger2(true);
        gc.setBaseColumnList(true);
        gc.setBaseResultMap(true);
        gc.setIdType(IdType.AUTO);  //自增
        gc.setDateType(DateType.ONLY_DATE);    //日期类型用 java.util.date
        gc.setServiceName("%sService");
        gc.setServiceImplName("%sServiceImpl");
        gc.setMapperName("%sMapper");
        gc.setXmlName("%sMapper");
        gc.setControllerName("%sController");
        ag.setGlobalConfig(gc);


        //数据源配置
        DataSourceConfig ds = new DataSourceConfig();
        ds.setUrl("jdbc:mysql://172.21.185.26:6606/panda_rcs?allowMultiQueries=true&useUnicode=true&characterEncoding=utf-8&allowPublicKeyRetrieval=true");
        ds.setDriverName("com.mysql.cj.jdbc.Driver");
        ds.setUsername("root");
        ds.setPassword("PAiF]Z9=~]x+52~LNjB-Xoy#2*h+aG)v3H");
        ag.setDataSource(ds);
        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setParent("com.panda.sport.rcs");

        if (StringUtils.isNotBlank(moduleName) && !moduleName.equals("all")) {
            //配置路径
            pc.setEntity("pojo");
            pc.setMapper("mapper");
            pc.setService("trade.service");
            pc.setServiceImpl("trade.service" + ".impl");
            pc.setController("trade.controller");
            pc.setXml("xml");
        }
        ag.setPackageInfo(pc);

        //策略配置
        StrategyConfig sc = new StrategyConfig();

        if (StringUtils.isNotBlank(tables) && !tables.equals("all")) {
            sc.setInclude(tables.split(","));
        }
        sc.setNaming(NamingStrategy.underline_to_camel);
        sc.setColumnNaming(NamingStrategy.underline_to_camel);
        sc.setEntityLombokModel(true);
        sc.setRestControllerStyle(true);//开启驼峰命名
//		sc.setSuperControllerClass("com.fl.play.base.BaseController");
        sc.setControllerMappingHyphenStyle(true);

        //自动填充的配置
        TableFill create_time = new TableFill("create_time", FieldFill.INSERT);//设置时的生成策略
        TableFill update_time = new TableFill("update_time", FieldFill.INSERT_UPDATE);//设置更新时间的生成策略
        ArrayList<TableFill> list = new ArrayList<>();
        list.add(create_time);
        list.add(update_time);
        sc.setTableFillList(list);

        ag.setStrategy(sc);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();
//        templateConfig.setController(null);
        ag.setTemplate(templateConfig);

        //执行生成文件
        ag.execute();
    }

    */
/**
     * <p>
     * 读取控制台内容,用于自己输入要生成的模块(生成后以文件夹形式)和表名
     * </p>
     *//*

    public static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append(tip + "：");
        System.out.println(help.toString());
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (StringUtils.isNotEmpty(ipt)) {
                return ipt;
            }
        }
        throw new MybatisPlusException("请输入正确的" + tip + "！");
    }
}

*/
