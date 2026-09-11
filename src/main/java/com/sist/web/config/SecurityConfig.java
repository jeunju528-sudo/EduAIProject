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
/*
 *     /member/login  => Login form
 *        |
 *     /member/login_process => 실제 구현 => submit 
 *     ---------------------               userid,userpwd
 *        |
 *     Security FilterChain
 *        |
 *      UsernamePasswordAuthenticationFilter = 인증 
 *        => .userParameter("userid")
 *        => .passwordParameter("userpwd")
 *        => 암호화된 비밀번호 => 복화화 인증 
 *           => match / encoder()
 *        |
 *      AuthenticationManager
 *        |
 *      AuthenticationProvider 
 *        |
 *      JdbcUserDetailsManager 
 *        | DB 조회 => member / authority
 *                    ------------------ ROLE_INSTRUCTOR
 *                    ------------------ ROLE_USER
 *                    ------------------ ROLE-ADMIN
 *      저장 : 사용자 정보 저장 
 *        |
 *      UserDetails
 *        |
 *      BCryptPasswordEncoder 
 *        |
 *     --------------------
 *     |                  |
 *   Success             Failure
 *                        |
 *                       Login Form으로 이동 => 실패한 원인
 *      |
 *    SecurityContext : 저장  
 *      |
 *    Session에저장 
 *      |
 *    사용자 인증 완료 
 *      |
 *    @Controller 
 *      
 */
@Configuration
@EnableWebSecurity
//@RequiredArgsConstructor
public class SecurityConfig {
  private final LoginSuccessHandler loginSuccessHandler;
  private final LoginFailureHandler loginFailureHandler;
  private final DataSource dataSource;
  
