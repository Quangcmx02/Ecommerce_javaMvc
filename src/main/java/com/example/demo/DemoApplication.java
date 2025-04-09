package com.example.demo;

import com.example.demo.Entity.Category;
import com.example.demo.Entity.Product;
import com.example.demo.Repository.CategoryRepository;
import com.example.demo.Repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	CommandLineRunner run(ProductRepository productRepository, CategoryRepository categoryRepository) {
		return args -> {
			if (productRepository.count() == 10) {
				Category category = new Category();
				category.setName("Category 2");
				categoryRepository.save(category);

				List<String> imageLinks = List.of(
						"https://i.pinimg.com/474x/2a/7b/47/2a7b47351c2561a8d9b3ed46e4bc4453.jpg",
						"https://i.pinimg.com/474x/ef/a4/34/efa434fd0559a6767f268aeccd8606d5.jpg",
						"https://i.pinimg.com/474x/7e/b5/9f/7eb59fa8228de20561456b908ef58c17.jpg",
						"https://i.pinimg.com/474x/cd/24/d5/cd24d569d17c64ba817e9785f6170eab.jpg",
						"https://i.pinimg.com/474x/6a/79/ca/6a79caad39b9520bcd7458c6ab226b0c.jpg"

				);

				for (int i = 10; i <= 20; i++) {
					Product p = new Product();
					p.setName("Sản phẩm " + i);
					p.setQuantity(10 + i);
					p.setSize("M");
					p.setDescription("Mô tả cho sản phẩm " + i);
					p.setImageLink(imageLinks.get(i % 2));
					p.setActive(true);
					p.setPrice(100000f + i * 10000f);
					p.setStatus(true);
					p.setCategory(category);
					productRepository.save(p);
				}
			}
		};
	}
}
