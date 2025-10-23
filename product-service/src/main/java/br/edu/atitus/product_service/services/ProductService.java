package br.edu.atitus.product_service.services;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import br.edu.atitus.product_service.clients.CurrencyClient;
import br.edu.atitus.product_service.clients.CurrencyResponse;
import br.edu.atitus.product_service.entities.ProductEntity;
import br.edu.atitus.product_service.repositories.ProductRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CurrencyClient currencyClient;
    
    public ProductService(ProductRepository productRepository, CurrencyClient currencyClient) {
        this.productRepository = productRepository;
        this.currencyClient = currencyClient;
    }
    
    public ProductEntity getProductWithConversion(Long idProduct, String targetCurrency) {
        ProductEntity product = productRepository.findById(idProduct)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (targetCurrency.equalsIgnoreCase(product.getCurrency())) {
            product.setConvertedPrice(product.getPrice());
        } else {
            CurrencyResponse currency = getCurrencyWithFallback(
                product.getPrice(), 
                product.getCurrency(), 
                targetCurrency
            );
            product.setConvertedPrice(currency.getConvertedValue());
            product.setEnviroment(product.getEnviroment() + " - " + currency.getEnviroment());
        }
        
        return product;
    }
    
    @Cacheable(value = "product", key = "#idProduct + '_' + #targetCurrency")
    public ProductEntity getProductWithConversionCached(Long idProduct, String targetCurrency) {
        return getProductWithConversion(idProduct, targetCurrency);
    }
    
    @CircuitBreaker(name = "currency-service", fallbackMethod = "getCurrencyFallback")
    public CurrencyResponse getCurrencyWithFallback(double value, String source, String target) {
        return currencyClient.getCurrency(value, source, target);
    }
    
    public CurrencyResponse getCurrencyFallback(double value, String source, String target, Exception ex) {
        // Fallback response when currency service is unavailable
        CurrencyResponse fallbackResponse = new CurrencyResponse();
        fallbackResponse.setConvertedValue(-1);
        fallbackResponse.setEnviroment("Currency service unavailable - Fallback");
        return fallbackResponse;
    }
}
