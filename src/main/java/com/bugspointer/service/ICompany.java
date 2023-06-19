package com.bugspointer.service;

import com.bugspointer.dto.*;
import com.bugspointer.entity.Company;

import javax.servlet.http.HttpServletRequest;

public interface ICompany {

    Company getCompanyByMail(String mail);
    Company getCompanyByPublicKey(String publicKey);
    String createPublicKey();
    Response saveCompany(AuthRegisterCompanyDTO dto);
    Company getCompanyWithToken(HttpServletRequest request);
    AccountDTO getAccountDto(Company company);
    AccountDeleteDTO getAccountDeleteDto(Company company);
    DashboardDTO getDashboardDto(Company company);
    Response mailUpdate(AccountDTO dto);
    Response passwordUpdate(AccountDTO dto);
    Response smsUpdate(AccountDTO dto);
    Company companyTryUpdateLastVisit(Company company);
    Response delete(AccountDeleteDTO dto);
    Response validateRegister(String publicKey);
    Response registerDomaine(AccountDTO dto);
    Response sendPwLost(AccountDTO dto);
    Response resetPassword(String publicKey, AccountDTO dto);
}
