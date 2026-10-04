package com.example.aws_project01.controller;

import com.example.aws_project01.model.Product;
import com.example.aws_project01.repository.ProductRepository;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> saveProduct(@PathVariable Long id, @RequestBody Product product) {
        if(!productRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        product.setId(id);
        return ResponseEntity.status(HttpStatus.OK).body(productRepository.save(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Product> deleteProduct(@PathVariable Long id) {
        if(!productRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        productRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/bycode")
    public ResponseEntity<Product> findByCode(@RequestParam String code) {
        Optional<Product> optionalProduct = productRepository.findByCode(code);
        return optionalProduct.map(product -> ResponseEntity.status(HttpStatus.OK).body(product))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
