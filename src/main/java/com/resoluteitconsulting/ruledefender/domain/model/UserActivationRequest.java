package com.resoluteitconsulting.ruledefender.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserActivationRequest {

    private String email;
    private String activationCode;

}
