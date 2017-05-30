package com.noteanalyzer.security.security.endpoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.noteanalyzer.mvc.model.UserModel;
import com.noteanalyzer.mvc.service.impl.UserServiceImpl;
import com.noteanalyzer.security.security.auth.jwt.extractor.TokenExtractor;
import com.noteanalyzer.security.security.auth.jwt.verifier.TokenVerifier;
import com.noteanalyzer.security.security.config.JwtSettings;
import com.noteanalyzer.security.security.config.WebSecurityConfig;
import com.noteanalyzer.security.security.exceptions.InvalidJwtToken;
import com.noteanalyzer.security.security.model.UserContext;
import com.noteanalyzer.security.security.model.token.JwtToken;
import com.noteanalyzer.security.security.model.token.JwtTokenFactory;
import com.noteanalyzer.security.security.model.token.RawAccessJwtToken;
import com.noteanalyzer.security.security.model.token.RefreshToken;

/**
 * RefreshTokenEndpoint
 * 
 */
@RestController
public class RefreshTokenEndpoint {
	@Autowired
	private JwtTokenFactory tokenFactory;
	@Autowired
	private JwtSettings jwtSettings;
	@Autowired
	private UserServiceImpl userService;
	@Autowired
	private TokenVerifier tokenVerifier;
	@Autowired
	@Qualifier("jwtHeaderTokenExtractor")
	private TokenExtractor tokenExtractor;

	@RequestMapping(value = "/api/auth/token", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody JwtToken refreshToken(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String tokenPayload = tokenExtractor.extract(request.getHeader(WebSecurityConfig.JWT_TOKEN_HEADER_PARAM));

		RawAccessJwtToken rawToken = new RawAccessJwtToken(tokenPayload);
		RefreshToken refreshToken = RefreshToken.create(rawToken, jwtSettings.getTokenSigningKey())
				.orElseThrow(() -> new InvalidJwtToken());

		String jti = refreshToken.getJti();
		if (!tokenVerifier.verify(jti)) {
			throw new InvalidJwtToken();
		}

		String subject = refreshToken.getSubject();
		UserModel user = userService.getByUsername(subject)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + subject));
		/*
		 * if (user.getRoles() == null) throw new
		 * InsufficientAuthenticationException("User has no roles assigned");
		 * List<GrantedAuthority> authorities = user.getRoles().stream()
		 * .map(authority -> new
		 * SimpleGrantedAuthority(authority.getRole().authority()))
		 * .collect(Collectors.toList());
		 */
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ADMIN"));

		UserContext userContext = UserContext.create(user.getEmail(), authorities);

		return tokenFactory.createAccessJwtToken(userContext);
	}
}
