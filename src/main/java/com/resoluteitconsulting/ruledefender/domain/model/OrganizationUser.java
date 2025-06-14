package com.resoluteitconsulting.ruledefender.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrganizationUser {

    private String username;
    private String fullName;
    private String password;
    private String email;
    private String userId;

}
