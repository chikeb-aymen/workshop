package com.workshop.after.user.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String email;
    private String displayName;
    private String phone;
    private String address;

    @Version                       // JPA optimistic locking
    private long version;

    // getters / setters
    public String getId()          { return id; }
    public String getEmail()       { return email; }
    public String getDisplayName() { return displayName; }
    public String getPhone()       { return phone; }
    public String getAddress()     { return address; }
    public long   getVersion()     { return version; }

    public void setEmail(String email)           { this.email = email; }
    public void setDisplayName(String name)      { this.displayName = name; }
    public void setPhone(String phone)           { this.phone = phone; }
    public void setAddress(String address)       { this.address = address; }
}
