package com.auction.observers;

import com.auction.models.AuctionItem;
import com.auction.models.Bid;
import com.auction.models.User;

/**
 * Observer interface for receiving auction notifications
 */
public interface BidObserver {
    /**
     * Called when a new bid is placed on an auction
     */
    void onBidPlaced(AuctionItem item, Bid newBid);
    
    /**
     * Called when an auction ends
     */
    void onAuctionEnded(AuctionItem item, User winner);
    
    /**
     * Called when a user is outbid
     */
    void onOutbid(AuctionItem item, Bid newBid);
}
