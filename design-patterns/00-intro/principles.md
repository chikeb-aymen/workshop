# Design Patterns Principles

Design patterns are based on fundamental design principles.

Understanding these principles will help you know **when to use a pattern**.

---

# SOLID Principles

## Single Responsibility Principle (SRP)

A class should have **one reason to change**.

Bad example: A class that handles payment, logging, and email notifications.

Better: Separate classes for each responsibility.

---

## Open/Closed Principle (OCP)

Software should be **open for extension but closed for modification**.

Instead of modifying existing code, extend behavior using **interfaces** or **composition**.

Example: Strategy Pattern.

---

## Liskov Substitution Principle (LSP)

Subclasses must be replaceable for their base class without breaking the system.

---

## Interface Segregation Principle (ISP)

Clients should not be forced to depend on interfaces they do not use.

Prefer **small focused interfaces**.

---

## Dependency Inversion Principle (DIP)

High-level modules should not depend on low-level modules.

Both should depend on **abstractions**.

This principle is heavily used in **Spring Dependency Injection**.

---

# Composition Over Inheritance

Prefer **combining objects** instead of building deep inheritance hierarchies.

Example: Strategy Pattern.

---

# Loose Coupling

Classes should depend as little as possible on other classes.

**Benefits:**

- easier testing
- easier maintenance
- easier extension
