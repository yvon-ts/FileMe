package net.fileme.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CustomController {
    @GetMapping("/access-denied")
    public ModelAndView accessDenied(){
        ModelAndView view = new ModelAndView("error");
        return view;
    }
    @PostMapping("/access-denied")
    public ModelAndView accessDeniedPost(){
        ModelAndView view = new ModelAndView("error");
        return view;
    }
}
