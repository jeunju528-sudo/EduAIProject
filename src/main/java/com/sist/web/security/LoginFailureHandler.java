package com.sist.web.security;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler{

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		String errMsg = "";
		try {
			if(exception instanceof BadCredentialsException) {
				errMsg = "아이디나 비밀번호가 없습니다.";
			}
			else if(exception instanceof DisabledException) {
				errMsg = "휴면 계정입니다.";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		request.setAttribute("msg", errMsg);
		// request를 유지하기 위해서 forword 사용
		// sendredirect는 완전히 이동되는거라서 request 초기화 됨
		request.getRequestDispatcher("/member/login").forward(request, response);
	}

}
