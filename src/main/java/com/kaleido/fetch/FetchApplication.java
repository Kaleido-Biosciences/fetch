package com.kaleido.fetch;

import com.kaleido.cabinet.client.CabinetClientConfiguration;
import com.kaleido.kaptureclient.KaptureClientConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({KaptureClientConfiguration.class,CabinetClientConfiguration.class})
@ComponentScan("com.kaleido")
public class FetchApplication {

    public static void main(String[] args) {
        SpringApplication.run(FetchApplication.class, args);
    }

}
