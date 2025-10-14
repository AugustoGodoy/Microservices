package br.edu.atitus.currency_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import br.edu.atitus.currency_service.dtos.BCBResponse;

@FeignClient(name = "bcb-client", url = "https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata")
public interface BCBClient {
    
    @GetMapping("/CotacaoMoedaDia(moeda=@moeda,dataCotacao=@dataCotacao)?@moeda='{currency}'&@dataCotacao='{date}'&$format={format}")
    BCBResponse getCurrencyRate(
        @PathVariable("currency") String currency,
        @PathVariable("date") String date,
        @PathVariable("format") String format
    );
}
