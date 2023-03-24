package com.bugspointer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("app/admin")
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
