package main.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class Controller {
    @GetMapping("/sample")
    public String showForm() {
        return "sample";
    }
    @GetMapping("/")
    public String showHomePage() {
        return "sis";
    }

}
