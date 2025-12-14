# JSON-like Interactive Data Engine (Java)

## Overview
An **interactive Java learning project** that implements a **custom JSON-like in-memory data engine** using only **manual string processing and regex**, without any external JSON libraries.

The project focuses on **understanding parsing, validation, constraints, and query logic at a low level**, rather than clean architecture or production design.

---

## Important Design Note (Intentional)
⚠️ **This code is intentionally NOT clean or fully object-oriented.**

- Implemented as a **single-file, monolithic class** on purpose
- Multiple responsibilities are handled together to keep all logic visible in one place
- Regex-based parsing is used deliberately to explore edge cases and limitations
- No design patterns or abstraction layers were introduced intentionally

This project prioritizes **mechanical understanding over software architecture**.  
A proper OOP refactor was deliberately postponed to avoid hiding logic behind abstractions.

---

## Functionality
- Interactive command-based execution
- Schema definition with field constraints (`required`, `unique`)
- Insert, update, search, and delete operations
- Simple filtering with `=`, `<`, `>`
- In-memory data storage
- Console output in tabular format

---

## Constraints
- No external JSON parsers or libraries
- Java String methods and regex only
- Case-insensitive input
- Single-line command processing

---

## Status
**Archived / Learning Project**  
Not intended for production use.
