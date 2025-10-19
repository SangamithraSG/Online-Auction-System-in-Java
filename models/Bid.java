package com.auction.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Immutable class representing a bid in an auction
 */
public class Bid implements Comparable<Bid>, Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String bidId;
    private final String bidderId;
    private final String bidderUsername;
    private final double amount;
    private final LocalDateTime timestamp;
    private final String auctionItemId;
    
    public Bid(User bidder, double amount, String auctionItemId) {
        this.bidId = UUID.randomUUID().toString();
        this.bidderId = bidder.getUserId();
        this.bidderUsername = bidder.getUsername();
        this.amount = amount;
        this.auctionItemId = auctionItemId;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public String getBidId() {
        return bidId;
    }
    
    public String getBidderId() {
        return bidderId;
    }
    
    public String getBidderUsername() {
        return bidderUsername;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getAuctionItemId() {
        return auctionItemId;
    }
    
    @Override
    public int compareTo(Bid other) {
        return Double.compare(this.amount, other.amount);
    }
    
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("Bid[%s: $%.2f by %s at %s]", 
            bidId.substring(0, 8), amount, bidderUsername, timestamp.format(formatter));
    }
}
