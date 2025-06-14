package com.resoluteitconsulting.ruledefender.domain.usecases;

import com.resoluteitconsulting.ruledefender.domain.model.OrganizationUser;

public interface UserService {

    String createUser(OrganizationUser organizationUser);

    String createUserWithoutPassword(OrganizationUser organizationUser);

    void resetUserPassword(OrganizationUser organizationUser);

    String generateRandomPassword(int length);
}
