package com.auction.managers;
import com.auction.enums.AuctionState;

import com.auction.enums.ItemCategory;
import com.auction.enums.UserRole;
import com.auction.models.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Singleton class managing all auctions and users in the system
 * Thread-safe implementation using double-checked locking
 */
public class AuctionManager {
    private static volatile AuctionManager instance;
    
    private Map<String, AuctionItem> auctions;
    private Map<String, User> users;
    private ScheduledExecutorService scheduler;
    private Map<String, ScheduledFuture<?>> scheduledTasks;
    
    /**
     * Private constructor for Singleton pattern
     */
    private AuctionManager() {
        auctions = new ConcurrentHashMap<>();
        users = new ConcurrentHashMap<>();
        scheduler = Executors.newScheduledThreadPool(10);
        scheduledTasks = new ConcurrentHashMap<>();
    }
    
    /**
     * Get the singleton instance (thread-safe double-checked locking)
     */
    public static AuctionManager getInstance() {
        if (instance == null) {
            synchronized (AuctionManager.class) {
                if (instance == null) {
                    instance = new AuctionManager();
                }
            }
        }
        return instance;
    }
    
    // ==================== USER MANAGEMENT ====================
    
    /**
     * Register a new user
     */
    public boolean registerUser(String username, String password, String email, UserRole role) {
        // Check if username already exists
        if (users.values().stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username))) {
            System.out.println("❌ Registration failed: Username '" + username + "' already exists");
            return false;
        }
        
        // Create user based on role
        User newUser = (role == UserRole.ADMIN) 
            ? new Admin(username, password, email)
            : new User(username, password, email, role);
        
        users.put(newUser.getUserId(), newUser);
        System.out.println("✅ User registered successfully: " + username + " (" + role + ")");
        return true;
    }
    
    /**
     * Login user with username and password
     */
    public User login(String username, String password) {
        Optional<User> userOpt = users.values().stream()
            .filter(u -> u.getUsername().equalsIgnoreCase(username))
            .findFirst();
        
        if (userOpt.isPresent() && userOpt.get().verifyPassword(password)) {
            System.out.println("✅ Login successful: " + username);
            return userOpt.get();
        }
        
        System.out.println("❌ Login failed: Invalid username or password");
        return null;
    }
    
    /**
     * Get all users (for admin)
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(String userId) {
        return users.get(userId);
    }
    
    // ==================== AUCTION MANAGEMENT ====================
    
    /**
     * Create a new auction
     */
    public String createAuction(String title, String description, double startingPrice,
                               double reservePrice, User seller, ItemCategory category,
                               int durationMinutes) {
        
        // Validate inputs
        if (startingPrice < 0 || reservePrice < 0) {
            System.out.println("❌ Invalid prices");
            return null;
        }
        
        if (durationMinutes <= 0) {
            System.out.println("❌ Invalid duration");
            return null;
        }
        
        LocalDateTime endTime = LocalDateTime.now().plusMinutes(durationMinutes);
        AuctionItem item = new AuctionItem(title, description, startingPrice,
                                          reservePrice, seller, category, endTime);
        
        auctions.put(item.getItemId(), item);
        
        // Start auction immediately
        item.startAuction();
        
        // Schedule auction end
        scheduleAuctionEnd(item, durationMinutes);
        
        System.out.printf("✅ Auction created: '%s' (Duration: %d minutes)\n", title, durationMinutes);
        return item.getItemId();
    }
    
    /**
     * Schedule automatic auction end
     */
    private void scheduleAuctionEnd(AuctionItem item, int durationMinutes) {
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            System.out.println("\n⏰ Timer expired for auction: " + item.getTitle());
            item.endAuction();
            scheduledTasks.remove(item.getItemId());
        }, durationMinutes, TimeUnit.MINUTES);
        
        scheduledTasks.put(item.getItemId(), future);
    }
    
    /**
     * Remove an auction (admin only)
     */
    public boolean removeAuction(String itemId) {
        AuctionItem item = auctions.remove(itemId);
        if (item != null) {
            // Cancel scheduled task
            ScheduledFuture<?> task = scheduledTasks.remove(itemId);
            if (task != null) {
                task.cancel(false);
            }
            
            // End auction if still active
            if (item.getState() == AuctionState.ACTIVE) {
                item.endAuction();
            }
            
            System.out.println("✅ Auction removed: " + item.getTitle());
            return true;
        }
        return false;
    }
    
    /**
     * Get all auctions
     */
    public List<AuctionItem> getAllAuctions() {
        return new ArrayList<>(auctions.values());
    }
    
    /**
     * Get active auctions only
     */
    public List<AuctionItem> getActiveAuctions() {
        return auctions.values().stream()
            .filter(a -> a.getState() == AuctionState.ACTIVE)
            .sorted(Comparator.comparing(AuctionItem::getEndTime))
            .collect(Collectors.toList());
    }
    
    /**
     * Search auctions by keyword
     */
    public List<AuctionItem> searchAuctions(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return auctions.values().stream()
            .filter(a -> a.getTitle().toLowerCase().contains(lowerKeyword) ||
                        a.getDescription().toLowerCase().contains(lowerKeyword))
            .collect(Collectors.toList());
    }
    
    /**
     * Get auctions by category
     */
    public List<AuctionItem> getAuctionsByCategory(ItemCategory category) {
        return auctions.values().stream()
            .filter(a -> a.getCategory() == category)
            .collect(Collectors.toList());
    }
    
    /**
     * Get auction by ID
     */
    public AuctionItem getAuction(String itemId) {
        return auctions.get(itemId);
    }
    
    /**
     * Get auctions by seller
     */
    public List<AuctionItem> getAuctionsBySeller(User seller) {
        return auctions.values().stream()
            .filter(a -> a.getSellerId().equals(seller.getUserId()))
            .collect(Collectors.toList());
    }
    
    // ==================== SYSTEM MANAGEMENT ====================
    
    /**
     * Shutdown the scheduler gracefully
     */
    public void shutdown() {
        System.out.println("\nShutting down auction system...");
        
        // End all active auctions
        auctions.values().stream()
            .filter(a -> a.getState() == AuctionState.ACTIVE)
            .forEach(AuctionItem::endAuction);
        
        // Shutdown scheduler
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Auction system shutdown complete.");
    }
    
    /**
     * Get system statistics
     */
    public void printStatistics() {
        System.out.println("\n=== SYSTEM STATISTICS ===");
        System.out.println("Total Users: " + users.size());
        System.out.println("Total Auctions: " + auctions.size());
        System.out.println("Active Auctions: " + getActiveAuctions().size());
        
        long totalBids = auctions.values().stream()
            .mapToLong(a -> a.getBidHistory().size())
            .sum();
        System.out.println("Total Bids Placed: " + totalBids);
    }
}
