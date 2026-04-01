package com.bloodbound.backend.commitment;

public class CommitmentResult {

    private final boolean success;
    private final Commitment commitment;
    private final String errorCode;
    private final String message;

    private CommitmentResult(boolean success, Commitment commitment,
                             String errorCode, String message) {
        this.success    = success;
        this.commitment = commitment;
        this.errorCode  = errorCode;
        this.message    = message;
    }

    public static CommitmentResult success(Commitment commitment, String message) {
        return new CommitmentResult(true, commitment, null, message);
    }

    public static CommitmentResult error(String code, String message) {
        return new CommitmentResult(false, null, code, message);
    }

    public boolean isSuccess()          { return success; }
    public Commitment getCommitment()   { return commitment; }
    public String getErrorCode()        { return errorCode; }
    public String getMessage()          { return message; }
}