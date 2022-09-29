package com.smart.controller;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import javax.swing.text.html.Option;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    //    Method for adding common to response
    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {
        String userName = principal.getName();
        System.out.println("USERNAME " + userName);
        User user = userRepository.getUserByUserName(userName);
        System.out.println("USER " + user);
        model.addAttribute("user", user);
    }

    //  dashboard home
    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal) {

        model.addAttribute("title", "User Dashboard");
        return "normal/user_dashboard";
    }

    // open add form handler
    @GetMapping("/add-contact")
    public String openAddContactForm(Model model) {

        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());

        return "normal/add_contact_form";
    }

    //    processing  add contact form
    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact,
                                 @RequestParam("profileImage") MultipartFile file,
                                 Principal principal, HttpSession session) {
        try {
            String name = principal.getName();
            User user = userRepository.getUserByUserName(name);
//         processing and uploading file
            if (file.isEmpty()) {
                System.out.println("File is empty");
                contact.setImage("contact.png");
            } else {
                contact.setImage(file.getOriginalFilename());
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Image is uploaded");
            }
            contact.setUser(user);
            user.getContacts().add(contact);
            this.userRepository.save(user);
            session.setAttribute("message", new Message("Your Contact is added !! Add more..", "success"));
        } catch (Exception e) {
            System.out.println("ERROR");
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong !! Try again", "danger"));
        }
        return "normal/add_contact_form";
    }

    // show contact handler
//    {page} for pagination
    @GetMapping("/show-contacts/{page}")
    public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
        model.addAttribute("title", "Show User Contacts");

        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);
//        contactPage page
//        contact per page
        Pageable pageable = PageRequest.of(page, 5);
        Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(), pageable);
        model.addAttribute("contacts", contacts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contacts.getTotalPages());

        return "normal/show_contacts";
    }

    //  showing particular contact detail
    @RequestMapping("/{cId}/contact")
    public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
        System.out.println(cId);
        Optional<Contact> contactOption = this.contactRepository.findById(cId);
        Contact contact = contactOption.get();
        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);
        if (user.getId() == contact.getUser().getId()) {
            model.addAttribute("contact", contact);
            model.addAttribute("title", contact.getName());
        }
        return "normal/contact_detail";
    }


}
