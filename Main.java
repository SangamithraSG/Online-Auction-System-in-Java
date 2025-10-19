package com.auction;

import com.auction.enums.*;
import com.auction.managers.AuctionManager;
import com.auction.models.*;
import com.auction.ui.ConsoleUI;

import java.time.LocalDateTime;

/**
 * Main entry point for the Online Auction System
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("Starting Online Auction System...");
        
        // Option 1: Run with sample data (for demo/testing)
        if (args.length > 0 && args[0].equals("--demo")) {
            runDemoMode();
        } 
        // Option 2: Run interactive console UI
        else {
            runInteractiveMode();
        }
    }
    
    /**
     * Run demo mode with sample data
     */
    private static void runDemoMode() {
        System.out.println("\n=== DEMO MODE ===\n");
        
        AuctionManager manager = AuctionManager.getInstance();
        
        // Create sample users
        System.out.println("Creating sample users...");
        manager.registerUser("alice", "password123", "alice@auction.com", UserRole.USER);
        manager.registerUser("bob", "password123", "bob@auction.com", UserRole.USER);
        manager.registerUser("charlie", "password123", "charlie@auction.com", UserRole.USER);
        manager.registerUser("admin", "admin123", "admin@auction.com", UserRole.ADMIN);
        
        // Login users
        User alice = manager.login("alice", "password123");
        User bob = manager.login("bob", "password123");
        User charlie = manager.login("charlie", "password123");
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("CREATING SAMPLE AUCTIONS");
        System.out.println("=".repeat(60) + "\n");
        
        // Create sample auctions
        String auction1 = manager.createAuction(
            "Vintage Rolex Watch",
            "Rare 1960s Rolex Submariner in excellent condition",
            500.0,
            800.0,
            alice,
            ItemCategory.COLLECTIBLES,
            10 // 10 minutes
        );
        
        String auction2 = manager.createAuction(
            "MacBook Pro 2024",
            "Brand new MacBook Pro 16-inch with M3 chip",
            1200.0,
            1500.0,
            alice,
            ItemCategory.ELECTRONICS,
            15 // 15 minutes
        );
        
        String auction3 = manager.createAuction(
            "Original Picasso Painting",
            "Authentic Picasso artwork from 1952",
            5000.0,
            8000.0,
            bob,
            ItemCategory.ART,
            20 // 20 minutes
        );
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SIMULATING BIDDING ACTIVITY");
        System.out.println("=".repeat(60) + "\n");
        
        // Get auction items
        AuctionItem watch = manager.getAuction(auction1);
        AuctionItem laptop = manager.getAuction(auction2);
        AuctionItem painting = manager.getAuction(auction3);
        
        // Simulate bidding
        if (watch != null && bob != null && charlie != null) {
            System.out.println("\n--- Bidding on Vintage Rolex Watch ---");
            watch.placeBid(bob, 550.0);
            
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            
            watch.placeBid(charlie, 650.0);
            
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            
            watch.placeBid(bob, 750.0);
            
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            
            watch.placeBid(charlie, 900.0); // Meets reserve!
        }
        
        if (laptop != null && bob != null && charlie != null) {
            System.out.println("\n--- Bidding on MacBook Pro ---");
            laptop.placeBid(bob, 1300.0);
            
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            
            laptop.placeBid(charlie, 1400.0);
        }
        
        if (painting != null && charlie != null) {
            System.out.println("\n--- Bidding on Picasso Painting ---");
            painting.placeBid(charlie, 5500.0); // Below reserve
        }
        
        // Display statistics
        System.out.println("\n" + "=".repeat(60));
        manager.printStatistics();
        System.out.println("=".repeat(60));
        
        // Show active auctions
        System.out.println("\n--- ACTIVE AUCTIONS ---");
        for (AuctionItem item : manager.getActiveAuctions()) {
            System.out.println(item);
            System.out.println("  Bids: " + item.getBidHistory().size() + 
                             " | Reserve " + (item.isReserveMet() ? "✅ Met" : "⚠️ Not Met"));
        }
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("DEMO COMPLETE - Auctions will automatically end when timers expire");
        System.out.println("=".repeat(60));
        
        // Keep application running for a bit to show timer functionality
        System.out.println("\nPress Ctrl+C to exit or wait for auctions to end...\n");
        
        try {
            Thread.sleep(60000); // Wait 1 minute
        } catch (InterruptedException e) {
            System.out.println("\nDemo interrupted.");
        }
        
        manager.shutdown();
    }
    
    /**
     * Run interactive console mode
     */
    private static void runInteractiveMode() {
        // Optionally load saved data
        // FileManager.loadUsers();
        // FileManager.loadAuctions();
        
        ConsoleUI ui = new ConsoleUI();
        ui.start();
    }
}
