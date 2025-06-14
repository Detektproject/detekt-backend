package com.resoluteitconsulting.ruledefender.domain.usecases;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
@Slf4j
public class EmailService {

    @Value("${app.sendgrid.apiKey}")
    private String apiKey;

    @Value("${app.sendgrid.templateId}")
    private String templateId;

    public void sendSimpleEmail(String email, String activationCode) {
        Email from = new Email("no-reply@resoluteitconsulting.com");
        Email to = new Email(email);
        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setTemplateId(templateId);

        Personalization personalization = new Personalization();
        personalization.addTo(to);
        personalization.addDynamicTemplateData("activationCode", activationCode);
        mail.addPersonalization(personalization);
        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            log.info("Status code: {}, Body: {}, Headers: {}", response.getStatusCode(), response.getBody(), response.getHeaders());
        } catch (IOException ex) {
            log.error("Error occurred while sending email", ex);
        }
    }


}
