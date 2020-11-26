package com.shanjupay.merchant;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class TestApp {



    @Test
    public void test(){
        String str = "12312312321321";
        log.info("---------------------------------------------------有一个条数据{}",str);
    }
}
