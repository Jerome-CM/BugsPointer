package com.bugspointer.controller;

import com.bugspointer.jwtConfig.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("app/admin")
@Slf4j
public class Admin {

    @GetMapping("dashboard")
    String getDashboard(){
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