  // @Qualifier : 객체 선택 
  public SecurityConfig(
		  LoginSuccessHandler loginSuccessHandler,
		  LoginFailureHandler loginFailureHandler,
		  @Qualifier("oracleDataSource") DataSource dataSource 
  )
  {
	  this.dataSource=dataSource;
	  this.loginFailureHandler=loginFailureHandler;
	  this.loginSuccessHandler=loginSuccessHandler;
  }
  
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http)
  {
	  /*
	   *   CSRF
	   *     => Cross site Request Forgery
	   *     => 인증된 브라우저 저장된 쿠키 / 세션정보를 활용
	   *        다른 사이트로 요청 정보 전달 : 위조 , 파밍
	   *     => 일반 보안 방지 : csrf.disable()
	   */
	  http 
	    .csrf(csrf -> csrf.disable()) 
	    // 접근 권한 설정 (URL)
	    .authorizeHttpRequests(auth-> auth
	       .requestMatchers("/","/member/**").permitAll()
	       // 로그인 없이 가능 
	       .requestMatchers("/admin/**").hasAnyRole("ADMIN","INSTRUCTOR")
	       .anyRequest().permitAll()
	    )
	    // 로그인 설정  403
	    .formLogin(form -> form 
	       .loginPage("/edu/login")
	       .loginProcessingUrl("/member/login_process")
	       .usernameParameter("username")
	       .passwordParameter("password")
	       .defaultSuccessUrl("/",false)
	       .successHandler(loginSuccessHandler)
	       .failureHandler(loginFailureHandler)
	       .permitAll()
	    )
	    // 로그아웃 설정 
	    .logout(logout-> logout
	       .logoutUrl("/member/logout")	
	       .logoutSuccessUrl("/")
	       .invalidateHttpSession(true)
	       .deleteCookies("remember-me","JSESSIONID")
	     )
	    // 자동 로그인 
	    .rememberMe(remember-> remember
	       .key("my-secret-key")
	       .rememberMeParameter("remember-me")
	       .tokenValiditySeconds(60*60*24)
	       .tokenRepository(persistentTokenRepository())
	     );
	  return http.build();
  }
  // 인증 관리자 
  @Bean
  public AuthenticationManager authenticationManager(
     HttpSecurity http,
     BCryptPasswordEncoder passwordEncoder
  ) throws Exception
  {
	  AuthenticationManagerBuilder builder
	       =http.getSharedObject(AuthenticationManagerBuilder.class);
	  builder
	   .userDetailsService(jdbcUserDetailsManager())
	   .passwordEncoder(passwordEncoder);
	   
	  return builder.build();
  }
  // 데이터베이스에서 값을 읽어 온다 
  @Bean
  public JdbcUserDetailsManager jdbcUserDetailsManager() 
  {
	 JdbcUserDetailsManager manager=
			 new JdbcUserDetailsManager(dataSource);
	 manager.setUsersByUsernameQuery(
	   "SELECT username,password,enabled "
	   +"FROM member WHERE username=?"
	 );
	 manager.setAuthoritiesByUsernameQuery(
	   "SELECT username,a.member_id,authority "
	  +"FROM authroity a JOIN member m "
	  +"ON a.member_id=m.member_id "
	  +"AND username=?"
	 );
	 return manager;
  }
  // 비밀번호 암호화 
  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
	  return new BCryptPasswordEncoder();
  }
  // 자동 로그인 
  @Bean
  public PersistentTokenRepository persistentTokenRepository() {
	  JdbcTokenRepositoryImpl repo=
			  new JdbcTokenRepositoryImpl();
	  repo.setDataSource(dataSource);
	  return repo;
  }
  /*
   *    Spring Security 라이브러리 
   *    1) @EnableWebSecurity : => AOP로 동작 
   *           => AOP : Security , Transaction
   *           => 공통모듈 
   *           Spring Security 활성화 => 사용하기 위해 설정 
   *       user === Spring Security ==== DispatcherServlet
   *    2) SecurityConfig : 보안 전체 설정 담당 => 사용자 정의 
   *    3) HttpSecurity : 접근권한 / 로그인 / 로그아웃 / 자동로그인
   *                                               => rememberMe
   *                                                  => 기간 설정 (쿠키)
   *                                                  => token 생성 
   *                                                     ----- 쿠키명 
   *                                                  => 유지 기간 설정 
   *                                      => logout
   *                                         => invalidate() : 전체 세션 내용 삭제 
   *                                         => cookie삭제 : 자동로그인 설정 
   *                                         => 화면 이동 설정 
   *                               => formLogin
   *                                  => 사용자가 보낸준 id,password
   *                                  => success/fail
   *                                  => 화면 이동 지정 
   *                      CSRF 설정 구성
   *                      authorizeHttpRequests
   *                        .requestMatchers(사이트 URI)
   *                         => hasRole() => 한개만 설정 
   *                         => hasAnyRole() => 여러개 설정 
   *                         => permitAll() => 모든 
   *                        .anyRequest() => 기타
   *    4) SecurityFilterChain : HTTP요청에 대한 
   *                             Spring Security 필터 처리 순서를 정의
   *    5) AuthenticationManager : 사용자의 인증을 수행을 총괄 
   *    6) AuthenticationProvider : 사용자의 인증을 실제 수행하는 객체 
   *    7) UserDetailsService : 로그인한 사용의 정보를 조회 
   *    8) JdbcUserDetailsManager : DB에서 사용자 / 권한 조회해서 저장하는 역할 
   *                                => SQL
   *    9) UserDetails : 한명의 사용자 정보가 저장된 객체 
   *    10) BCryptPasswordEncoder : 비밀번호를 암호화 
   *    11) JdbcTokenRepositoryImpl : 자동 로그인 => 
   *                     사용자를 구분하기 위한 토큰을 저장을 역할 
   *    12) SecurityContext :  인증 정보를 저장하는 보관소
   *    13) rememberMe : 자동 로그인 처리 
   *    Filer 종류 
   *          - Manager=>UserDetailsService 
   *          - PasswordEncoder
   *          - Authentication 
   *          - SecurityContext
   *          
   *    DB 처리 
   *       회원 정보 / 권한 => 두개의 테이블 생성 
   *         | 로그인된 사용자 정보
   *       Authentication 
   *         | Authentication된 정보 저장 
   *       SecurityContext 
   *         |
   *       Session 
   *    -------------------------------
   *    인증 : AuthenticationManager
   *    성공 : Authentication -> SecurityContext 
   *    유지 : Session -> Principal
   *                        
   */
}