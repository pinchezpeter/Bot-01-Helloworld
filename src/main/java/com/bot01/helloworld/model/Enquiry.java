package com.bot01.helloworld.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "enquiries")
public class Enquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerPhone;
    private String customerName;
    private String service;

    @Column(length = 2000)
    private String request;

    private String status;
    private LocalDateTime createdAt;

    public Enquiry() {
    }

    public Enquiry(String customerPhone, String customerName,
                   String service, String request, String status) {
        this.customerPhone = customerPhone;
        this.customerName = customerName;
        this.service = service;
        this.request = request;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }
   public String getCustomerPhone() {
      if (customerPhone == null) {
        return "";
    }

    // Convert Kenyan local format 07XXXXXXXX to 2547XXXXXXXX
    if (customerPhone.startsWith("0")) {
        return "254" + customerPhone.substring(1);
    }

    // Remove + if the number is already international
    if (customerPhone.startsWith("+")) {
        return customerPhone.substring(1);
    }

    return customerPhone;
}
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getStatus() {
        return status;
    }

public LocalDateTime getCreatedAt() {
    return createdAt;
}
    public void setStatus(String status) {
        this.status = status;
    }
}
