package com.auction.models;

import com.auction.enums.UserRole;
import com.auction.observers.BidObserver;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

/**
 * User class representing a user in the auction system
 * Implements BidObserver to receive auction notifications
 */
public class User implements BidObserver, Serializable {
    private static final long serialVersionUID = 1L;
    
    private String userId;
    private String username;
    private String passwordHash;
    private String salt;
    private String email;
    private UserRole role;
    private List<String> myBidIds;
    
    public User(String username, String password, String email, UserRole role) {
        this.userId = UUID.randomUUID().toString();
        this.username = username;
        this.email = email;
        this.role = role;
        this.myBidIds = new ArrayList<>();
        
        // Generate salt and hash password
        this.salt = generateSalt();
        this.passwordHash = hashPassword(password, this.salt);
    }
    
    /**
     * Generate a random salt for password hashing
     */
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Hash password with salt using SHA-256
     */
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Verify if the provided password matches the stored hash
     */
    public boolean verifyPassword(String password) {
        String hashedAttempt = hashPassword(password, this.salt);
        return this.passwordHash.equals(hashedAttempt);
    }
    
    /**
     * Add a bid ID to the user's bid history
     */
    public void addBidId(String bidId) {
        myBidIds.add(bidId);
    }
    
    // Observer pattern implementation
    @Override
    public void onBidPlaced(AuctionItem item, Bid newBid) {
        System.out.println("[NOTIFICATION] New bid of $" + String.format("%.2f", newBid.getAmount()) + 
                         " placed on: " + item.getTitle());
    }
    
    @Override
    public void onAuctionEnded(AuctionItem item, User winner) {
        if (winner != null && winner.getUserId().equals(this.userId)) {
            System.out.println("\n*** CONGRATULATIONS! You won the auction: " + item.getTitle() + 
                             " for $" + String.format("%.2f", item.getCurrentBid()) + " ***\n");
        } else if (myBidIds.stream().anyMatch(bidId -> 
                    item.getBidHistory().stream().anyMatch(b -> b.getBidId().equals(bidId)))) {
            System.out.println("[NOTIFICATION] Auction ended for: " + item.getTitle());
        }
    }
    
    @Override
    public void onOutbid(AuctionItem item, Bid newBid) {
        System.out.println("\n[ALERT] You have been outbid on: " + item.getTitle() + 
                         " | New bid: $" + String.format("%.2f", newBid.getAmount()) + "\n");
    }
    
    // Getters
    public String getUserId() {
        return userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public String getEmail() {
        return email;
    }
    
    public List<String> getMyBidIds() {
        return new ArrayList<>(myBidIds);
    }
    
    // For persistence - internal use only
    protected String getSalt() {
        return salt;
    }
    
    protected String getPasswordHash() {
        return passwordHash;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId.equals(user.userId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
    
    @Override
    public String toString() {
        return String.format("User[%s - %s (%s)]", username, email, role);
    }
}
