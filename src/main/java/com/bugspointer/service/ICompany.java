package com.bugspointer.service;

import com.bugspointer.dto.AuthRegisterCompanyDTO;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Company;

public interface ICompany {

    public Company getCompanyByMail(String mail);
    public String createPublicKey();
    public Response saveCompany(AuthRegisterCompanyDTO dto);
}
