package com.jpmc.midascore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class TransactionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private UserRecord sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private UserRecord recipient;

    private float amount;

    // NEW FIELD for incentive
    private float incentive;

    private LocalDateTime created = LocalDateTime.now();

    public TransactionRecord() {}

    public TransactionRecord(UserRecord sender, UserRecord recipient, float amount, float incentive) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.incentive = incentive;
    }

    public Long getId() {
        return id;
    }
    public UserRecord getSender() {
        return sender;
    }
    public UserRecord getRecipient() {
        return recipient;
    }
    public float getAmount() {
        return amount;
    }
    public float getIncentive() {
        return incentive;
    }
    public LocalDateTime getCreated() {
        return created;
    }
    public void setSender(UserRecord sender) {
        this.sender = sender;
    }
    public void setRecipient(UserRecord recipient) {
        this.recipient = recipient;
    }
    public void setAmount(float amount) {
        this.amount = amount;
    }
    public void setIncentive(float incentive) {
        this.incentive = incentive;
    }

    @Override
    public String toString() {
        return "TransactionRecord{id=%d, sender=%s, recipient=%s, amount=%f, incentive=%f, created=%s}"
                .formatted(id, sender, recipient, amount, incentive, created.toString());
    }
}
