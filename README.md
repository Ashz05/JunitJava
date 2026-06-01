# Race Condition Removal — `org.ThreadCheck`

## Overview

This document explains the race condition present in the original `ThreadCheck` code, why it occurs, and how it was resolved.

---

## The Problem

### What is a Race Condition?

A race condition occurs when two or more threads access shared mutable state concurrently, and the final result depends on the unpredictable order of execution.

### Where the Race Condition Lives

Despite `Counting()` being marked `synchronized`, there is a subtle but critical bug:

## java
// In class A
Counter count = new Counter(); // ← new instance per thread

// In class B
Counter count = new Counter(); // ← different instance, different lock
` ` `

Each thread creates **its own `Counter` instance**. Java's `synchronized` on an instance method locks on `this` — the specific object instance. Since threads A and B hold **different locks**, the synchronization provides **no mutual exclusion**.

However, `count` (the field being incremented) is `static` — shared across all instances. This means:

- Two threads increment the same `static int count`
- Each thread locks on a **different object**
- The `++` operation (read → increment → write) is **not atomic**
- Threads can interleave these steps, causing lost updates

### Example of the Race

| Step | Thread A | Thread B | `count` |
|------|----------|----------|---------|
| 1 | reads `count = 5` | | 5 |
| 2 | | reads `count = 5` | 5 |
| 3 | writes `count = 6` | | 6 |
| 4 | | writes `count = 6` | 6 ← **lost increment** |

Expected result with `initializer = 100`: `count = 200`  
Actual result: anywhere between `100` and `200`, non-deterministically.

---

## The Fix

### Option 1 — Share a Single `Counter` Instance (Recommended)

Pass a **single shared `Counter` instance** into both threads instead of creating one per thread.

## Java Code
class A implements Runnable {
    int initializer;
    Counter count; 

    public A(int initializer, Counter count) {
        this.initializer = initializer;
        this.count = count;
    }

    @Override
    public void run() {
        int i = 0;
        while (i < initializer) {
            try {
                count.Counting(); // same lock, safe
                Thread.sleep(1);
                i++;
            } catch (InterruptedException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}

Same change applies to class B

In ThreadTest.ThreadWork():
Counter sharedCounter = new Counter();
Runnable obj1 = new A(this.initializer, sharedCounter);
Runnable obj2 = new B(this.initializer, sharedCounter);


Now both threads lock on the **same object**, so `synchronized` works as intended.

---

### Option 2 — Use `AtomicInteger` (Lock-Free)

Replace `static int count` with `AtomicInteger` for a lock-free, thread-safe alternative:


import java.util.concurrent.atomic.AtomicInteger;

class Counter {
    static AtomicInteger count = new AtomicInteger(0);

    public static void Counting() {
        count.incrementAndGet(); // atomic, no synchronization needed
    }
}


`AtomicInteger.incrementAndGet()` uses CPU-level compare-and-swap (CAS), making it safe without any `synchronized` keyword.

---

### Option 3 — Use a Static Lock Object

If keeping separate instances is required, synchronize on the **class** rather than the instance:


class Counter {
    static int count;

    public static void Counting() {
        synchronized (Counter.class) { // class-level lock
            count++;
        }
    }
}


This ensures all threads, regardless of which `Counter` instance they hold, compete for the same lock.

## Summary Table

| Approach | Thread-Safe | Lock Type | Notes |
|---|---|---|---|
| Original code | ❌ | Per-instance (broken) | Different instances = different locks |
| Shared instance | ✅ | Per-instance | Simplest structural fix |
| `AtomicInteger` | ✅ | Lock-free (CAS) | Best performance at scale |
| `synchronized(Counter.class)` | ✅ | Class-level | Works with multiple instances |

---

## Key Takeaways

- `synchronized` on an **instance method** locks on `this` — useless if different threads use different objects.
- A `static` field shared across instances requires either a **shared lock** or an **atomic type**.
- Prefer `AtomicInteger` or `java.util.concurrent` utilities over manual `synchronized` blocks for modern Java concurrency.
- Always reset shared static state between test runs (e.g., `Counter.count = 0`) to avoid test pollution.

