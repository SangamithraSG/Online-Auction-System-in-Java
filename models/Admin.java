package com.auction.models;

import com.auction.enums.UserRole;
import com.auction.managers.AuctionManager;

/**
 * Admin class extending User with additional administrative privileges
 */
public class Admin extends User {
    private static final long serialVersionUID = 1L;
    
    public Admin(String username, String password, String email) {
        super(username, password, email, UserRole.ADMIN);
    }
    
    /**
     * Remove an auction from the system
     */
    public boolean removeAuction(String itemId) {
        boolean removed = AuctionManager.getInstance().removeAuction(itemId);
        if (removed) {
            System.out.println("[ADMIN] Successfully removed auction: " + itemId);
        } else {
            System.out.println("[ADMIN] Failed to remove auction: " + itemId);
        }
        return removed;
    }
    
    /**
     * View all users in the system
     */
    public void viewAllUsers() {
        System.out.println("\n=== All Registered Users ===");
        var users = AuctionManager.getInstance().getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users registered.");
        } else {
            for (User user : users) {
                System.out.println(user);
            }
        }
        System.out.println("Total users: " + users.size());
    }
    
    /**
     * View all auctions in the system
     */
    public void viewAllAuctions() {
        System.out.println("\n=== All Auctions ===");
        var auctions = AuctionManager.getInstance().getAllAuctions();
        if (auctions.isEmpty()) {
            System.out.println("No auctions available.");
        } else {
            for (AuctionItem item : auctions) {
                System.out.printf("%s | State: %s | Current Bid: $%.2f\n", 
                    item.getTitle(), item.getState(), item.getCurrentBid());
            }
        }
        System.out.println("Total auctions: " + auctions.size());
    }
    
    @Override
    public String toString() {
        return String.format("Admin[%s - %s]", getUsername(), getEmail());
    }
}
