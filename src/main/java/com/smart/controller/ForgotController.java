package com.smart.controller;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;
import jdk.swing.interop.SwingInterOpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Random;

@Controller
public class ForgotController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @RequestMapping("/forgot")
    public String openEmailForm() {
        return "forgot_email_form";
    }

    // OTP generation and sending to mail
    @PostMapping("/send-otp")
    public String sendOTP(@RequestParam("email") String email, HttpSession session) {
        System.out.println("EMAIL " + email);
//       generating 4 digit otp
        Random random = new Random();
        int otp = random.nextInt(999999);
        System.out.println("OTP: " + otp);

//       code for sending otp to mail
        String subject = "OTP from Cloud Contact";
        String message = ""
                + "<div style='border:1px solid #e2e2e2; padding:20px'>"
                + "<h1>"
                + "OTP is "
                + "<b>" + otp
                + "</n>"
                + "<h1>"
                + "</div>";
        String to = email;

        boolean flag = this.emailService.sendEmail(subject, message, to);

        if (flag) {
            session.setAttribute("myotp", otp);
            session.setAttribute("email", email);
            return "verify_otp";
        } else {
            session.setAttribute("message", "Check your Email Id");
            return "forgot_email_form";
        }
    }

    //    verify otp
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp") Integer otp, HttpSession session) {

        Integer myOtp = (int) session.getAttribute("myotp");
        String email = (String) session.getAttribute("email");

        if (myOtp.equals(otp)) {

            User user = this.userRepository.getUserByUserName(email);

            if (user == null) {
//send error page
                session.setAttribute("message", "User does not exists with this email check your email");
                return "forgot_email_form";
            } else {
//                send change password form
            return "password_change_form";
            }
        } else {
            session.setAttribute("message", "you have entered wrong otp !!");
            return "verify_otp";
        }
    }

//    change password
    @PostMapping("/change-password")
    public  String changePassword(@RequestParam("newpassword") String newPassword,HttpSession session){

        String email = (String) session.getAttribute("email");
        User user = this.userRepository.getUserByUserName(email);
        user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
        this.userRepository.save(user);
        return "redirect:/signin?change=password changed successfully";
    }
}
