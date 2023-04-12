package com.bugspointer.controller;

import com.bugspointer.jwtConfig.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("app/admin")
@Slf4j
public class Admin {

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @GetMapping("dashboard")
    String getDashboard(HttpServletRequest request){
        return "admin/dashboard";
    }

    @GetMapping("addData")
    String getAddData(){
        return "admin/addData";
    }

    @GetMapping("compagniesList")
    String getCompagniesList(){
        return "admin/compagniesList";
    }
}
