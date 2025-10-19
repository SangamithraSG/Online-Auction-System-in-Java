package com.auction.managers;

import com.auction.models.*;

import java.io.*;
import java.util.*;

/**
 * FileManager handles data persistence using Java serialization
 */
public class FileManager {
    private static final String USERS_FILE = "data/users.dat";
    private static final String AUCTIONS_FILE = "data/auctions.dat";
    
    /**
     * Save all users to file
     */
    public static void saveUsers(Map<String, User> users) {
        createDataDirectory();
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
            System.out.println("✅ Users saved successfully to " + USERS_FILE);
        } catch (IOException e) {
            System.err.println("❌ Error saving users: " + e.getMessage());
        }
    }
    
    /**
     * Load users from file
     */
    @SuppressWarnings("unchecked")
    public static Map<String, User> loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            System.out.println("No saved users file found. Starting fresh.");
            return new HashMap<>();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(USERS_FILE))) {
            Map<String, User> users = (Map<String, User>) ois.readObject();
            System.out.println("✅ Loaded " + users.size() + " users from file");
            return users;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Error loading users: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * Save all auctions to file
     */
    public static void saveAuctions(Map<String, AuctionItem> auctions) {
        createDataDirectory();
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(AUCTIONS_FILE))) {
            oos.writeObject(auctions);
            System.out.println("✅ Auctions saved successfully to " + AUCTIONS_FILE);
        } catch (IOException e) {
            System.err.println("❌ Error saving auctions: " + e.getMessage());
        }
    }
    
    /**
     * Load auctions from file
     */
    @SuppressWarnings("unchecked")
    public static Map<String, AuctionItem> loadAuctions() {
        File file = new File(AUCTIONS_FILE);
        if (!file.exists()) {
            System.out.println("No saved auctions file found. Starting fresh.");
            return new HashMap<>();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(AUCTIONS_FILE))) {
            Map<String, AuctionItem> auctions = (Map<String, AuctionItem>) ois.readObject();
            System.out.println("✅ Loaded " + auctions.size() + " auctions from file");
            return auctions;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Error loading auctions: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * Create data directory if it doesn't exist
     */
    private static void createDataDirectory() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }
    }
    
    /**
     * Export auction data to CSV (for reporting)
     */
    public static void exportAuctionsToCSV(List<AuctionItem> auctions, String filename) {
        createDataDirectory();
        try (PrintWriter writer = new PrintWriter(new FileWriter("data/" + filename))) {
            writer.println("ItemID,Title,Description,CurrentBid,ReservePrice,State,Category,Seller");
            
            for (AuctionItem item : auctions) {
                writer.printf("%s,\"%s\",\"%s\",%.2f,%.2f,%s,%s,%s%n",
                    item.getItemId(),
                    item.getTitle().replace("\"", "\"\""),
                    item.getDescription().replace("\"", "\"\""),
                    item.getCurrentBid(),
                    item.getReservePrice(),
                    item.getState(),
                    item.getCategory(),
                    item.getSellerUsername());
            }
            System.out.println("✅ Exported " + auctions.size() + " auctions to data/" + filename);
        } catch (IOException e) {
            System.err.println("❌ Error exporting to CSV: " + e.getMessage());
        }
    }
}
