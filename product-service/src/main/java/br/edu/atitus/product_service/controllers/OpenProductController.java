package br.edu.atitus.product_service.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.product_service.entities.ProductEntity;
import br.edu.atitus.product_service.services.ProductService;

@RestController
@RequestMapping("products")
public class OpenProductController {
	
	private final ProductService productService;
	
	@Value("${server.port}")
	private int serverPort;

	public OpenProductController(ProductService productService) {
		super();
		this.productService = productService;
	}
	
	@GetMapping("/{idProduct}/{targetCurrency}")
	public ResponseEntity<ProductEntity> getProduct(
			@PathVariable Long idProduct,
			@PathVariable String targetCurrency
 			) throws Exception {
		
		ProductEntity product = productService.getProductWithConversionCached(idProduct, targetCurrency);
		product.setEnviroment("Product-service running on Port: " + serverPort + " - " + product.getEnviroment());
		
		return ResponseEntity.ok(product);
	}

}
