package com.berryfi.portal.dto.billing;

/**
 * DTO for payment method data.
 */
public class PaymentMethodDto {
    private String id;
    private String type;
    private String last4;
    private int expiryMonth;
    private int expiryYear;
    private String brand;
    private boolean isDefault;

    public PaymentMethodDto() {}

    public PaymentMethodDto(String id, String type, String last4, int expiryMonth, int expiryYear, 
                           String brand, boolean isDefault) {
        this.id = id;
        this.type = type;
        this.last4 = last4;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.brand = brand;
        this.isDefault = isDefault;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLast4() { return last4; }
    public void setLast4(String last4) { this.last4 = last4; }

    public int getExpiryMonth() { return expiryMonth; }
    public void setExpiryMonth(int expiryMonth) { this.expiryMonth = expiryMonth; }

    public int getExpiryYear() { return expiryYear; }
    public void setExpiryYear(int expiryYear) { this.expiryYear = expiryYear; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
}
