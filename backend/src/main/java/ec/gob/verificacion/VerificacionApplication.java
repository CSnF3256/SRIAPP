package ec.gob.verificacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class VerificacionApplication {
    public static void main(String[] args) {
        SpringApplication.run(VerificacionApplication.class, args);
    }
}
