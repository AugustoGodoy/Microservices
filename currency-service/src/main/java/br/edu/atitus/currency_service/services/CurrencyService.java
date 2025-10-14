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

@Service
public class CurrencyService {
    
    private final BCBClient bcbClient;
    private final CurrencyRepository currencyRepository;
    
    public CurrencyService(BCBClient bcbClient, CurrencyRepository currencyRepository) {
        this.bcbClient = bcbClient;
        this.currencyRepository = currencyRepository;
    }
    
    public CurrencyEntity getCurrencyRate(String source, String target) {
        // Primeiro tenta buscar na API do BCB
        try {
            return getCurrencyFromBCB(source, target);
        } catch (Exception e) {
            // Fallback para o banco local
            System.out.println("❌ Erro na API BCB: " + e.getMessage());
            System.out.println("🔄 Fazendo fallback para banco local");
            return getCurrencyFromDatabase(source, target);
        }
    }
    
    @Cacheable(value = "currency", key = "#source + '_' + #target")
    public CurrencyEntity getCurrencyRateWithCache(String source, String target) {
        return getCurrencyRate(source, target);
    }
    
    public CurrencyEntity getCurrencyFromBCB(String source, String target) {
        try {
            String date = getLastBusinessDay();
            System.out.println("🔍 [DEBUG] Tentando chamar API BCB para " + source + " -> " + target + " na data: " + date);
            System.out.println("🔍 [DEBUG] URL completa: https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata/CotacaoMoedaDia(moeda=@moeda,dataCotacao=@dataCotacao)?@moeda='" + source + "'&@dataCotacao='" + date + "'&$format=json");
            
            BCBResponse response = bcbClient.getCurrencyRate(
                source, 
                date, 
                "json"
            );
            
            System.out.println("✅ [DEBUG] Resposta da API BCB recebida: " + (response != null ? "OK" : "NULL"));
            if (response != null && response.getValue() != null) {
                System.out.println("✅ [DEBUG] Número de registros na resposta: " + response.getValue().size());
            }
            
            if (response != null && response.getValue() != null && !response.getValue().isEmpty()) {
                BCBResponse.BCBValue bcbValue = response.getValue().get(0);
                System.out.println("✅ [DEBUG] Primeiro registro - CotacaoCompra: " + bcbValue.getCotacaoCompra() + ", CotacaoVenda: " + bcbValue.getCotacaoVenda());
                
                double rate = Double.parseDouble(bcbValue.getCotacaoCompra());
                
                CurrencyEntity currency = new CurrencyEntity();
                currency.setSource(source);
                currency.setTarget(target);
                currency.setConversionRate(rate);
                currency.setEnviroment("API BCB");
                
                System.out.println("✅ [DEBUG] Cotação criada com sucesso: " + rate);
                return currency;
            } else {
                System.out.println("❌ [DEBUG] Resposta vazia ou nula da API BCB");
                throw new RuntimeException("No data found in BCB API");
            }
        } catch (Exception e) {
            System.out.println("❌ [DEBUG] Erro detalhado na API BCB: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error calling BCB API: " + e.getMessage());
        }
    }
    
    public CurrencyEntity getCurrencyFromDatabase(String source, String target) {
        System.out.println("🔄 [DEBUG] Tentando buscar no banco local: " + source + " -> " + target);
        Optional<CurrencyEntity> currencyOpt = currencyRepository.findBySourceAndTarget(source, target);
        
        if (currencyOpt.isPresent()) {
            CurrencyEntity currency = currencyOpt.get();
            currency.setEnviroment("Local Database");
            System.out.println("✅ [DEBUG] Encontrado no banco local: " + currency.getConversionRate());
            return currency;
        } else {
            System.out.println("❌ [DEBUG] Moeda não encontrada no banco local: " + source + " -> " + target);
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
