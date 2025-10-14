package br.edu.atitus.currency_service.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.currency_service.entities.CurrencyEntity;
import br.edu.atitus.currency_service.services.CurrencyService;

@RestController
@RequestMapping("currency")
public class CurrencyController {
	
	private final CurrencyService currencyService;
	
	@Value("${server.port}")
	private int serverPort;

	public CurrencyController(CurrencyService currencyService) {
		super();
		this.currencyService = currencyService;
	}
	
	@GetMapping("/{value}/{source}/{target}")
	public ResponseEntity<CurrencyEntity> getConversion(
			@PathVariable double value,
			@PathVariable String source,
			@PathVariable String target) throws Exception{
		
		CurrencyEntity currency = currencyService.getCurrencyRateWithCache(source, target);
		
		currency.setConvertedValue(value * currency.getConversionRate());
		currency.setEnviroment(currency.getEnviroment() + " - Port: " + serverPort);
		
		return ResponseEntity.ok(currency);
	}
	
	
	

}
