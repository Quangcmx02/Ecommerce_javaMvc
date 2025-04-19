package com.example.demo.Service.Impl;

import com.example.demo.Entity.Cart;
import com.example.demo.Entity.CartItem;
import com.example.demo.Entity.Product;
import com.example.demo.Entity.User;
import com.example.demo.Repository.CartRepository;
import com.example.demo.Service.CartService;
import com.example.demo.Service.LoggerService;
import com.example.demo.Service.ProductService;
import com.example.demo.Service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
private CartRepository cartRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private UserService userService;

    @Autowired
    private LoggerService loggerService;

    @Override
    public Cart getCartByUserId(Long userId) {
        loggerService.logInfo("Fetching cart for userId: " + userId);
        Optional<Cart> cartOptional = cartRepository.findByUserUserId(userId);
        if (cartOptional.isPresent()) {
            return cartOptional.get();
        }

        // Nếu không tìm thấy Cart, tạo mới
        loggerService.logInfo("Cart not found for userId: " + userId + ", creating new cart");
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new EntityNotFoundException("User not found for userId: " + userId);
        }

        Cart newCart = new Cart();
        newCart.setUser(user);
        newCart.setCreatedAt(new Date());
        newCart.setUpdatedAt(new Date());
        Cart savedCart = cartRepository.save(newCart);
        loggerService.logInfo("Created new cart for userId: " + userId + ", cartId: " + savedCart.getCartId());
        return savedCart;
    }

    @Override
    @Transactional
    public CartItem updateCartItemQuantity(Long cartItemId, int quantity) {
        loggerService.logInfo("Updating quantity for cartItemId: " + cartItemId + ", quantity: " + quantity);
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        Cart cart = cartRepository.findByCartItemId(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for cartItemId: " + cartItemId));
        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getCartItem_Id().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found: " + cartItemId));

        cartItem.setQuantity(quantity);
        cartItem.setPrice(cartItem.getProduct().getPrice() * quantity); // Cập nhật giá
        cart.setUpdatedAt(new Date());
        cartRepository.save(cart);

        loggerService.logInfo("Updated cartItemId: " + cartItemId + ", new quantity: " + quantity);
        return cartItem;
    }

    @Override
    @Transactional
    public void removeCartItem(Long cartItemId) {
        loggerService.logInfo("Removing cartItemId: " + cartItemId);
        Cart cart = cartRepository.findByCartItemId(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for cartItemId: " + cartItemId));
        cart.getCartItems().removeIf(item -> item.getCartItem_Id().equals(cartItemId));
        cart.setUpdatedAt(new Date());
        cartRepository.save(cart);
        loggerService.logInfo("Removed cartItemId: " + cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(Long cartId) {
        loggerService.logInfo("Clearing cartId: " + cartId);
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found: " + cartId));
        cart.getCartItems().clear();
        cart.setUpdatedAt(new Date());
        cartRepository.save(cart);
        loggerService.logInfo("Cleared cartId: " + cartId);
    }

    @Override
    @Transactional
    public CartItem addToCart(Long userId, Long productId, int quantity) {
        loggerService.logInfo("Adding productId: " + productId + ", quantity: " + quantity + " to cart for userId: " + userId);

        // Kiểm tra số lượng hợp lệ
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        // Lấy hoặc tạo Cart
        Cart cart = getCartByUserId(userId);

        // Lấy Product
        Optional<Product> productOptional = productService.findById(productId);
        if (!productOptional.isPresent()) {
            throw new EntityNotFoundException("Sản phẩm không tồn tại: " + productId);
        }
        Product product = productOptional.get();

        // Kiểm tra tồn kho
        if (quantity > product.getQuantity()) {
            throw new IllegalArgumentException("Số lượng yêu cầu vượt quá tồn kho: Còn lại " + product.getQuantity() + " sản phẩm");
        }

        // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
        Optional<CartItem> existingCartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getProductId().equals(productId))
                .findFirst();

        CartItem cartItem;
        if (existingCartItem.isPresent()) {
            // Cập nhật số lượng nếu sản phẩm đã có trong giỏ
            cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;
            if (newQuantity > product.getQuantity()) {
                throw new IllegalArgumentException("Tổng số lượng vượt quá tồn kho: Còn lại " + product.getQuantity() + " sản phẩm");
            }
            cartItem.setQuantity(newQuantity);
            cartItem.setPrice(product.getPrice() * newQuantity);
            loggerService.logInfo("Updated existing cartItem for productId: " + productId + ", new quantity: " + newQuantity);
        } else {
            // Tạo CartItem mới
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setPrice(product.getPrice() * quantity);
            cart.getCartItems().add(cartItem);
            loggerService.logInfo("Added new cartItem for productId: " + productId + ", quantity: " + quantity);
        }

        // Cập nhật thời gian và lưu Cart
        cart.setUpdatedAt(new Date());
        cartRepository.save(cart);

        return cartItem;
    }
    @Override
    public int countCartItems(Long userId) {
        loggerService.logInfo("Counting cart items for userId: " + userId);
        Cart cart = getCartByUserId(userId);
        int totalItems = cart.getCartItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        loggerService.logInfo("Total cart items for userId: " + userId + " is " + totalItems);
        return totalItems;
    }
}

