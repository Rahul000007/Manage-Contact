package com.smart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Random;

@Controller
public class ForgotController {

    @RequestMapping("/forgot")
    public String openEmailForm() {
        return "forgot_email_form";
    }

    // OTP generation and sending to mail
    @PostMapping("/send-otp")
    public String sendOTP(@RequestParam("email") String email) {
        System.out.println("EMAIL " + email);
//        generating 4 digit otp
        Random random = new Random();
        int otp = random.nextInt(999999);
        System.out.println("OTP " + otp);

//        sending otp to mail

        return "verify_otp";
    }

}
