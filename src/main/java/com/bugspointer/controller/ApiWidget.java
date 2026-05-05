package com.bugspointer.controller;

import com.bugspointer.dto.WidgetConfigDTO;
import com.bugspointer.service.implementation.CompanyPreferencesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/widget")
public class ApiWidget {

    private final CompanyPreferencesService preferencesService;

    public ApiWidget(CompanyPreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    @GetMapping("config")
    WidgetConfigDTO getConfig(@RequestParam("public_key") String publicKey) {
        return preferencesService.getWidgetConfigDTO(publicKey);
    }
}
