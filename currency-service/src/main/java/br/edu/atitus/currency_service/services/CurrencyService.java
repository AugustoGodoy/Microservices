package br.edu.atitus.currency_service.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import br.edu.atitus.currency_service.clients.BCBClient;
import br.edu.atitus.currency_service.dtos.BCBResponse;
import br.edu.atitus.currency_service.entities.CurrencyEntity;
import br.edu.atitus.currency_service.repositories.CurrencyRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class CurrencyService {
    
    private final BCBClient bcbClient;
    private final CurrencyRepository currencyRepository;
    
    public CurrencyService(BCBClient bcbClient, CurrencyRepository currencyRepository) {
        this.bcbClient = bcbClient;
        this.currencyRepository = currencyRepository;
    }
    
    @CircuitBreaker(name = "bcb-api", fallbackMethod = "getCurrencyFromDatabaseFallback")
    public CurrencyEntity getCurrencyRate(String source, String target) {
        return getCurrencyFromBCB(source, target);
    }
    
    @Cacheable(value = "currency", key = "#source + '_' + #target")
    public CurrencyEntity getCurrencyRateWithCache(String source, String target) {
        return getCurrencyRate(source, target);
    }
    
    public CurrencyEntity getCurrencyFromBCB(String source, String target) {
        String date = getLastBusinessDay();
        
        BCBResponse response = bcbClient.getCurrencyRate(
            source, 
            date, 
            "json"
        );
        
        if (response != null && response.getValue() != null && !response.getValue().isEmpty()) {
            BCBResponse.BCBValue bcbValue = response.getValue().get(0);
            double rate = Double.parseDouble(bcbValue.getCotacaoCompra());
            
            CurrencyEntity currency = new CurrencyEntity();
            currency.setSource(source);
            currency.setTarget(target);
            currency.setConversionRate(rate);
            currency.setEnviroment("API BCB");
            
            return currency;
        } else {
            throw new RuntimeException("No data found in BCB API");
        }
    }
    
    public CurrencyEntity getCurrencyFromDatabaseFallback(String source, String target, Exception ex) {
        Optional<CurrencyEntity> currencyOpt = currencyRepository.findBySourceAndTarget(source, target);
        
        if (currencyOpt.isPresent()) {
            CurrencyEntity currency = currencyOpt.get();
            currency.setEnviroment("Local Database");
            return currency;
        } else {
            throw new RuntimeException("Currency not found in database for " + source + " -> " + target);
        }
    }
    
    private String getLastBusinessDay() {
        LocalDate today = LocalDate.now();
        LocalDate lastBusinessDay = today;
        
        // Se for fim de semana, volta para sexta
        while (lastBusinessDay.getDayOfWeek().getValue() > 5) {
            lastBusinessDay = lastBusinessDay.minusDays(1);
        }
        
        // API BCB espera formato MM-dd-yyyy
        return lastBusinessDay.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
    }
}
