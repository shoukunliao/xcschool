package com.xuecheng.system;

import com.xuecheng.system.model.po.Dictionary;
import com.xuecheng.system.service.DictionaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class DicTest {

    @Autowired
    DictionaryService dictionaryService;

    @Test
    public void listTest(){
        List<Dictionary> list = dictionaryService.list();
        list.forEach(System.out::println);

    }
}
