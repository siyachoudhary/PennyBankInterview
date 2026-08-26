package com.example.bank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Behavioral tests for PennyBank. These describe the *intended* behavior.
 * Fix the source in Bank.java until they all pass — do not change the tests.
 *
 * There are 6 planted bugs: 4 easy to spot from a single failing test, and 2 subtler ones
 * that only bite on an edge case. Each assertion carries a message describing the intent.
 */
class BankTest {

    // -----------------------------------------------------------------------
    // The 4 easier bugs
    // -----------------------------------------------------------------------

    @Test
    void depositAddsToBalance() {
        // Deposits accumulate: 50 then 30 leaves 80, not 30.
        Bank bank = new Bank();
        Account acct = bank.openAccount("Alice", 0);
        bank.deposit(acct.getId(), 50);
        bank.deposit(acct.getId(), 30);
        assertEquals(80, bank.balance(acct.getId()),
                "deposit() should ADD to the balance (0 + 50 + 30 = 80), not overwrite it");
    }

    @Test
    void richestAccountHasHighestBalance() {
        Bank bank = new Bank();
        bank.openAccount("Alice", 100);
        bank.openAccount("Bob", 300);
        assertEquals("Bob", bank.richestAccount().getOwner(),
                "richestAccount() should return the HIGHEST-balance account (Bob at 300), not the lowest");
    }

    @Test
    void accountsForReturnsOwnersAccounts() {
        Bank bank = new Bank();
        bank.openAccount("Alice");
        bank.openAccount("Bob");
        List<String> owners = bank.accountsFor("Alice").stream()
                .map(Account::getOwner)
                .collect(Collectors.toList());
        assertEquals(List.of("Alice"), owners,
                "accountsFor('Alice') should return Alice's accounts, not everyone else's");
    }

    @Test
    void totalAssetsSumsEveryAccount() {
        Bank bank = new Bank();
        bank.openAccount("Alice", 100);
        bank.openAccount("Bob", 200);
        bank.openAccount("Carol", 50);
        assertEquals(350, bank.totalAssets(),
                "totalAssets() should sum ALL balances (100 + 200 + 50 = 350); no account should be skipped");
    }

    // -----------------------------------------------------------------------
    // The 2 harder bugs (edge cases)
    // -----------------------------------------------------------------------

    @Test
    void withdrawAllowsExactBalance() {
        // Withdrawing your entire balance is allowed: amount equals balance exactly.
        Bank bank = new Bank();
        Account acct = bank.openAccount("Alice", 100);
        assertTrue(bank.withdraw(acct.getId(), 100),
                "withdraw should succeed when the amount EQUALS the balance (100 from 100): use >=, not >");
        assertEquals(0, bank.balance(acct.getId()), "after withdrawing the full balance it should be 0");
    }

    @Test
    void transferIsAtomicWhenFundsAreShort() {
        // A transfer the source can't cover must move NO money.
        Bank bank = new Bank();
        Account a = bank.openAccount("Alice", 50);
        Account b = bank.openAccount("Bob", 0);
        assertFalse(bank.transfer(a.getId(), b.getId(), 100),
                "a transfer larger than the source balance should fail");
        assertEquals(0, bank.balance(b.getId()),
                "when a transfer fails, the destination must NOT be credited (Bob stays 0, not 100)");
        assertEquals(50, bank.balance(a.getId()),
                "a failed transfer must leave the source balance unchanged");
    }

    // -----------------------------------------------------------------------
    // Correct behavior (kept as clean reference points)
    // -----------------------------------------------------------------------

    @Test
    void openAccountAssignsIdsAndInitialBalance() {
        Bank bank = new Bank();
        Account a = bank.openAccount("Alice", 100);
        Account b = bank.openAccount("Bob");
        assertEquals(1, a.getId(), "the first account opened should get id 1");
        assertEquals(2, b.getId(), "the second account opened should get id 2");
        assertEquals(100, bank.balance(a.getId()), "an account should start at its initial balance");
    }

    @Test
    void transferMovesFundsWhenSufficient() {
        // The happy path: a covered transfer moves the money and conserves the total.
        Bank bank = new Bank();
        Account a = bank.openAccount("Alice", 100);
        Account b = bank.openAccount("Bob", 0);
        assertTrue(bank.transfer(a.getId(), b.getId(), 40), "a covered transfer should succeed");
        assertEquals(60, bank.balance(a.getId()), "the source should be debited by the transferred amount");
        assertEquals(40, bank.balance(b.getId()), "the destination should be credited by the transferred amount");
    }
}
