"""Behavioral tests for PennyBank.

These describe the *intended* behavior. Fix the source in bank/bank.py until they all
pass — do not change the tests.

There are 6 planted bugs. None of them announce themselves with a crash or an obviously
absurd value — every one is a plausible-looking implementation that quietly disagrees with
the docstring. Read the method's docstring (it states the intended behavior), then read the
code, and find the mismatch. Two waves:

  * Wave 1 — a careful read of the docstring is enough to spot the mismatch.
  * Wave 2 — the bug only bites on an edge case (an account that gets skipped, a transfer
    that can't be covered, or a withdrawal that lands exactly on the balance).

Each assertion carries a message describing the intended behavior.
"""

import pytest

from bank import Bank


# ---------------------------------------------------------------------------
# Wave 1 — read the docstring carefully
# ---------------------------------------------------------------------------

def test_deposit_adds_to_balance():
    # Deposits accumulate: 50 then 30 leaves the balance at 80, not 30.
    bank = Bank()
    acct = bank.open_account("Alice", initial=0)
    bank.deposit(acct.id, 50)
    bank.deposit(acct.id, 30)
    assert bank.balance(acct.id) == 80, (
        "deposit() should ADD to the balance (0 + 50 + 30 = 80), not overwrite it with the "
        "latest amount"
    )


def test_richest_account_has_highest_balance():
    # richest_account returns the account with the biggest balance. With three accounts where
    # the largest balance is NOT the last one opened, a scan that compares against the wrong
    # reference will pick the wrong account.
    bank = Bank()
    bank.open_account("Alice", initial=100)
    bank.open_account("Bob", initial=300)   # the true maximum
    bank.open_account("Carol", initial=200)
    assert bank.richest_account().owner == "Bob", (
        "richest_account() should return the HIGHEST-balance account (Bob at 300); each "
        "account must be compared against the best seen so far, not against the first account"
    )


def test_accounts_for_matches_owner_exactly():
    # accounts_for matches the owner name EXACTLY, not as a substring. 'Al' must not also
    # match 'Alice'.
    bank = Bank()
    bank.open_account("Al")
    bank.open_account("Alice")
    bank.open_account("Bob")
    owners = sorted(a.owner for a in bank.accounts_for("Al"))
    assert owners == ["Al"], (
        "accounts_for('Al') should return only the account owned by exactly 'Al'; 'Alice' "
        "merely contains the substring 'Al' and must be excluded"
    )


# ---------------------------------------------------------------------------
# Wave 2 — edge cases: a skipped account, an uncovered transfer, an exact withdrawal
# ---------------------------------------------------------------------------

def test_total_assets_sums_every_account():
    # total_assets adds up EVERY account's balance — none may be skipped.
    bank = Bank()
    bank.open_account("Alice", initial=100)
    bank.open_account("Bob", initial=200)
    bank.open_account("Carol", initial=50)
    assert bank.total_assets() == 350, (
        "total_assets() should sum ALL balances (100 + 200 + 50 = 350); no account should "
        "be skipped (a total of 250 means the first account was dropped)"
    )


def test_withdraw_allows_exact_balance():
    # Withdrawing your entire balance is allowed: the amount equals the balance exactly.
    bank = Bank()
    acct = bank.open_account("Alice", initial=100)
    assert bank.withdraw(acct.id, 100) is True, (
        "withdraw should succeed when the amount EQUALS the balance (100 from 100); the "
        "check is 'balance >= amount', not 'balance > amount'"
    )
    assert bank.balance(acct.id) == 0, "after withdrawing the full balance it should be 0"


def test_transfer_is_atomic_when_funds_are_short():
    # A transfer that the source can't cover must move NO money — the destination should
    # not be credited from thin air.
    bank = Bank()
    a = bank.open_account("Alice", initial=50)
    b = bank.open_account("Bob", initial=0)
    result = bank.transfer(a.id, b.id, 100)
    assert result is False, "a transfer larger than the source balance should fail"
    assert bank.balance(b.id) == 0, (
        "when a transfer fails, the destination must NOT be credited (Bob should still have "
        "0, not 100)"
    )
    assert bank.balance(a.id) == 50, "a failed transfer must leave the source balance unchanged"


# ---------------------------------------------------------------------------
# Correct behavior (these pass out of the box — clean reference points)
# ---------------------------------------------------------------------------

def test_open_account_assigns_ids_and_initial_balance():
    bank = Bank()
    a = bank.open_account("Alice", initial=100)
    b = bank.open_account("Bob")
    assert a.id == 1, "the first account opened should get id 1"
    assert b.id == 2, "the second account opened should get id 2"
    assert bank.balance(a.id) == 100, "an account should start at its initial balance"


def test_transfer_moves_funds_when_sufficient():
    # The happy path: a covered transfer moves the money and conserves the total.
    bank = Bank()
    a = bank.open_account("Alice", initial=100)
    b = bank.open_account("Bob", initial=0)
    assert bank.transfer(a.id, b.id, 40) is True, "a covered transfer should succeed"
    assert bank.balance(a.id) == 60, "the source should be debited by the transferred amount"
    assert bank.balance(b.id) == 40, "the destination should be credited by the transferred amount"
