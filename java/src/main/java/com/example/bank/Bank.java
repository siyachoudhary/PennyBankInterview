package com.example.bank;

import java.util.ArrayList;
import java.util.List;

/**
 * PennyBank — a tiny in-memory bank ledger.
 *
 * The Javadoc on each method describes what it is *supposed* to do. The behavioral tests
 * in BankTest check that intent. Some implementations don't match their description —
 * those are the bugs you're here to find.
 */
public class Bank {

    private final List<Account> accounts = new ArrayList<>();
    private int nextId = 1;

    /** Open a new account for `owner` with an optional starting balance, and return it. */
    public Account openAccount(String owner, int initial) {
        Account account = new Account(nextId, owner, initial);
        accounts.add(account);
        nextId++;
        return account;
    }

    public Account openAccount(String owner) {
        return openAccount(owner, 0);
    }

    /** Return the account with the given id, or null if there isn't one. */
    public Account getAccount(int accountId) {
        for (Account account : accounts) {
            if (account.getId() == accountId) {
                return account;
            }
        }
        return null;
    }

    /** Add `amount` to the account's balance and return the new balance. */
    public int deposit(int accountId, int amount) {
        Account account = getAccount(accountId);
        account.setBalance(amount);
        return account.getBalance();
    }

    /**
     * Withdraw `amount` from the account. If the balance is at least `amount`, subtract it
     * and return true. Otherwise change nothing and return false. Withdrawing your entire
     * balance is allowed.
     */
    public boolean withdraw(int accountId, int amount) {
        Account account = getAccount(accountId);
        if (account.getBalance() > amount) {
            account.setBalance(account.getBalance() - amount);
            return true;
        }
        return false;
    }

    /**
     * Move `amount` from one account to another. This is all-or-nothing: only move the
     * money if the source account has enough. If it doesn't, neither balance changes and
     * the method returns false.
     */
    public boolean transfer(int fromId, int toId, int amount) {
        Account src = getAccount(fromId);
        Account dst = getAccount(toId);
        dst.setBalance(dst.getBalance() + amount);
        if (src.getBalance() >= amount) {
            src.setBalance(src.getBalance() - amount);
            return true;
        }
        return false;
    }

    /** Return the account's current balance. */
    public int balance(int accountId) {
        return getAccount(accountId).getBalance();
    }

    /** Return the combined balance held across every account in the bank. */
    public int totalAssets() {
        int sum = 0;
        for (int i = 1; i < accounts.size(); i++) {
            sum += accounts.get(i).getBalance();
        }
        return sum;
    }

    /**
     * Return the account with the highest balance, or null if the bank is empty. Scans every
     * account and keeps the largest balance seen so far. Ties may resolve to any of the tied
     * accounts.
     */
    public Account richestAccount() {
        if (accounts.isEmpty()) {
            return null;
        }
        Account best = accounts.get(0);
        for (Account account : accounts) {
            if (account.getBalance() > accounts.get(0).getBalance()) {
                best = account;
            }
        }
        return best;
    }

    /**
     * Return every account whose owner EXACTLY equals `owner`. This is an exact match on the
     * owner name, not a substring or prefix test.
     */
    public List<Account> accountsFor(String owner) {
        List<Account> result = new ArrayList<>();
        for (Account account : accounts) {
            if (account.getOwner().contains(owner)) {
                result.add(account);
            }
        }
        return result;
    }
}
