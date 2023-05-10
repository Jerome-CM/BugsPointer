package com.bugspointer.service;

import com.bugspointer.dto.AccountDTO;
import com.bugspointer.dto.AuthRegisterCompanyDTO;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Company;

import javax.servlet.http.HttpServletRequest;

public interface ICompany {

    public Company getCompanyByMail(String mail);
    public String createPublicKey();
    public Response saveCompany(AuthRegisterCompanyDTO dto);
    public Company getCompanyWithToken(HttpServletRequest request);
    public AccountDTO getAccountDto(Company company);
    public Response mailUpdate(AccountDTO dto);
}
