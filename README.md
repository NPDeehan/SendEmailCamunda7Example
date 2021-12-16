# Send Email External Task Example

This example shows how an External Task can connect to Camunda and send an email when a process reaches a service task with the `SendEmail` Topic

## Running the Project
Running the project has 3 pretty simple steps
1. Download and Run Camunda
1. Configure and Startup The Worker
1. Deploy the Send Email Process

Below I'll describe each step in detail if you aren't already aware of how Camunda projects generally work. 

### **Download and Run Camunda**

Simply go to the [Camunda.com and download](https://camunda.com/download/) the latest version of Camunda Run. 

Unzip the contents into a folder and run the `start.bat` or `start.sh` file depending on your operating system 


### **Configure and Startup The Worker**

The most important part of the worker to understand is the `SendEmailHandler.java` file. This is where all the work is done.

It work by subscribing to a *Topic* and when tasks for that topic are picked up it will send an email. This line shows that the worker is subscribed to the `SendEmail` Topic 

```java
@ExternalTaskSubscription("SendEmail") 
```

when it starts up it will send a request to Camunda asking for any tasks with that topic. 

When it finds one it will attempt to send an email. if successful it will return with

```java
externalTaskService.complete(externalTask);
```

If an error occurs it will return with a failure

```java
 externalTaskService.handleFailure(externalTask, "Email not Sent!", e.getMessage(), 0, 0);
```  

The configuration for the worker is all contained in the `application.yml` file and before you start up the worker you need to add some additional configuration to it so that it can send out an email.

```yml
spring.mail:
  host: smtp.gmail.com
  port: 587
  username: yourEmail@gmail.com
  password: yourPassword
  properties.mail.smtp:
    auth: true
    starttls.enable: true

```
Once you're added the details of your email server you can run the `Application.java` file within your IDE.

The worker will then connect to Camunda looking for work related to the `SendEmail` Topic

### **Deploy Email Process**

If you use this worker for any process but i've included a demo process to make it easy to test it out.

You just need to download the Camunda Modeler and open the `./ExampleProcess/SendEmailExample.bpmn` file. You can then deploy this file directly from the modeler. To do this click on the deploy button at the bottom and make sure you add the `EmailDetailsForm.form` to the deployment. This is in the same folder as the model. 

If you've deployed it successfully you should be able to go to
`