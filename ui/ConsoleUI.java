package com.auction.ui;

import com.auction.enums.*;
import com.auction.managers.AuctionManager;
import com.auction.models.*;

import java.util.*;

/**
 * Console-based User Interface for the Online Auction System
 */
public class ConsoleUI {
    private Scanner scanner;
    private AuctionManager manager;
    private User currentUser;
    
    public ConsoleUI() {
        scanner = new Scanner(System.in);
        manager = AuctionManager.getInstance();
    }
    
    public void start() {
        printBanner();
        
        while(true) {
            try {
                if (currentUser == null) {
                    showLoginMenu();
                } else {
                    showMainMenu();
                }
            } catch (Exception e) {
                System.out.println("\n❌ Error: " + e.getMessage());
                System.out.println("Please try again.\n");
            }
        }
    }
    
    private void printBanner() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("          ONLINE AUCTION SYSTEM");
        System.out.println("          Real-time Bidding Platform");
        System.out.println("=".repeat(60) + "\n");
    }
    
    private void showLoginMenu() {
        System.out.println("\n--- LOGIN MENU ---");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. View Active Auctions (Guest)");
        System.out.println("0. Exit");
        System.out.print("\nChoose option: ");
        
        int choice = getIntInput();
        
        switch (choice) {
            case 1 -> login();
            case 2 -> register();
            case 3 -> browseAuctions();
            case 0 -> exitApplication();
            default -> System.out.println("❌ Invalid option");
        }
    }
    
    private void login() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        currentUser = manager.login(username, password);
        if(currentUser != null) {
            System.out.println("\nWelcome back, " + currentUser.getUsername() + "!");
            if(currentUser.getRole() == UserRole.ADMIN) {
                System.out.println("⭐ Admin privileges enabled");
            }
        }
    }
    
    private void register() {
        System.out.println("\n--- REGISTRATION ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Register as Admin? (y/n): ");
        String isAdmin = scanner.nextLine().trim();
        UserRole role = isAdmin.equalsIgnoreCase("y") ? UserRole.ADMIN : UserRole.USER;
        
        if (manager.registerUser(username, password, email, role)) {
            System.out.println("\n✅ Registration successful! You can now login.");
        }
    }
    
    private void showMainMenu() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("MAIN MENU - Welcome, " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        System.out.println("=".repeat(60));
        System.out.println("""
            1. Browse Active Auctions
            2. Search Auctions
            3. View Auction Details & Place Bid
            4. My Bids
            5. Create New Auction
            6. My Auctions""");
        if(currentUser.getRole() == UserRole.ADMIN) {
            System.out.println("""
                
                --- ADMIN FUNCTIONS ---
                7. View All Users
                8. View All Auctions
                9. Remove Auction
                10. System Statistics""");
        }
        System.out.println("\n0. Logout");
        System.out.print("\nChoose option: ");
        
        int choice = getIntInput();
        
        switch(choice) {
            case 1 -> browseAuctions();
            case 2 -> searchAuctions();
            case 3 -> viewAuctionDetailsAndBid();
            case 4 -> viewMyBids();
            case 5 -> createAuction();
            case 6 -> viewMyAuctions();
            case 7 -> { if(currentUser instanceof Admin) ((Admin) currentUser).viewAllUsers(); }
            case 8 -> { if(currentUser instanceof Admin) ((Admin) currentUser).viewAllAuctions(); }
            case 9 -> { if(currentUser instanceof Admin) removeAuction(); }
            case 10 -> { if(currentUser instanceof Admin) manager.printStatistics(); }
            case 0 -> logout();
            default -> System.out.println("❌ Invalid option");
        }
    }
    
    private void browseAuctions() {
        List<AuctionItem> activeAuctions = manager.getActiveAuctions();
        if (activeAuctions.isEmpty()) {
            System.out.println("\n⚠️  No active auctions at the moment.");
            return;
        }
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ACTIVE AUCTIONS (" + activeAuctions.size() + ")");
        System.out.println("=".repeat(60));
        for (int i=0; i<activeAuctions.size(); i++) {
            AuctionItem item = activeAuctions.get(i);
            System.out.printf("\n[%d] %s\n", i+1, item.getTitle());
            System.out.printf("    Category: %s | Current Bid: $%.2f\n", item.getCategory(), item.getCurrentBid());
            System.out.printf("    Time Remaining: %d minutes | %s\n", item.getTimeRemainingMinutes(),
                item.isReserveMet() ? "✅ Reserve Met" : "⚠️ Reserve Not Met");
        }
        System.out.println();
    }
    
    private void searchAuctions() {
        System.out.print("\nEnter search keyword: ");
        String keyword = scanner.nextLine().trim();
        List<AuctionItem> results = manager.searchAuctions(keyword);
        if(results.isEmpty()) {
            System.out.println("\n⚠️  No auctions found matching '" + keyword + "'");
            return;
        }
        System.out.println("\n=== SEARCH RESULTS (" + results.size() + ") ===");
        for(AuctionItem item : results) {
            System.out.printf("%s - $%.2f [%s] (ID: %s)\n",
                item.getTitle(), item.getCurrentBid(), item.getState(),
                item.getItemId().substring(0, 8));
        }
    }
    
    private void viewAuctionDetailsAndBid() {
        System.out.print("\nEnter Auction ID (first 8 characters): ");
        String idPrefix = scanner.nextLine().trim();
        AuctionItem item = manager.getAllAuctions().stream()
                .filter(a -> a.getItemId().startsWith(idPrefix))
                .findFirst().orElse(null);
        if(item == null) {
            System.out.println("❌ Auction not found");
            return;
        }
        System.out.println("\n" + "=".repeat(60));
        System.out.println("AUCTION DETAILS");
        System.out.println("=".repeat(60));
        System.out.println("Title: " + item.getTitle());
        System.out.println("Description: " + item.getDescription());
        System.out.println("Category: " + item.getCategory());
        System.out.printf("Current Bid: $%.2f\n", item.getCurrentBid());
        System.out.printf("Reserve Price: $%.2f [%s]\n", item.getReservePrice(), item.isReserveMet() ? "✅ Met" : "⚠️ Not Met");
        System.out.printf("Minimum Next Bid: $%.2f\n", item.getCurrentBid() + item.getMinimumBidIncrement());
        System.out.println("State: " + item.getState());
        System.out.println("Seller: " + item.getSellerUsername());
        System.out.println("Time Remaining: " + item.getTimeRemainingMinutes() + " minutes");
        System.out.println("\nBid History: " + item.getBidHistory().size() + " bid(s)");
        if(!item.getBidHistory().isEmpty()) {
            System.out.println("\nRecent Bids:");
            List<Bid> history = item.getBidHistory();
            int showCount = Math.min(5, history.size());
            for(int i = history.size() - 1; i >= history.size() - showCount; i--) {
                System.out.println("  " + history.get(i));
            }
        }
        if(currentUser !=null && item.getState() == AuctionState.ACTIVE) {
            System.out.print("\nPlace a bid? (y/n): ");
            String resp = scanner.nextLine().trim();
            if(resp.equalsIgnoreCase("y")) {
                System.out.print("Enter bid amount: $");
                double amount = getDoubleInput();
                item.placeBid(currentUser, amount);
            }
        } else if(currentUser == null) {
            System.out.println("\n⚠️  Please login to place bids.");
        }
    }
    
    private void viewMyBids() {
        List<String> myBidIds = currentUser.getMyBidIds();
        if(myBidIds.isEmpty()) {
            System.out.println("\n⚠️  You haven't placed any bids yet.");
            return;
        }
        System.out.println("\n=== MY BIDS (" + myBidIds.size() + ") ===");
        for(AuctionItem auction : manager.getAllAuctions()) {
            List<Bid> relevantBids = auction.getBidHistory().stream()
                .filter(bid -> myBidIds.contains(bid.getBidId()))
                .toList();
            if (!relevantBids.isEmpty()) {
                System.out.println("\n📦 " + auction.getTitle() + " [" + auction.getState() + "]");
                for(Bid bid : relevantBids) {
                    System.out.println("   " + bid);
                }
                System.out.printf("   Current Winning Bid: $%.2f\n", auction.getCurrentBid());
            }
        }
    }
    
    private void createAuction() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("CREATE NEW AUCTION");
        System.out.println("=".repeat(60));
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Description: ");
        String desc = scanner.nextLine().trim();
        System.out.print("Starting Price: $");
        double startPrice = getDoubleInput();
        System.out.print("Reserve Price: $");
        double reservePrice = getDoubleInput();
        System.out.print("Duration (minutes): ");
        int duration = getIntInput();
        System.out.println("\nAvailable Categories:");
        ItemCategory[] categories = ItemCategory.values();
        for(int i=0; i<categories.length; i++) {
            System.out.printf("[%d] %s\n", i+1, categories[i]);
        }
        System.out.print("Choose category (1-" + categories.length + "): ");
        int catChoice = getIntInput() - 1;
        if(catChoice < 0 || catChoice >= categories.length) {
            System.out.println("❌ Invalid category");
            return;
        }
        String id = manager.createAuction(title, desc, startPrice, reservePrice, currentUser, categories[catChoice], duration);
        if(id != null) {
            System.out.println("\n✅ Auction created successfully!");
            System.out.println("Auction ID: " + id.substring(0, 8));
        }
    }
    
    private void viewMyAuctions() {
        List<AuctionItem> myAuctions = manager.getAuctionsBySeller(currentUser);
        if(myAuctions.isEmpty()) {
            System.out.println("\n⚠️  You haven't created any auctions yet.");
            return;
        }
        System.out.println("\n=== MY AUCTIONS (" + myAuctions.size() + ") ===");
        for(AuctionItem item : myAuctions) {
            System.out.printf("\n%s [%s]\n", item.getTitle(), item.getState());
            System.out.printf("  Current Bid: $%.2f | Bids: %d\n", item.getCurrentBid(), item.getBidHistory().size());
        }
    }
    
    private void removeAuction() {
        System.out.print("\nEnter Auction ID to remove: ");
        String idPrefix = scanner.nextLine().trim();
        AuctionItem item = manager.getAllAuctions().stream()
                .filter(a -> a.getItemId().startsWith(idPrefix))
                .findFirst().orElse(null);
        if(item != null) {
            ((Admin)currentUser).removeAuction(item.getItemId());
        } else {
            System.out.println("❌ Auction not found");
        }
    }
    
    private void logout() {
        System.out.println("\n👋 Goodbye, " + currentUser.getUsername() + "!");
        currentUser = null;
    }
    
    private void exitApplication() {
        System.out.print("\nSave data before exit? (y/n): ");
        String save = scanner.nextLine().trim();
        if(save.equalsIgnoreCase("y")) {
            System.out.println("💾 Saving data...");
            // Call FileManager save methods here if implemented
        }
        manager.shutdown();
        System.out.println("\n👋 Thank you for using Online Auction System!");
        System.exit(0);
    }
    
    private int getIntInput() {
        while(true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch(NumberFormatException e) {
                System.out.print("❌ Invalid input. Please enter a number: ");
            }
        }
    }
    
    private double getDoubleInput() {
        while(true) {
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch(NumberFormatException e) {
                System.out.print("❌ Invalid input. Please enter a number: ");
            }
        }
    }
}
