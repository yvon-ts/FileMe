package net.fileme.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CustomController {
    @GetMapping("/access-denied")
    public ModelAndView accessDenied(HttpServletRequest request){
        ModelAndView view = new ModelAndView("error");
        view.addObject("errMsg", request.getAttribute("errMsg"));
        return view;
    }
    @PostMapping("/access-denied")
    public ModelAndView accessDeniedPost(HttpServletRequest request){
        ModelAndView view = new ModelAndView("error");
        view.addObject("errMsg", request.getAttribute("errMsg"));
        return view;
    }
}
