package com.monthlyib.server.auth.util;

import com.monthlyib.server.constant.Authority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomAuthorityUtils {

    /* 권한 부여 메소드 */
    public List<GrantedAuthority> createAuthorities(List<String> roles) {
        List<GrantedAuthority> result =  roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_"+role))
                .collect(Collectors.toList());
        return result;
    }

    public static List<GrantedAuthority> getAuthorities(List<String> roles) {
        List<GrantedAuthority> result =  roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_"+role))
                .collect(Collectors.toList());
        return result;
    }

    /* 권한 String 생성 메소드*/
    public static List<String> createUserRoles(String username) {
        return Authority.USER.getStringRole();
    }

    public static List<String> createAdminRoles(String username) {
        return Authority.ADMIN.getStringRole();
    }

}