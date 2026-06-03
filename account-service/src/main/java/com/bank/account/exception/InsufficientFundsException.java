package com.bank.account.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(BigDecimal balance, BigDecimal requested) {
        super("Insufficient funds. Available: " + balance + ", requested: " + requested);
    }
}
