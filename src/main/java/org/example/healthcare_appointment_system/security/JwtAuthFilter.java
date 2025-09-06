package org.example.healthcare_appointment_system.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    //OncePerRequestFilter: ensures the filter runs only once per request
    // (avoiding duplicate executions in the same request lifecycle)

    private final JwtService jwtService;

    //loads user details (roles, authorities, password hash) from DB or another source.
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        //Step 1: Get the Authorization header
        //Looks for Authorization: Bearer <token> in the request header.
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        //Check if it’s a JWT Bearer token
        if (header != null && header.startsWith("Bearer ")) {
            final String token = header.substring(7);

            //Validate the token
            if (jwtService.isValid(token)) {
                //Extract username (from JWT payload)
                String username = jwtService.extractUsername(token);

                //Check if authentication is not already set
                //Prevents re-authentication if the user is already authenticated in this request context.
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    //Load user details from DB.
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    //Create an authentication token
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            //null (since we’re using JWT, not password login)
                            null,
                            //roles/permissions
                            userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    //Store it in the security context.This makes Spring Security treat the request as authenticated.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        //Passes the request on to the next filter (or to the controller).
        chain.doFilter(request, response);
    }
}