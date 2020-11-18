package com.hider.vclub;

import com.hider.vclub.util.SentistiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = VclubApplication.class)
public class SensitiveTests {
    @Autowired
    private SentistiveFilter sentistiveFilter;

    @Test
    public void testSensitiveFilter() {
        String text = "这里可以赌博,可以嫖娼的啊.";
        text = sentistiveFilter.filter(text);
        System.out.println(text);
    }


}
