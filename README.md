# Webhook SQL Solver

An automated **Spring Boot** application that talks to a webhook, solves a SQL challenge, and posts the final solution — all in one smooth startup.

## Overview

When the app starts, it:
1. Sends a **POST** request to generate a webhook.
2. Receives a **SQL problem** to solve.
3. Executes logic to determine the correct **SQL query solution**.
4. Posts the final query result to the provided **webhook URL** — securely using a **JWT token**.

Everything happens automatically at startup — no manual trigger required.

---

## Why This Exists

This project simulates how modern platforms test developer skills using **webhooks and real-time code evaluation**.  
It’s a practical exercise in:
- Event-driven programming  
- REST communication  
- Secure data exchange using JWT  
- SQL logic automation  

It’s also a neat way to combine **Java backend + database problem-solving** into one workflow.

---

## Tech Stack

- **Java 17+**
- **Spring Boot**
- **Maven** 
- **RESTful APIs**
- **JWT Authentication**

---

## Project Flow

```text
App Startup
   ↓
Send POST request → Generate Webhook
   ↓
Receive SQL problem
   ↓
Solve + Construct final SQL query
   ↓
POST solution back → Webhook URL (with JWT)
