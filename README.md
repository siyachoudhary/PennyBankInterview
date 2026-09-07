# PennyBank — Debugging Technical Interview

Welcome! This is a **timeboxed (~60 minute)** technical interview built around a small
banking library called **PennyBank**. There are two identical implementations in the same
repo — **Python** and **Java** — so pick whichever language you're most comfortable in.

The interview is really **one main task with an optional bonus**:

1. **Debugging (the whole interview)** — The library ships with a failing test suite. Eight
   bugs have been planted. Find and fix them until the tests are green. None of them are
   one-liners that scream at you — they're the kind of plausible-looking code that quietly
   does the wrong thing, so take your time and reason carefully.
2. **Add a Feature (extra credit)** — *Only if you finish the debugging comfortably early*
   (roughly, all tests green in under 30 minutes) we'll spend the remaining time adding a
   small feature together. This is a bonus, not a requirement — a thorough, well-narrated
   debugging pass is the main thing we're evaluating.

We're not looking for perfection. We want to see how you read unfamiliar code, form
hypotheses, verify them, and communicate as you go. **Think out loud.**

---

## What is PennyBank?

A tiny in-memory bank ledger. A `Bank` holds accounts, each with an owner and a balance.
You can open accounts, deposit, withdraw (only when the funds are there), transfer money
between accounts, look up balances, find the richest account, total the bank's assets, close
out low-balance accounts, and charge a flat fee across the board.

The two implementations behave identically — same classes, same methods, same bugs.

---

## Setup

### Prerequisites

| Language | Needs |
|----------|-------|
| Python   | Python 3.9+ and `pytest` (`pip install pytest`) |
| Java     | JDK 17+ and Maven 3.8+ |

### Python

```bash
cd python
python -m pip install pytest        # once
python -m pytest -v                 # run the tests
```

### Java

```bash
cd java
mvn test                            # compiles and runs the tests
```

> Tip: run a single test while iterating.
> - Python: `python -m pytest tests/test_bank.py::test_transfer_is_atomic_when_funds_are_short -v`
> - Java: `mvn -Dtest=BankTest#transferIsAtomicWhenFundsAreShort test`

---

## Part 1 — Debugging (the main task)

1. Run the test suite. You should see multiple failures.
2. Read the failing tests to understand the *intended* behavior (each assertion has a
   message describing it).
3. Open the source (`bank/bank.py` or `src/main/java/com/example/bank/Bank.java`) — each
   method's docstring/Javadoc states what it should do — and fix the bugs.
4. Re-run until everything is green.

There are **eight** planted bugs, and **none of them are loud** — there are no crashes or
wildly-wrong values to point the way. Each is a plausible implementation that quietly
disagrees with the method's docstring: think a running-maximum that compares against the
wrong reference, an exact-match that's secretly a substring test, an accumulator that skips
an account, a money move that isn't atomic, a bulk-close loop that mutates the list while it
walks it, or a fee that forgets a balance can't go negative. The **docstring on each method
states what it is supposed to do** — the bug is (almost) always a mismatch between that
description and the code.

The tests come in two waves: *Wave 1* is catchable from a careful read of the docstring;
*Wave 2* only bites on an edge case (a skipped account, a transfer that can't be covered, a
withdrawal that lands exactly on the balance, an adjacent pair dropped during a bulk close,
or a fee larger than a small balance). Fix the source, **not** the tests.

**As you work, tell us:** what does the failing test expect, what did you observe, what's
your hypothesis, and how did the fix confirm it?

---

## Part 2 — Add a Feature (extra credit — only if you finish early)

**This part is a bonus.** We only reach it if you've finished the debugging comfortably
early — as a rough rule of thumb, all tests green in **under 30 minutes** with time to
spare. If debugging takes the whole session, that's completely fine; a careful, well-
narrated debugging pass is what we're really evaluating. Don't rush Part 1 to get here.

If we do have time: pick **one** feature below (or propose your own) and implement it,
**including at least one test**. Reach for whatever tools and references you'd normally use
(docs, Google, StackOverflow, AI assistants such as Copilot/ChatGPT/Claude, etc.).

- **Transaction history.** Record each deposit/withdrawal/transfer and expose
  `history(id)`.
- **Overdraft limit.** Allow an account to go negative down to a per-account limit, and
  enforce it in `withdraw`/`transfer`.
- **Interest.** Add `apply_interest(rate)` that grows every balance by a rate.
- **Freeze account.** Add `freeze(id)` / `unfreeze(id)` and have `withdraw`/`transfer`
  refuse to move money out of a frozen account.

Walk us through your design choices, edge cases, and how you'd extend it further.

---

## What we're evaluating

- **Debugging method** — reading code, isolating faults, verifying fixes.
- **Communication** — narrating your reasoning and trade-offs.
- **Code quality** — clean, readable changes that match the surrounding style.
- **Feature judgment** — sensible design, edge-case awareness, and a test that proves it.

Good luck — and remember to think out loud!
