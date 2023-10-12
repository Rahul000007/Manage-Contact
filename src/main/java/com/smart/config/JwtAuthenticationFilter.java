//package com.smart.config;
//
//import com.smart.helper.JwtUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//    @Autowired
//    private UserDetailsServiceImpl userDetailsServiceImpl;
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//
//        final String authorizationHeader = request.getHeader("Authorization");
//        String username = null;
//        String jwt = null;
//        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//
//            jwt = authorizationHeader.substring(7);
//
//            try {
//                username = this.jwtUtil.extractUsername(jwt);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            UserDetails userDetails = this.userDetailsServiceImpl.loadUserByUsername(username);
//
//            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//                if(this.jwtUtil.validateToken(jwt,userDetails)) {
//                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
//                }else {
//                    System.out.println("token not valid");
//                }
//            } else {
//                System.out.println("Token is not valid");
//            }
//        }
//        filterChain.doFilter(request, response);
//    }
//}
