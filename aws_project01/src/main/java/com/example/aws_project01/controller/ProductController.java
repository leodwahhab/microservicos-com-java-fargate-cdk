package com.example.aws_project01.controller;

import com.example.aws_project01.enums.EventType;
import com.example.aws_project01.model.Product;
import com.example.aws_project01.repository.ProductRepository;
import com.example.aws_project01.service.ProductPublisher;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@AllArgsConstructor
public class ProductController {
    private ProductRepository productRepository;
    private ProductPublisher productPublisher;

    @GetMapping
    public Collection<Product> findAll() {
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> findById(@PathVariable Long id) {
        Optional<Product> optional = productRepository.findById(id);

        return optional.map(product -> ResponseEntity.status(HttpStatus.OK).body(product))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Product> saveProduct(@RequestBody Product product) {
        Product savedProduct = productRepository.save(product);

        productPublisher.publishProductEvent(savedProduct, EventType.PRODUCT_CREATED, "Harry");

        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> saveProduct(@PathVariable Long id, @RequestBody Product product) {
        if(!productRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        product.setId(id);

        Product updatedProduct = productRepository.save(product);

        productPublisher.publishProductEvent(updatedProduct, EventType.PRODUCT_UPDATED, "doralice");

        return ResponseEntity.status(HttpStatus.OK).body(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Product> deleteProduct(@PathVariable Long id) {
        Optional<Product> deletedProduct = productRepository.findById(id);

        if(deletedProduct.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        productRepository.delete(deletedProduct.get());

        productPublisher.publishProductEvent(deletedProduct.get(), EventType.PRODUCT_DELETED, "Hillary");

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/bycode")
    public ResponseEntity<Product> findByCode(@RequestParam String code) {
        Optional<Product> optionalProduct = productRepository.findByCode(code);
        return optionalProduct.map(product -> ResponseEntity.status(HttpStatus.OK).body(product))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
