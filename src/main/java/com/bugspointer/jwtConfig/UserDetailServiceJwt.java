package com.bugspointer.jwtConfig;

import com.bugspointer.entity.Company;
import com.bugspointer.repository.CompanyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.management.relation.Role;
import java.util.*;

import static org.apache.catalina.realm.UserDatabaseRealm.getRoles;

@Service
@Slf4j
public class UserDetailServiceJwt implements UserDetailsService {

    @Autowired
    //private UserRepository userRepository;
    private CompanyRepository companyRepository;

    /**
     * Load a user present in DB in UserDetails
     * @param mail :
     * @return User Spring
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {

        Company company = companyRepository.findByMail(mail);

        if (company == null) {
            throw new UsernameNotFoundException(mail);
        }
        /* load username, password and Authorities in a User Spring */
        UserDetails userAuth = org.springframework.security.core.userdetails.User.withUsername(company.getMail()).password(company.getPassword()).authorities(company.getRole().toString()).build();
        //log.info("Connexion User : {}", userAuth);
        return userAuth;
    }

    public List<String> getRoleList(UserDetails userDetails) {
        List<String> roles = new ArrayList();
        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            roles.add(authority.getAuthority());
        }
        log.info("Roles : " + roles);
        return roles;
    }

}
