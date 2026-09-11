package com.sist.web.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
public class OracleMyBatisConfig {
	@Bean(name = "oracleSqlSessionFactory")
	// 주입받을 DataSource가 여러 개 있을 때, 그중 이름이 oracleDataSource인 데이터소스(ORACLESQL 연결 정보)를 지정해서 가져옴
	public SqlSessionFactory oracleSqlSessionFactory(@Qualifier("oracleDataSource") DataSource dataSource)
			throws Exception {
		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		factory.setDataSource(dataSource);
		
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		
		factory.setMapperLocations(resolver.getResources("classpath*:/mapper/oracle/*.xml"));

		return factory.getObject();
	}
	
	@Bean(name = "oracleSessionTemplate")
	public SqlSessionTemplate oracleSessionTemplate(@Qualifier("oracleSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
}
