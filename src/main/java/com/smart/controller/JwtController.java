//package com.smart.controller;
//
//
//import com.smart.config.UserDetailsServiceImpl;
//import com.smart.helper.JwtUtil;
//import com.smart.model.JwtRequest;
//import com.smart.model.JwtResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class JwtController {
//    @Autowired
//    private AuthenticationManager authenticationManager;
//    @Autowired
//    private UserDetailsServiceImpl userDetailsServiceImpl;
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    @PostMapping("/token")
//    public ResponseEntity<?> generateToken(@RequestBody JwtRequest jwtRequest) throws Exception {
//
//        System.out.println(jwtRequest);
//
//        try {
//            this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(jwtRequest.getUsername(), jwtRequest.getPassword()));
//        } catch (UsernameNotFoundException e) {
//            throw new Exception("User Not Found" + e.getMessage());
//        } catch (BadCredentialsException e) {
//            e.printStackTrace();
//            throw new Exception("Bang Bang Bad Credential : " +e.getMessage());
//        }
//
//        UserDetails userDetails = this.userDetailsServiceImpl.loadUserByUsername(jwtRequest.getUsername());
//        String token = this.jwtUtil.generateToken(userDetails);
//
//        System.out.printf("JWT TOKEN: " + token);
//        return ResponseEntity.ok(new JwtResponse(token));
//    }
//
//}
