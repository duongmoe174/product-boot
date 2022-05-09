package com.duong.productboot.controller;

import com.duong.productboot.model.Category;
import com.duong.productboot.model.Product;
import com.duong.productboot.model.ProductForm;
import com.duong.productboot.service.category.ICategoryService;
import com.duong.productboot.service.product.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Controller
@RequestMapping("/products")
@CrossOrigin("*")
public class ProductController {
    @Autowired
    private Environment environment;

    @Autowired
    private ICategoryService categoryService;
    @Autowired
    private IProductService productService;

    @GetMapping("")
    public ResponseEntity<Iterable<Product>> getAllProduct() {
        Iterable<Product> products = productService.findAll();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/categories")
    public ResponseEntity<Iterable<Category>> getAllCategories() {
        Iterable<Category> categories = categoryService.findAll();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @GetMapping("/list")
    public ModelAndView showListProduct() {
        ModelAndView modelAndView = new ModelAndView("list");
        modelAndView.addObject("products", productService.findAll());
        return modelAndView;
    }

    @PostMapping("")
    public ResponseEntity<Product> createProduct (@ModelAttribute ProductForm productForm) {
        MultipartFile file = productForm.getImage();
        String fileName = file.getOriginalFilename();
        String fileUpload = environment.getProperty("upload.path").toString();
        String name = productForm.getName();
        Category category = productForm.getCategory();
        Product product = new Product(name, fileName, category);
        try {
            FileCopyUtils.copy(productForm.getImage().getBytes(), new File(fileUpload + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        productService.save(product);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Product> deleteProduct(@PathVariable Long id) {
        Optional<Product> productOptional = productService.findById(id);
        if (!productOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        productService.remove(id);
        return new ResponseEntity<>(productOptional.get(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        Optional<Product> productOptional = productService.findById(id);
        if (!productOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(productOptional.get(), HttpStatus.OK);
    }

    @PostMapping("/edit/{id}")
    public ResponseEntity<Product> editProduct (@PathVariable Long id, @ModelAttribute ProductForm productForm) {
        Optional<Product> productOptional = productService.findById(id);
        if (!productOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            productService.remove(id);
            MultipartFile file = productForm.getImage();
            String fileName = file.getOriginalFilename();
            String fileUpload = environment.getProperty("upload.path").toString();
            String name = productForm.getName();
            Category category = productForm.getCategory();
            Product product = new Product(name, fileName, category);
            try {
                FileCopyUtils.copy(productForm.getImage().getBytes(), new File(fileUpload + fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
            product.setId(id);
            productService.save(product);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }
}
