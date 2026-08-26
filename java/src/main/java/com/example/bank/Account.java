package com.example.bank;

/** A bank account with an owner and a balance. */
public class Account {
    private final int id;
    private final String owner;
    private int balance;

    public Account(int id, String owner, int balance) {
        this.id = id;
        this.owner = owner;
        this.balance = balance;
    }

    public int getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return String.format("Account(#%d %s balance=%d)", id, owner, balance);
    }
}
