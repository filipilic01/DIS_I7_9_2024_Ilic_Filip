package com.bank.transaction.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(BigDecimal available, BigDecimal requested) {
        super(String.format("Insufficient funds: available=%.2f, requested=%.2f", available, requested));
    }
}
