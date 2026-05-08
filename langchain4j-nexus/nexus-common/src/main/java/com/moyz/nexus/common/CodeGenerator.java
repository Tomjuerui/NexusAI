package com.moyz.nexus.common;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.sql.Types;
import java.util.Collections;

public class CodeGenerator {
    public static void main(String[] args) {
        FastAutoGenerator.create(
                "jdbc:postgres://172.17.30.40:5432/Nexus?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&tinyInt1isBit=false&allowMultiQueries=true",
                "postgres", "postgres")
                .globalConfig(builder -> {
                    builder.author("moyz") // 设置作�?
                            .enableSwagger() // 开�?swagger 模式
                            .fileOverride() // 覆盖已生成文�?
                            .outputDir("D://"); // 指定输出目录
                })
                .dataSourceConfig(builder -> builder.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                    int typeCode = metaInfo.getJdbcType().TYPE_CODE;
                    if (typeCode == Types.SMALLINT) {
                        // 自定义类型转�?
                        return DbColumnType.INTEGER;
                    }
                    return typeRegistry.getColumnType(metaInfo);

                }))
                .packageConfig(builder -> {
                    builder.mapper("com.nexus.common.mapper")
                            .parent("")
                            .moduleName("")
                            .entity("po")
                            .serviceImpl("service.impl")
                            .pathInfo(Collections.singletonMap(OutputFile.xml, "D://mybatisplus-generatorcode")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude("nexus_knowledge_base_qa_record") // 设置需要生成的表名
                            .addTablePrefix("nexus_");
                    builder.mapperBuilder().enableBaseResultMap().enableMapperAnnotation().build();
                })
                .execute();
    }
}
