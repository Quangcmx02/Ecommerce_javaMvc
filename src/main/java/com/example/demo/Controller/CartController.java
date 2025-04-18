package com.example.demo.Controller;

import com.example.demo.Entity.Cart;
import com.example.demo.Entity.CartItem;
import com.example.demo.Entity.Order;
import com.example.demo.Entity.User;
import com.example.demo.Service.CartService;
import com.example.demo.Service.LoggerService;
import com.example.demo.Service.OrderService;
import com.example.demo.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;
    @Autowired

    private UserService userService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private LoggerService loggerService;

    // Hiển thị giỏ hàng
    @GetMapping("/cart")
    public String viewCart(Model model) {
        Long userId = getCurrentUserId();
        try {
            Cart cart = cartService.getCartByUserId(userId);
            double totalPrice = cart.getCartItems().stream()
                    .mapToDouble(CartItem::getPrice)
                    .sum();
            model.addAttribute("cart", cart);
            model.addAttribute("totalPrice", totalPrice);
            if (cart.getCartItems().isEmpty()) {
                model.addAttribute("infoMsg", "Giỏ hàng của bạn đang trống.");
            }
            loggerService.logInfo("Displayed cart for userId: " + userId);
        } catch (Exception e) {
            loggerService.logError("Failed to display cart for userId: " + userId, e);
            model.addAttribute("errorMsg", "Không thể tải giỏ hàng: " + e.getMessage());
        }
        return "cart/cart";
    }
    // Cập nhật số lượng sản phẩm
    @PostMapping("/update")
    public String updateCartItem(
            @RequestParam("cartItemId") Long cartItemId,
            @RequestParam("quantity") int quantity,
            RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId();
        try {
            cartService.updateCartItemQuantity(cartItemId, quantity);
            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật số lượng thành công!");
            loggerService.logInfo("Updated quantity for cartItemId: " + cartItemId + ", userId: " + userId);
        } catch (IllegalArgumentException e) {
            loggerService.logError("Invalid quantity for cartItemId: " + cartItemId, e);
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            loggerService.logError("Failed to update cartItemId: " + cartItemId, e);
            redirectAttributes.addFlashAttribute("errorMsg", "Không thể cập nhật số lượng: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    // Xóa sản phẩm khỏi giỏ hàng
    @PostMapping("/remove")
    public String removeCartItem(
            @RequestParam("cartItemId") Long cartItemId,
            RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId();
        try {
            cartService.removeCartItem(cartItemId);
            redirectAttributes.addFlashAttribute("successMsg", "Đã xóa sản phẩm khỏi giỏ hàng!");
            loggerService.logInfo("Removed cartItemId: " + cartItemId + ", userId: " + userId);
        } catch (Exception e) {
            loggerService.logError("Failed to remove cartItemId: " + cartItemId, e);
            redirectAttributes.addFlashAttribute("errorMsg", "Không thể xóa sản phẩm: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    // Xóa toàn bộ giỏ hàng
    @PostMapping("/clear")
    public String clearCart(RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId();
        try {
            Cart cart = cartService.getCartByUserId(userId);
            cartService.clearCart(cart.getCartId());
            redirectAttributes.addFlashAttribute("successMsg", "Đã xóa toàn bộ giỏ hàng!");
            loggerService.logInfo("Cleared cart for userId: " + userId);
        } catch (Exception e) {
            loggerService.logError("Failed to clear cart for userId: " + userId, e);
            redirectAttributes.addFlashAttribute("errorMsg", "Không thể xóa giỏ hàng: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    // Lấy userId từ SecurityContext (giả định email là username)
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);
        if (user == null) {
            throw new IllegalStateException("User not found");
        }
        return user.getUserId();
    }
    // Thêm sản phẩm vào giỏ hàng
    @PostMapping("/add")
    public String addToCart(
            @RequestParam("id") Long productId,
            @RequestParam("quantity") int quantity,
            RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId();
        try {
            cartService.addToCart(userId, productId, quantity);
            redirectAttributes.addFlashAttribute("successMsg", "Thêm sản phẩm vào giỏ hàng thành công!");
            loggerService.logInfo("Added productId: " + productId + ", quantity: " + quantity + " to cart for userId: " + userId);
            return "redirect:/cart/cart";
        } catch (IllegalArgumentException e) {
            loggerService.logError("Failed to add productId: " + productId + " to cart for userId: " + userId, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/product/detail/" + productId;
        } catch (Exception e) {
            loggerService.logError("Failed to add productId: " + productId + " to cart for userId: " + userId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể thêm sản phẩm vào giỏ hàng: " + e.getMessage());
            return "redirect:/product/detail/" + productId;
        }
    }
    // Hiển thị trang thanh toán
    @GetMapping("/checkout")
    public String showCheckout(Model model) {
        Long userId = getCurrentUserId();
        try {
            Cart cart = cartService.getCartByUserId(userId);
            if (cart.getCartItems().isEmpty()) {
                model.addAttribute("errorMsg", "Giỏ hàng trống, không thể thanh toán");
                return "cart/cart";
            }
            double totalPrice = cart.getCartItems().stream()
                    .mapToDouble(CartItem::getPrice)
                    .sum();
            model.addAttribute("cart", cart);
            model.addAttribute("totalPrice", totalPrice);
            loggerService.logInfo("Displayed checkout page for userId: " + userId);
        } catch (Exception e) {
            loggerService.logError("Failed to display checkout page for userId: " + userId, e);
            model.addAttribute("errorMsg", "Không thể tải trang thanh toán: " + e.getMessage());
        }
        return "cart/checkout";
    }

    // Xử lý thanh toán
    @PostMapping("/place-order")
    public String placeOrder(@RequestParam("address") String address, RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId();
        try {
            Order order = orderService.placeOrder(userId, address);
            redirectAttributes.addFlashAttribute("successMsg", "Đặt hàng thành công! Mã đơn hàng: " + order.getOrderId());
            loggerService.logInfo("Order placed successfully for userId: " + userId + ", orderId: " + order.getOrderId());
            return "redirect:/cart/cart";
        } catch (IllegalStateException e) {
            loggerService.logError("Failed to place order for userId: " + userId, e);
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/cart/checkout";
        } catch (Exception e) {
            loggerService.logError("Failed to place order for userId: " + userId, e);
            redirectAttributes.addFlashAttribute("errorMsg", "Không thể đặt hàng: " + e.getMessage());
            return "redirect:/cart/checkout";
        }
    }

//
}
