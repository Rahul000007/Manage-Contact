package com.smart.controller;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.Random;

@Controller
public class HomeController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @RequestMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Home - Cloud Contact");
        return "home";
    }

    @RequestMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About - Cloud Contact");
        return "about";
    }

    @RequestMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("title", "Register - Cloud Contact");
        model.addAttribute("user", new User());
        return "signup";
    }

    // Handler for registering user
    @RequestMapping(value = "/do_register", method = RequestMethod.POST)
    public String registerUser(@ModelAttribute("user") User user, @RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
                               Model model, HttpSession session) {
        try {
            if (!agreement) {
                System.out.println("you have not agreed terms and conditions");
                throw new Exception("you have not agreed terms and conditions");
            } else {
                user.setRole("ROLE_USER");
                user.setEnabled(true);
                user.setImageUrl("default.png");
                user.setPassword(passwordEncoder.encode(user.getPassword()));

                System.out.println("Agreement " + agreement);
                session.setAttribute("newUser", user);

                return "redirect:/send-otp-new-user";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("user", user);
            session.setAttribute("message", new Message("Something went wrong " + e.getMessage(), "alert-danger"));
            return "signup";
        }
    }

    //  Sending OTP to new user
    @GetMapping("/send-otp-new-user")
    public String sendOTP(HttpSession session, Model model) {

        User user = (User) session.getAttribute("newUser");
        String email = user.getEmail();
        System.out.println(email);

//        generating 4 digit otp

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
            session.setAttribute("NewUserOtp", otp);
            session.setAttribute("email", email);
            model.addAttribute("title", "Enter OTP");
            return "verify_NewUser_otp";
        } else {
            model.addAttribute("user", user);
            session.setAttribute("message", new Message("Something went wrong Unable to send Otp check your Email Id", "alert-danger"));
            return "signup";
        }
    }

    //    verify New User OTP
    @PostMapping("/verify-otp-new-user")
    public String verifyOTP(@RequestParam("otp") Integer otp, HttpSession session, Model model) {

        Integer newUserOtp = (int) session.getAttribute("NewUserOtp");
        String email = (String) session.getAttribute("email");

        if (newUserOtp.equals(otp)) {

            User user1 = this.userRepository.getUserByUserName(email);

            if (user1 == null) {
                User user = (User) session.getAttribute("newUser");
                User result = this.userRepository.save(user);
                model.addAttribute("user", new User());
                session.setAttribute("message", new Message("Successfully  Registered !!", "alert-success"));
                model.addAttribute("title", "Register Here");
                return "signup";
            } else {
                model.addAttribute("user", new User());
                session.setAttribute("message", new Message("User Already Exists with this Email " + email, "alert-danger"));
                return "signup";
            }
        } else {
            session.setAttribute("message", "you have entered wrong otp !!");
            return "verify_NewUser_otp";
        }
    }

    // Handler for login
    @GetMapping("/signin")
    public String login(Model model) {
        model.addAttribute("title", "Login");
        return "login";
    }
}

