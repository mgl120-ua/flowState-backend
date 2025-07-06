package com.marta.flowstate.security;

import com.marta.flowstate.model.AppUser;
import com.marta.flowstate.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SessionUserService {

    private final AppUserRepository userRepo;

    public SessionUserService(AppUserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public AppUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepo.findByEmail(email);
    }

    public Long getCurrentCompanyId() {
        return getCurrentUser().getCompany().getId();
    }
}
