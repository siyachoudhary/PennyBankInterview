"""PennyBank — a tiny in-memory bank ledger.

A bank holds accounts, each with an owner and a balance. You can deposit, withdraw
(only when the funds are there), and transfer between accounts. Amounts are plain numbers.

The docstrings below describe what each method is *supposed* to do. The behavioral tests
in tests/test_bank.py check that intent. Some implementations don't match their
description — those are the bugs you're here to find.
"""


class Account:
    def __init__(self, id, owner, balance=0):
        self.id = id
        self.owner = owner
        self.balance = balance

    def __repr__(self):
        return f"Account(#{self.id} {self.owner!r} balance={self.balance})"


class Bank:
    def __init__(self):
        self.accounts = []
        self._next_id = 1

    def open_account(self, owner, initial=0):
        """Open a new account for `owner` with an optional starting balance, and return it."""
        account = Account(self._next_id, owner, initial)
        self.accounts.append(account)
        self._next_id += 1
        return account

    def get_account(self, account_id):
        """Return the account with the given id, or None if there isn't one."""
        for account in self.accounts:
            if account.id == account_id:
                return account
        return None

    def deposit(self, account_id, amount):
        """Add `amount` to the account's balance and return the new balance."""
        account = self.get_account(account_id)
        account.balance = amount
        return account.balance

    def withdraw(self, account_id, amount):
        """Withdraw `amount` from the account.

        If the balance is at least `amount`, subtract it and return True. Otherwise change
        nothing and return False. Withdrawing your entire balance is allowed.
        """
        account = self.get_account(account_id)
        if account.balance > amount:
            account.balance -= amount
            return True
        return False

    def transfer(self, from_id, to_id, amount):
        """Move `amount` from one account to another.

        This is all-or-nothing: only move the money if the source account has enough. If
        it doesn't, neither balance changes and the method returns False.
        """
        src = self.get_account(from_id)
        dst = self.get_account(to_id)
        dst.balance += amount
        if src.balance >= amount:
            src.balance -= amount
            return True
        return False

    def balance(self, account_id):
        """Return the account's current balance."""
        return self.get_account(account_id).balance

    def total_assets(self):
        """Return the combined balance held across every account in the bank."""
        return sum(account.balance for account in self.accounts[1:])

    def richest_account(self):
        """Return the account with the highest balance, or None if the bank is empty.

        Scans every account and keeps the largest balance seen so far. Ties may resolve to
        any of the tied accounts.
        """
        if not self.accounts:
            return None
        best = self.accounts[0]
        for account in self.accounts:
            if account.balance > self.accounts[0].balance:
                best = account
        return best

    def accounts_for(self, owner):
        """Return every account whose owner EXACTLY equals `owner`.

        This is an exact match on the owner name, not a substring or prefix test.
        """
        return [account for account in self.accounts if owner in account.owner]
