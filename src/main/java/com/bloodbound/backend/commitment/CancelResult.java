package com.bloodbound.backend.commitment;

public class CancelResult {

    public enum Status { SUCCESS, NOT_FOUND, FORBIDDEN, NOT_CANCELLABLE }

    private final Status status;

    private CancelResult(Status status) {
        this.status = status;
    }

    public static CancelResult success()        { return new CancelResult(Status.SUCCESS); }
    public static CancelResult notFound()       { return new CancelResult(Status.NOT_FOUND); }
    public static CancelResult forbidden()      { return new CancelResult(Status.FORBIDDEN); }
    public static CancelResult notCancellable() { return new CancelResult(Status.NOT_CANCELLABLE); }

    public Status getStatus() { return status; }
}