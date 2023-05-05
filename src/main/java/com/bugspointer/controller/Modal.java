package com.bugspointer.controller;

import com.bugspointer.dto.ModalDTO;
import com.bugspointer.service.implementation.ModalService;
import org.springframework.stereotype.Controller;
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
        String getModal(ModalDTO dtoSave){
        return "download/modal";
    }

    @PostMapping("/modal")
    String modal(@Valid ModalDTO dto){

        return "download/modal";
    }
}
