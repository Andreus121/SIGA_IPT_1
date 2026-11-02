package cl.siga.siga_ipt.config;

import cl.siga.siga_ipt.service.SeedDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeedDataRunner implements ApplicationRunner {

    private final SeedDataService seedDataService;

    @Override
    public void run(ApplicationArguments args) {
        seedDataService.seedIfDatabaseIsEmpty(SeedData.data());
    }
}
