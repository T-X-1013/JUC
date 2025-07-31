package com.day04;

public class AccountTest {
    public static void main(String[] args) {
        Account.demo(new AccountUnsafe(10000));
        Account.demo(new AccountSync(10000));
        Account.demo(new AccountSafe(10000));
    }
}
