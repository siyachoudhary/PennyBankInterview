"""Behavioral tests for PennyBank.

These describe the *intended* behavior. Fix the source in bank/bank.py until they all
pass — do not change the tests.

There are 6 planted bugs: 4 are easy to spot from a single failing test, and 2 are subtler
(they only bite on an edge case). Each assertion carries a message describing the intended
behavior, so a failure tells you what the method should do — not just how two values
differ.
"""

import pytest

from bank import Bank


# ---------------------------------------------------------------------------
# The 4 easier bugs
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
    # richest_account returns the account with the biggest balance.
    bank = Bank()
    bank.open_account("Alice", initial=100)
    bank.open_account("Bob", initial=300)
    assert bank.richest_account().owner == "Bob", (
        "richest_account() should return the HIGHEST-balance account (Bob at 300), not the "
        "lowest"
    )


def test_accounts_for_returns_owners_accounts():
    # accounts_for returns the accounts that belong TO that owner.
    bank = Bank()
    bank.open_account("Alice")
    bank.open_account("Bob")
    owners = [a.owner for a in bank.accounts_for("Alice")]
    assert owners == ["Alice"], (
        "accounts_for('Alice') should return Alice's accounts, not everyone else's"
    )


def test_total_assets_sums_every_account():
    # total_assets adds up EVERY account's balance.
    bank = Bank()
    bank.open_account("Alice", initial=100)
    bank.open_account("Bob", initial=200)
    bank.open_account("Carol", initial=50)
    assert bank.total_assets() == 350, (
        "total_assets() should sum ALL balances (100 + 200 + 50 = 350); no account should "
        "be skipped"
    )


# ---------------------------------------------------------------------------
# The 2 harder bugs (edge cases)
# ---------------------------------------------------------------------------

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
# Correct behavior (kept as clean reference points)
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
