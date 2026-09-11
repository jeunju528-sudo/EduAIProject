package com.sist.web.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "com.sist.web.mapper.oracle", sqlSessionFactoryRef = "oracleSqlSessionFactory")
public class OracleMapperScanConfig {

}
