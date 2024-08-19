package edu.hit.fmpmm.controller;

import edu.hit.fmpmm.dto.Result;
import edu.hit.fmpmm.dto.UserDTO;
import edu.hit.fmpmm.service.web.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
//@CrossOrigin(origins = "http://10.245.1.6:5173/")
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result login(@RequestBody UserDTO user, HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        return userService.login(user, session, request, response);
    }

    @PostMapping("/register")
    public Result register(@RequestBody UserDTO user) {
        return userService.register(user);
    }

    @PostMapping("logout")
    public Result logout(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        return userService.logout(session, request, response);
    }

    @PutMapping("/update")
    public Result update(@RequestBody UserDTO user) {
        return userService.updateUserById(user);
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id, HttpSession session, HttpServletResponse response) {
        return userService.deleteUser(id, session, response);
    }
}
