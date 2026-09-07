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
 * There are 8 planted bugs. None of them announce themselves with a crash or an obviously
 * absurd value — every one is a plausible-looking implementation that quietly disagrees with
 * the Javadoc. Read the method's Javadoc (it states the intended behavior), then read the
 * code, and find the mismatch. Two waves:
 *
 *   - Wave 1: a careful read of the Javadoc is enough to spot the mismatch.
 *   - Wave 2: the bug only bites on an edge case (an account that gets skipped, a transfer
 *     that can't be covered, a withdrawal that lands exactly on the balance, an adjacent pair
 *     dropped during a bulk close, or a fee that drives a small balance negative).
 *
 * Each assertion carries a message describing the intent.
 */
class BankTest {

    // -----------------------------------------------------------------------
    // Wave 1 — read the Javadoc carefully
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
        // With three accounts where the largest balance is NOT the last one opened, a scan
        // that compares against the wrong reference will pick the wrong account.
        Bank bank = new Bank();
        bank.openAccount("Alice", 100);
        bank.openAccount("Bob", 300);   // the true maximum
        bank.openAccount("Carol", 200);
        assertEquals("Bob", bank.richestAccount().getOwner(),
                "richestAccount() should return the HIGHEST-balance account (Bob at 300); each account "
                        + "must be compared against the best seen so far, not against the first account");
    }

    @Test
    void accountsForMatchesOwnerExactly() {
        // accounts_for matches the owner name EXACTLY, not as a substring: 'Al' must not also
        // match 'Alice'.
        Bank bank = new Bank();
        bank.openAccount("Al");
        bank.openAccount("Alice");
        bank.openAccount("Bob");
        List<String> owners = bank.accountsFor("Al").stream()
                .map(Account::getOwner)
                .sorted()
                .collect(Collectors.toList());
        assertEquals(List.of("Al"), owners,
                "accountsFor('Al') should return only the account owned by exactly 'Al'; 'Alice' merely "
                        + "contains the substring 'Al' and must be excluded");
    }

    // -----------------------------------------------------------------------
    // Wave 2 — edge cases: a skipped account, an uncovered transfer, an exact withdrawal
    // -----------------------------------------------------------------------

    @Test
    void totalAssetsSumsEveryAccount() {
        Bank bank = new Bank();
        bank.openAccount("Alice", 100);
        bank.openAccount("Bob", 200);
        bank.openAccount("Carol", 50);
        assertEquals(350, bank.totalAssets(),
                "totalAssets() should sum ALL balances (100 + 200 + 50 = 350); no account should be "
                        + "skipped (a total of 250 means the first account was dropped)");
    }

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

    @Test
    void closeBelowDropsEveryLowAccount() {
        // close_below removes EVERY account under the cutoff. NOTE: the order these are
        // opened in is load-bearing — keep the two low-balance accounts adjacent.
        Bank bank = new Bank();
        bank.openAccount("Alice", 5);    // under 10
        bank.openAccount("Bob", 8);      // under 10, right after Alice
        bank.openAccount("Carol", 100);  // safe
        bank.closeBelow(10);
        List<String> owners = bank.getAccounts().stream()
                .map(Account::getOwner)
                .sorted()
                .collect(Collectors.toList());
        assertEquals(List.of("Carol"), owners,
                "closeBelow(10) should drop EVERY account under 10 (both Alice and Bob), leaving only Carol");
    }

    @Test
    void deductFeeLeavesAccountsThatCannotCoverUntouched() {
        // A flat fee is charged to everyone who can afford it. An account that cannot cover
        // the fee is left alone — a balance must never go negative. An account sitting
        // exactly at the fee pays it down to 0.
        Bank bank = new Bank();
        Account rich = bank.openAccount("Alice", 100);
        Account broke = bank.openAccount("Bob", 5);     // cannot cover a fee of 10
        Account exact = bank.openAccount("Carol", 10);  // covers it exactly, down to 0
        bank.deductFeeAll(10);
        assertEquals(90, bank.balance(rich.getId()), "an account that can cover the fee pays it (100 - 10 = 90)");
        assertEquals(5, bank.balance(broke.getId()),
                "an account that cannot cover the fee must be left untouched (Bob stays 5, never goes to -5)");
        assertEquals(0, bank.balance(exact.getId()), "an account with exactly the fee pays it down to 0");
    }

    // -----------------------------------------------------------------------
    // Correct behavior (these pass out of the box — clean reference points)
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
