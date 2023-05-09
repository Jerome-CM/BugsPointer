package com.bugspointer.controller;

import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.ModalDTO;
import com.bugspointer.dto.Response;
import com.bugspointer.service.implementation.ModalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;

@Controller
public class Modal {

    private final ModalService modalService;

    public Modal(ModalService modalService) {
        this.modalService = modalService;
    }

    @GetMapping("modal")
    String getModal(){
        return "download/modal";
    }

    @PostMapping("/modalControl")
    RedirectView modal(@Valid ModalDTO dto, BindingResult result){

        if (!result.hasErrors()){
            modalService.saveModalFree(dto);
        }

        return new RedirectView(dto.getUrl());
    }
}
