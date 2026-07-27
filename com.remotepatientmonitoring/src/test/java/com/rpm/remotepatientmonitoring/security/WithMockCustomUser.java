package com.rpm.remotepatientmonitoring.security;

import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.model.Account;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUser.WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {
    String username() default "test@example.com";
    String role() default "PATIENT";

    class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
        @Override
        public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();

            Account account = Account.builder()
                    .id(1)
                    .email(customUser.username())
                    .passwordHash("password")
                    .role(customUser.role())
                    .isEmailVerified(true)
                    .isActive(true)
                    .build();

            CustomUserDetails principal = new CustomUserDetails(account);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());
            context.setAuthentication(auth);
            return context;
        }
    }
}
