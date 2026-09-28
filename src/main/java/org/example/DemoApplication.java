package org.example;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
//    @Bean
//    public CommandLineRunner dumpStatements(SqlSessionFactory sqlSessionFactory) {
//        return args -> sqlSessionFactory.getConfiguration()
//                .getMappedStatementNames()
//                .forEach(name -> System.out.println(">>> " + name));
//    }
}

