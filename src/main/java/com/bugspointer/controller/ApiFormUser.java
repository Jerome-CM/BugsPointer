package com.bugspointer.controller;

import com.bugspointer.dto.ModalDTO;
import com.bugspointer.service.implementation.ModalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
@Slf4j
@RequestMapping("api/user")
public class ApiFormUser {

    private final ModalService modalService;

    public ApiFormUser(ModalService modalService) {
        this.modalService = modalService;
    }


    @PostMapping("/modalControl")
    RedirectView modal(@Valid ModalDTO dto, BindingResult result, HttpServletRequest request){
        if (!result.hasErrors()){
            String adresseIp = request.getRemoteAddr();
            log.info("adresseIp : {}", adresseIp);
            dto.setAdresseIp(adresseIp);
            modalService.saveModal(dto);
        }

        return new RedirectView(dto.getUrl());
    }

}
