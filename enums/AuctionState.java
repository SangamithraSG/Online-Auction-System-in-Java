package com.auction.enums;

/**
 * Enum representing the different states of an auction
 */
public enum AuctionState {
    PENDING("Auction not yet started"),
    ACTIVE("Auction is live and accepting bids"),
    CLOSED("Auction has ended");
    
    private final String description;
    
    AuctionState(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
