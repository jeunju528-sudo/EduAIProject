package com.sist.web.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import com.sist.web.security.LoginFailureHandler;
import com.sist.web.security.LoginSuccessHandler;

@Configuration
@EnableWebSecurity
// @RequiredArgsConstructor
public class SecurityConfig {
	
	private final LoginSuccessHandler loginSuccessHandler;
	private final LoginFailureHandler loginFailureHandler;
	private final DataSource dataSource;
	
	public SecurityConfig(LoginSuccessHandler loginSuccessHandler, LoginFailureHandler loginFailureHandler, @Qualifier("oracleDataSource") DataSource dataSource) {
		this.loginSuccessHandler = loginSuccessHandler;
		this.loginFailureHandler = loginFailureHandler;
		this.dataSource = dataSource;
	}
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) {
		return http
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(auth -> auth.requestMatchers("/","/member/**").permitAll()
										.requestMatchers("/admin/**").hasAnyRole("ADMIN","INSTRUCTOR")
										.anyRequest().permitAll())
			.formLogin(form -> form.loginPage("/edu/login")
								.loginProcessingUrl("/member/login_process")
								.usernameParameter("username")
								.passwordParameter("password")
								.defaultSuccessUrl("/", false)
								.successHandler(loginSuccessHandler)
								.failureHandler(loginFailureHandler))
			.logout(logout -> logout.logoutUrl("/member/logout")
								.logoutSuccessUrl("/")
								.invalidateHttpSession(true)
								.deleteCookies("remember-me","JSESSIONID"))
			// 자동 로그인
			.rememberMe(remember -> remember.key("my-secrey-key")
										.rememberMeParameter("remember-me")
										.tokenValiditySeconds(60*60*24)
										.tokenRepository(persistentTokenRepository())
						)
			.build();
	}
	
	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http, BCryptPasswordEncoder passwordEncoder) throws Exception{
		AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
		builder.userDetailsService(null)
				.passwordEncoder(passwordEncoder);
		
		return builder.build();
	}
	
	@Bean
	public JdbcUserDetailsManager jdbcUserDetailsManager() {
		JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
		manager.setUsersByUsernameQuery(
				"SELECT username, password, enabled "
				+ "FROM member "
				+ "WHERE username = ?");
		manager.setAuthoritiesByUsernameQuery(
				"SELECT m.username, a.member_id, a.authority "
				+ "FROM authority a JOIN member m "
				+ "ON a.member_id = m.member_id "
				+ "WHERE m.username = ?"
				);
		return manager;
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	// 자동 로그인 시 사용하는 토큰 저장소
	@Bean
	public PersistentTokenRepository persistentTokenRepository() {
		JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
		repo.setDataSource(dataSource);
		return repo;
	}
	
}
