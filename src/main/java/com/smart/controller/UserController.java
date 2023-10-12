package com.smart.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Value("${razorpay.key.id}")
    private String razorPayId;

    @Value("${rezorpay.key.secret}")
    private String razorPaySecret;

    //    Method for adding common to response
    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {
        String userName = principal.getName();
        User user = userRepository.getUserByUserName(userName);
        model.addAttribute("user", user);
    }

    //  dashboard home
    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal) {

        model.addAttribute("title", "Home");
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

    // delete contact handler
    @GetMapping("/delete/{cid}")
    @Transactional
    public String deleteContact(@PathVariable("cid") Integer cId, Model model, HttpSession session, Principal principal) {

        Contact contact = this.contactRepository.findById(cId).get();
        User user = this.userRepository.getUserByUserName(principal.getName());
        user.getContacts().remove(contact);

        session.setAttribute("message", new Message("Contact Deleted successfully..", "success"));
        return "redirect:/user/show-contacts/0";
    }

    //    Opening Update form handler
    @PostMapping("/update-contact/{cid}")
    public String updateForm(@PathVariable("cid") Integer cId, Model model) {

        model.addAttribute("title", "Update contact");

        Contact contact = this.contactRepository.findById(cId).get();
        model.addAttribute(contact);

        return "normal/update_form";
    }

    //    Processing Update Handler
    @PostMapping("/process-update")
    public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
                                Model model, Principal principal, HttpSession session) {

        try {
//            old contact detail
            Contact oldContact = this.contactRepository.findById(contact.getcId()).get();

            if (!file.isEmpty()) {
//                file work
//                rewrite
//                delete old photo
                File deleteFile = new ClassPathResource("static/img").getFile();
                File file1 = new File(deleteFile, oldContact.getImage());
                file1.delete();
//                update new photo
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                contact.setImage(file.getOriginalFilename());

            } else {
                contact.setImage(oldContact.getImage());
            }

            User user = this.userRepository.getUserByUserName(principal.getName());
            contact.setUser(user);
            this.contactRepository.save(contact);
            session.setAttribute("message", new Message("your contact updated", "success"));

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", new Message("Failed to update contact", "alert"));
        }

        return "redirect:/user/" + contact.getcId() + "/contact";
    }

    //    Profile handler
    @GetMapping("/profile")
    public String yourProfile(Model model) {
        model.addAttribute("title", "Profile Page");
        return "normal/profile";
    }

    //    opening setting handler
    @GetMapping("/settings")
    public String openSetting(Model model) {
        model.addAttribute("title", "Settings");
        return "normal/settings";
    }

    //    processing settings handler
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {

        String userName = principal.getName();
        User currentUser = this.userRepository.getUserByUserName(userName);

        if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {

            //changing the password
            currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
            this.userRepository.save(currentUser);
            session.setAttribute("message", new Message("Your Password is Changed successfully..", "success"));

        } else {

            session.setAttribute("message", new Message("Please Enter correct Old Password", "danger"));
            return "redirect:/user/settings";

        }
        return "redirect:/user/index";
    }

    @PostMapping("/create_payment")
    @ResponseBody
    public String createPayment(@RequestBody Map<String, Object> data) throws RazorpayException {
        int amount = Integer.parseInt(data.get("amount").toString());
        JSONObject orderRequest= new JSONObject();
        orderRequest.put("amount",amount*100);
        orderRequest.put("currency","INR");
        orderRequest.put("receipt","order_1234");
        RazorpayClient client= new RazorpayClient(razorPayId,razorPaySecret);
        Order order=client.orders.create(orderRequest);
        return order.toString();
    }
}
