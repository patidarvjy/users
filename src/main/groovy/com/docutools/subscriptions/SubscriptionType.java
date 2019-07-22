package com.docutools.subscriptions;

public enum SubscriptionType {
    Master(100, true),
    Multi(10, true),
    Combo(5, true),
    Pocket(1, 3, true),
    Test(0, 3, false);

    private static final int NO_ACCOUNT_LIMIT = -1;

    private int rank;
    private int maxAccounts;
    private boolean paid;

    SubscriptionType(int rank, boolean paid) {
        this(rank, NO_ACCOUNT_LIMIT, paid);
    }

    SubscriptionType(int rank, int maxAccounts, boolean paid) {
        this.rank = rank;
        this.maxAccounts = maxAccounts;
        this.paid = paid;
    }

    public int getRank() {
        return rank;
    }

    public int getMaxAccounts() {
        return maxAccounts;
    }

    public boolean hasAccountLimit() {
        return maxAccounts != NO_ACCOUNT_LIMIT;
    }

    public boolean isPaid() {
        return paid;
    }
}
