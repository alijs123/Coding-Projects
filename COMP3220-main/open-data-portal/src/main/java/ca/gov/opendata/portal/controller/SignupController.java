package ca.gov.opendata.portal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ca.gov.opendata.portal.model.User;

/**
 * Controller class for handling user signup functionality.
 */
@Controller
public class SignupController {

    private final MongoTemplate mongoTemplate;

    /**
     * Constructor for SignupController.
     * @param mongoTemplate The MongoDB template for database operations.
     */
    @Autowired
    public SignupController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Display the signup form.
     * @return The name of the HTML file containing the signup form.
     */
    @GetMapping("/signup")
    public String showSignupForm() {
        return "signup";
    }

    /**
     * Process user signup.
     * @param username The username entered by the user.
     * @param email The email entered by the user.
     * @param password The password entered by the user.
     * @param redirectAttributes Redirect attributes to add flash attributes.
     * @return The redirect path based on signup success or failure.
     */
    @PostMapping("/signup")
    public String signUpUser(@RequestParam("username") String username,
                             @RequestParam("email") String email,
                             @RequestParam("password") String password,
                             RedirectAttributes redirectAttributes) {
        try {
            boolean userExists = mongoTemplate.exists(
                    org.springframework.data.mongodb.core.query.Query.query(
                            org.springframework.data.mongodb.core.query.Criteria.where("username").is(username)
                                    .orOperator(org.springframework.data.mongodb.core.query.Criteria.where("email").is(email))),
                    "User");

            if (userExists) {
                redirectAttributes.addFlashAttribute("error", "User with the same username or email already exists.");
                return "redirect:/signup";
            }

            User user = new User(username, email, password);
            mongoTemplate.save(user, "User");
            System.out.println("Account successfully created.");
            return "redirect:/web/data/list";
        } catch (Exception e) {
            System.out.println("Error occurred while registering user: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Signup failed: " + e.getMessage());
            return "redirect:/signup";
        }
    }
}





