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
public class PostgresMyBatisConfig {
	@Bean(name = "postgresSqlSessionFactory")
	public SqlSessionFactory postgresSqlSessionFactory(@Qualifier("postgresDataSource") DataSource dataSource)
			throws Exception {
		// SqlSessionFactoryBean : MyBatis 설정을 도와주는 빌더 객체를 생성
		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		factory.setDataSource(dataSource);

		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

		factory.setMapperLocations(resolver.getResources("classpath*:/mapper/postgres/*.xml"));

		return factory.getObject();
	}

	@Bean(name = "postgresSessionTemplate")
	public SqlSessionTemplate postgresSessionTemplate(
			@Qualifier("postgresSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
}
