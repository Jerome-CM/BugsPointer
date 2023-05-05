package com.bugspointer.controller;

import com.bugspointer.dto.ModalDTO;
import com.bugspointer.service.implementation.ModalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

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
    String modal(@Valid ModalDTO dto){

        return "download/modal";
    }
}
