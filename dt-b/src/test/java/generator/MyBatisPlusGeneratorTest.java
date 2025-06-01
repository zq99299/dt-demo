package generator;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static java.lang.StringTemplate.STR;

public class MyBatisPlusGeneratorTest {
    static String url = "jdbc:mysql://mrcode.cn:3306/dt_demo_b?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&useSSL=false";
    static String username = "root";
    static String password = "123456";
    static String parentPackage = "cn.mrcode.dtdemo.b.repo";
    static String[] tableNames = {
            "t_storage"
    };
    // 是否以预设表名模式运行，如果为 false 则以交互模式运行
    static boolean presetRunMode = true;

    static String[] noAutoIncrementIdTableNames = {
            ""
    };

    public static void main(String[] args) {
        if (presetRunMode) {
            preset();
        } else {
            interactive();
        }
        System.err.println("请注意没有生成 @TableId 的实体类，请自行添加（因为某些表不是自增表，需要手动添加 @TableId(value = \"id\", type = IdType.INPUT)）");
        System.err.println("没有自增 ID 的表有：" + Arrays.toString(noAutoIncrementIdTableNames));
    }

    /**
     * 预设表明
     */
    public static void preset() {
        generator(tableNames);
    }

    /**
     * 交互式生成
     */
    public static void interactive() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入想要生成的表名，多个表中间使用空格或英文逗号分割分割 ：\n");
        String input = scanner.nextLine();
        if (StrUtil.isBlank(input)) {
            System.err.println("没有输入任何表名");
        }
        // 为了安全，不提供所有表生成，必须手动预设
        String[] tableArrays = StringUtils.tokenizeToStringArray(input, ", ");
        List<String> tables = Arrays.asList(tableArrays);
        // 输出用户输入的字符串
        System.out.println("您输入的表名是：%s \n请输入 y 继续（其他任意字符串退出）:".formatted(tables));
        input = scanner.next();
        if ("y".equalsIgnoreCase(input)) {
            generator(tableArrays);
        }
    }

    private static void generator(String[] tables) {
        FastAutoGenerator.create(url,
                        username,
                        password)
                .globalConfig(builder -> {
                    builder.author("zhuqiang") // 设置作者
//                            .enableSwagger() // 开启 swagger 模式
                            .outputDir(getProjectDir("build/mybatis")); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent(parentPackage) // 设置父包名
                            //.moduleName("repo") // 设置父包模块名
                            .pathInfo(Collections.singletonMap(OutputFile.xml,
                                    getProjectDir("build/mybatis/mapper/"))); // 设置 mapperXml 生成路径
                })
                .strategyConfig(builder -> {
                    builder.entityBuilder().enableFileOverride();
                    builder.addInclude(tables) // 设置需要生成的表名
//                            .addTablePrefix("t_", "c_") // 设置过滤表前缀, 仅对这些表生效
                    ;
                })
                // .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
    }

    /**
     * 获取当前项目的跟路径，后面再加上 dir 的相对路径
     *
     * @param dir
     * @return
     */
    private static String getProjectDir(String dir) {
        return STR."\{System.getProperty("user.dir")}/\{dir}";
    }
}
