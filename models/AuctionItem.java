package com.auction.models;

import com.auction.enums.AuctionState;
import com.auction.enums.ItemCategory;
import com.auction.observers.BidObserver;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * AuctionItem class representing an item being auctioned
 * Implements Observer pattern to notify bidders of changes
 */
public class AuctionItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final double DEFAULT_INCREMENT_PERCENTAGE = 0.05; // 5%
    
    private String itemId;
    private String title;
    private String description;
    private double startingPrice;
    private double reservePrice;
    private double currentBid;
    private double minimumBidIncrement;
    private String sellerId;
    private String sellerUsername;
    private ItemCategory category;
    private AuctionState state;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<Bid> bidHistory;
    private transient List<BidObserver> observers; // transient = not serialized
    private String currentHighestBidderId;
    
    public AuctionItem(String title, String description, double startingPrice,
                       double reservePrice, User seller, ItemCategory category,
                       LocalDateTime endTime) {
        this.itemId = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.startingPrice = startingPrice;
        this.reservePrice = reservePrice;
        this.currentBid = startingPrice;
        this.minimumBidIncrement = startingPrice * DEFAULT_INCREMENT_PERCENTAGE;
        this.sellerId = seller.getUserId();
        this.sellerUsername = seller.getUsername();
        this.category = category;
        this.state = AuctionState.PENDING;
        this.endTime = endTime;
        this.bidHistory = new ArrayList<>();
        this.observers = new ArrayList<>();
    }
    
    /**
     * Observer pattern: Add an observer to receive notifications
     */
    public void addObserver(BidObserver observer) {
        if (observers == null) {
            observers = new ArrayList<>();
        }
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    /**
     * Observer pattern: Remove an observer
     */
    public void removeObserver(BidObserver observer) {
        if (observers != null) {
            observers.remove(observer);
        }
    }
    
    /**
     * Notify all observers that a bid was placed
     */
    private void notifyBidPlaced(Bid bid) {
        if (observers != null) {
            for (BidObserver observer : observers) {
                observer.onBidPlaced(this, bid);
            }
        }
    }
    
    /**
     * Notify a specific user they were outbid
     */
    private void notifyOutbid(User previousBidder, Bid newBid) {
        previousBidder.onOutbid(this, newBid);
    }
    
    /**
     * Notify all observers that the auction ended
     */
    private void notifyAuctionEnded(User winner) {
        if (observers != null) {
            for (BidObserver observer : observers) {
                observer.onAuctionEnded(this, winner);
            }
        }
    }
    
    /**
     * Place a bid on this auction item
     * Returns true if bid was accepted, false otherwise
     */
    public synchronized boolean placeBid(User bidder, double amount) {
        // Validation 1: Check auction is active
        if (state != AuctionState.ACTIVE) {
            System.out.println("❌ Bid rejected: Auction is " + state.getDescription());
            return false;
        }
        
        // Validation 2: Prevent seller from bidding on own item
        if (bidder.getUserId().equals(sellerId)) {
            System.out.println("❌ Bid rejected: Seller cannot bid on their own item");
            return false;
        }
        
        // Validation 3: Check bid meets minimum increment
        double minimumBid = currentBid + minimumBidIncrement;
        if (amount < minimumBid) {
            System.out.printf("❌ Bid rejected: Minimum bid is $%.2f (current bid + $%.2f increment)\n", 
                minimumBid, minimumBidIncrement);
            return false;
        }
        
        // Validation 4: Check auction hasn't ended
        if (LocalDateTime.now().isAfter(endTime)) {
            endAuction();
            System.out.println("❌ Bid rejected: Auction has already ended");
            return false;
        }
        
        // Create and record the bid
        Bid newBid = new Bid(bidder, amount, this.itemId);
        bidHistory.add(newBid);
        
        // Get previous highest bidder for notification
        User previousBidder = null;
        if (currentHighestBidderId != null && !currentHighestBidderId.equals(bidder.getUserId())) {
            // notify previous highest bidder if user object is available
            if (observers != null) {
                for (BidObserver obs : observers) {
                    if (obs instanceof User) {
                        User user = (User) obs;
                        if (user.getUserId().equals(currentHighestBidderId)) {
                            previousBidder = user;
                            break;
                        }
                    }
                }
            }
        }
        
        // Notify previous bidder they've been outbid
        if (previousBidder != null) {
            notifyOutbid(previousBidder, newBid);
        }
        
        // Update current bid and bidder
        currentBid = amount;
        currentHighestBidderId = bidder.getUserId();
        bidder.addBidId(newBid.getBidId());
        
        // Add bidder as observer if not already added
        addObserver(bidder);
        
        // Notify all observers
        notifyBidPlaced(newBid);
        
        System.out.printf("✅ Bid accepted: $%.2f by %s on '%s'\n", 
            amount, bidder.getUsername(), title);
        return true;
    }
    
    /**
     * Start the auction
     */
    public void startAuction() {
        if (state == AuctionState.PENDING) {
            state = AuctionState.ACTIVE;
            startTime = LocalDateTime.now();
            System.out.println("🔔 Auction started: " + title);
        }
    }
    
    /**
     * End the auction and determine winner
     */
    public void endAuction() {
        if (state == AuctionState.ACTIVE) {
            state = AuctionState.CLOSED;
            
            User winner = null;
            
            // Check if reserve price is met
            if (currentBid >= reservePrice && currentHighestBidderId != null) {
                System.out.printf("\n🎉 Auction ended: '%s' SOLD for $%.2f\n", title, currentBid);
                System.out.println("   Winner ID: " + currentHighestBidderId);
                
                // Notify winner from observers list if available
                if (observers != null) {
                    for (BidObserver obs : observers) {
                        if (obs instanceof User) {
                            User user = (User) obs;
                            if (user.getUserId().equals(currentHighestBidderId)) {
                                winner = user;
                                break;
                            }
                        }
                    }
                }
            } else {
                System.out.printf("\n⚠️  Auction ended: '%s' - Reserve price NOT met (needed $%.2f)\n", 
                    title, reservePrice);
            }
            
            // Notify all observers
            notifyAuctionEnded(winner);
        }
    }
    
    /**
     * Check if reserve price has been met
     */
    public boolean isReserveMet() {
        return currentBid >= reservePrice;
    }
    
    /**
     * Get bid history (defensive copy)
     */
    public List<Bid> getBidHistory() {
        return new ArrayList<>(bidHistory);
    }
    
    /**
     * Get time remaining in minutes
     */
    public long getTimeRemainingMinutes() {
        if (state != AuctionState.ACTIVE) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endTime)) {
            return 0;
        }
        return java.time.Duration.between(now, endTime).toMinutes();
    }
    
    // Getters
    public String getItemId() {
        return itemId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public double getCurrentBid() {
        return currentBid;
    }
    
    public double getReservePrice() {
        return reservePrice;
    }
    
    public double getStartingPrice() {
        return startingPrice;
    }
    
    public double getMinimumBidIncrement() {
        return minimumBidIncrement;
    }
    
    public AuctionState getState() {
        return state;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public String getSellerId() {
        return sellerId;
    }
    
    public String getSellerUsername() {
        return sellerUsername;
    }
    
    public ItemCategory getCategory() {
        return category;
    }
    
    public String getCurrentHighestBidderId() {
        return currentHighestBidderId;
    }
    
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return String.format("Auction[%s | Current: $%.2f | Reserve: $%.2f | State: %s | Ends: %s]",
            title, currentBid, reservePrice, state, endTime.format(formatter));
    }
}
