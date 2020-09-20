package com.hider.vclub;

import com.hider.vclub.util.DirectMailClient;
import com.hider.vclub.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = VclubApplication.class)
public class MailTests {
    @Autowired
    private MailClient mailClient;

    @Autowired
    DirectMailClient directMailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail() {
        mailClient.sendMail("724598894@qq.com", "test", "Welcome");
    }

    @Test
    public void testHtmlMail() {
        Context context = new Context();
        context.setVariable("username", "Hider");

        String content = templateEngine.process("/mail/test", context);
        System.out.println(content);
        mailClient.sendMail("724598894@qq.com", "test", content);

    }

    @Test
    public void testDirectMail() {
        Context context = new Context();
        context.setVariable("username", "Hider");

        String content = templateEngine.process("/mail/test", context);
        System.out.println(content);
        directMailClient.sendMail("724598894@qq.com", "test", content);    }
}
