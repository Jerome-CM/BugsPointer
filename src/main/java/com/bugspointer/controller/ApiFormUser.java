package com.bugspointer.controller;

import com.bugspointer.dto.ModalDTO;
import com.bugspointer.service.implementation.ModalService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;

@Controller
@RequestMapping("api/user")
public class ApiFormUser {

    private final ModalService modalService;

    public ApiFormUser(ModalService modalService) {
        this.modalService = modalService;
    }


    @PostMapping("/modalControl")
    RedirectView modal(@Valid ModalDTO dto, BindingResult result){
        if (!result.hasErrors()){
            modalService.saveModalFree(dto);
        }

        return new RedirectView(dto.getUrl());
    }

}
