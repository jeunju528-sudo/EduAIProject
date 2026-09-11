package com.sist.web.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {
	@Bean(name = "oracleDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.oracle") // application.yml 파일에서 지정한 순차
	public DataSource oracleDataSource() {
		return DataSourceBuilder.create().build();
	}
	
	@Bean(name = "postgresDataSource")
	// @ConfigurationProperties: application.yml에 정의된 외부 설정값들을 자바 객체의 필드에 대량으로 바인딩해주는 기능
	@ConfigurationProperties(prefix = "spring.datasource.postgres")
	public DataSource postgresDataSource() {
		
		/*
		 * DataSourceBuilder.create().build() : 기본적으로 아무 설정도 없는 빈 DataSource 객체 생성
		 * 그 후 스프링 후처리가 구동되면서 spring.datasource.oracle.url, spring.datasource.oracle.username 등의 키값들을 찾아 자동으로 해당 객체의 setUrl(), setUsername()을 호출해 바인딩
		 * */
		return DataSourceBuilder.create().build();
	}
}
