package com.monthlyib.server.constant;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.List;

@Getter
public enum Authority {
    ADMIN(List.of("ADMIN", "USER"), AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER"), "관리자"),
    USER(List.of("USER"), AuthorityUtils.createAuthorityList( "ROLE_USER"), "사용자");

    private final List<String> stringRole;

    private final List<GrantedAuthority> role;

    private final String title;

    Authority(List<String> stringRole, List<GrantedAuthority> role, String title) {
        this.stringRole = stringRole;
        this.role = role;
        this.title = title;
    }
}

