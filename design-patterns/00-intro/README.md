# Design Patterns Workshop

###### Remember: A pattern should solve a real problem, not just look smart

**👋🏼 Welcome to the Design Patterns Workshop.**

This workshop helps you understand how experienced engineers structure code
to make it easier to maintain, extend, and scale.

Instead of only theory, every pattern in this workshop includes:

- A real problem
- A bad implementation
- A refactored implementation using the pattern
- Exercises

## What Are Design Patterns?

**Design patterns** are **reusable solutions to common software design problems**, to quick definition
is that design patterns are solutions to solving recurring/commonly problems in software design.

**Design patterns** are like ready-made architectural plans for code. They help solve common design
problems in a clean and reusable way.

We can't go to web and copy a design into your program with the same way you copy libraries or function
(like Math.random()). Pattern is not code is a concept for solving particular problem, you must follow
pattern concept and implement solution that match the logic of you program.

**Example problems:**

- How do we create objects without tightly coupling our code?
- How do we allow behavior to change dynamically?
- How do we simplify complex subsystems?

Design patterns give proven answers to these problems.

### Common misunderstood

They are not **libraries** or **frameworks**.

Design patterns are not a **rules you must always follow**

**“More patterns means better code.”** Sometimes patterns make code more complex.

**“Design patterns are only for big projects.”** Small projects can use them too, but only when needed.

**“A pattern should be used because it is popular.”** A pattern should solve a real problem, not just look smart
and this way of thinking change everything

**“Patterns replace good design thinking.”** They do not. You still need to understand the problem first.

**“One pattern fits every case.”** The same pattern can be great in one project and bad in another.

## How to choose Design Patterns?

This is the most common question, also the question that anyone should ask before thinking or
resolving solution for a problem, to answer really fast **"do not choose a pattern first; choose the problem first."**

### How to choose a design pattern:

Ask these questions ⁉️:

- **What is changing a lot?**
    - If behavior changes often, a pattern like **Strategy, Observer or Command** may help.
- **What is getting messy?**
    - If object creation is messy, to many args, think **Factory** or **Builder**.
- **Do I have many if/else branches?**
    - That often suggests **Strategy, State, or Command**.
- **Do many objects need to react to one event?**
    - That often suggests **Observer**.
- **Do I need to add steps without changing the main flow?**
    - That often suggests Chain of Responsibility.

### How to know if it is the best pattern

No one can be 100% sure at first. The best pattern is the one that:

- solves the real problem.
- keeps the code simple enough.
- makes future changes easier.
- does not add unnecessary complexity.

A pattern is probably not a good fit if:

- you are forcing it just because it is famous.
- it makes the code harder to read.
- the problem is still small and simple.
- you need many extra classes for no real benefit.

## Pattern Categories

In this workshop we explore three categories:

### Creational Patterns

Focus on **how objects are created**

Examples:

- Singleton
- Factory
- Builder

### Structural Patterns

Focus on **how classes and objects are composed**

Examples:

- Adapter
- Facade
- Decorator

### Behavioral Patterns

Focus on **how objects communicate**

Examples:

- Strategy
- Observer
- Commands

## How to Use This Workshop

1. Read the problem
2. Look at the **"before"** implementation
3. Try solving it yourself
4. Compare with the pattern solution
5. Complete the exercise

## Workshop Structure

Each pattern folder contains: (see: [Template Folder](/design-patterns/template))

- `problem.md` → the problem we want to solve
- `before/` → a naive implementation
- `after/` → the pattern implementation
- `exercise/` → hands-on practice
