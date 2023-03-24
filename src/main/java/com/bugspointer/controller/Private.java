package com.bugspointer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("app/private/")
public class Private {

    @GetMapping("invoices")
    String getInvoices(){
        return "private/invoices";
    }

    @GetMapping("notifications")
    String getNotifications(){
        return "private/notifications";
    }

    @GetMapping("account")
    String getAccount(){
        return "private/account";
    }

    @GetMapping("dashboard")
    String getFeatures(){
        return "private/dashboard";
    }

    @GetMapping("newBugList")
    String getNewBugList(){
        return "private/newBugList";
    }

    @GetMapping("newBugReport")
    String getNewBugReport(){
        return "private/newBugReport";
    }

    @GetMapping("pendingBugList")
    String getPendingBugList(){
        return "private/pendingBugList";
    }

    @GetMapping("pendingBugReport")
    String getPendingBugReport(){
        return "private/pendingBugReport";
    }

    @GetMapping("solvedBugList")
    String getSolvedBugReport(){
        return "private/solvedBugList";
    }

}
