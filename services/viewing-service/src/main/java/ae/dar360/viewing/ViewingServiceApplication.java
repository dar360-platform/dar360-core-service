package ae.dar360.viewing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {"ae.dar360.viewing.client.feign"})
public class ViewingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ViewingServiceApplication.class, args);
    }
}
