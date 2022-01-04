/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.platform.runtime.example;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.platform.runtime.example.entities.MailDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@ExternalTaskSubscription("SendEmail") // create a subscription for this topic name
public class SendEmailHandler implements ExternalTaskHandler {

  @Autowired
  private JavaMailSender javaMailSender;

  @Override
  public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
    MailDetails mailDetails = getMailDetails(externalTask);
    Logger.getLogger("Send Email")
            .log(Level.INFO, "Mail Details: "+" Sender: " + mailDetails.getSender() + " Receiver: " + mailDetails.getReceiver());
    // Validate the email addresses
    List<String> invalidEmailAddresses = new ArrayList();
    boolean invalidEmails = false;

    if(!ValidateEmail.isValidEmail(mailDetails.getSender())){
      invalidEmailAddresses.add(mailDetails.getSender());
      invalidEmails = true;
    }
    if(!ValidateEmail.isValidEmail(mailDetails.getReceiver())){
      invalidEmailAddresses.add(mailDetails.getReceiver());
      invalidEmails = true;
    }
    if(invalidEmails)
    {
      externalTaskService.handleBpmnError(externalTask, "INVALID_EMAIL",
              "The email address(s) " + invalidEmailAddresses + " found to be invalid");

    }else {

      // If both emails are valid we will try to send the email
      try {
        sendMail(mailDetails.getSender(), mailDetails.getReceiver(), mailDetails.getSubject(), mailDetails.getBody());
        // complete the external task
        Logger.getLogger("Send Email")
                .log(Level.INFO, "Email Is Sent");
        externalTaskService.complete(externalTask);

      } catch (MessagingException e) {
        e.printStackTrace();
        externalTaskService.handleFailure(externalTask, "Email not Sent!", e.getMessage(), 0, 0);
        Logger.getLogger("Send Email")
                .log(Level.INFO, "Email Failed to Send");
      }

      // we could call an external service to create the loan documents here

      Logger.getLogger("Send Email")
              .log(Level.INFO, "Email Worker has Finished");
    }
  }

  private MailDetails getMailDetails(ExternalTask externalTask) {
    MailDetails mailDetails = new MailDetails();

    mailDetails.setSender((String) externalTask.getVariable("sender"));
    mailDetails.setReceiver((String) externalTask.getVariable("receiver"));
    mailDetails.setBody((String) externalTask.getVariable("body"));
    mailDetails.setSubject((String) externalTask.getVariable("subject"));

    return mailDetails;
  }

  private void sendMail(String sender, String receiver, String subject, String body) throws MessagingException {
    MimeMessage message = javaMailSender.createMimeMessage();

    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    helper.setFrom(sender);
    helper.setTo(receiver);
    helper.setSubject(subject);
    helper.setText(body, true);

    javaMailSender.send(message);
  }

}
