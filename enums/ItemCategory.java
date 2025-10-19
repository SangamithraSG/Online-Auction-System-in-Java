package com.auction.enums;

/**
 * Enum representing different categories of auction items
 */
public enum ItemCategory {
    ELECTRONICS("Electronic devices and gadgets"),
    ART("Artwork and paintings"),
    COLLECTIBLES("Collectible items"),
    FASHION("Clothing and accessories"),
    HOME("Home and furniture"),
    OTHER("Other items");
    
    private final String description;
    
    ItemCategory(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
