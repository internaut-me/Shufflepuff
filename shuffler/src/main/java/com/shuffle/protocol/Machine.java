package com.shuffle.protocol;

import com.shuffle.bitcoin.SigningKey;
import com.shuffle.bitcoin.Transaction;
import com.shuffle.bitcoin.VerificationKey;
import com.shuffle.protocol.blame.Matrix;

import java.util.SortedSet;

/**
 * Created by Daniel Krawisz on 2/8/16.
 */
public class Machine {

    Phase phase;

    final SessionIdentifier session;

    Exception e = null;

    Transaction t = null;

    Matrix matrix = null;

    final long amount; // The amount to be shuffled.

    final SigningKey sk; // My signing private key.

    final SortedSet<VerificationKey> players;

    // the phase can be accessed concurrently in case we want to update
    // the user on how the protocol is going.
    public Phase phase() {
        return phase;
    }

    public Transaction transaction() {
        return t;
    }

    public Matrix blame() {
        return matrix;
    }

    public Exception exception() {
        return e;
    }

    // The ShuffleMachine cannot be instantiated directly.
    public Machine(
            SessionIdentifier session,
            long amount,
            SigningKey sk,
            SortedSet<VerificationKey> players) {

        if (session == null || sk == null || players == null) {
            throw new NullPointerException();
        }

        if (amount <= 0) {
            throw new IllegalArgumentException();
        }

        this.session = session;
        this.amount = amount;
        this.sk = sk;
        this.players = players;
        this.phase = Phase.Uninitiated;
    }

    Machine(
            SessionIdentifier session,
            Phase phase,
            Exception e,
            Matrix matrix
    ) {
        this.session = session;
        this.phase = phase;
        this.e = e;
        this.matrix = matrix;

        sk = null;
        players = null;
        amount = 0;
    }

    // Whether two return states are equivalent.
    public boolean match(Machine m) {
        return session == m.session &&
                (phase == null || phase == m.phase) &&
                (e == null && m.e == null ||
                        e != null && m.e != null && e.getClass().equals(m.e.getClass()))
                && (matrix == null && m.e == null || matrix != null && matrix.match(m.matrix));
    }

    public String toString() {
        String session = " " + this.session.toString();

        if (phase == Phase.Completed) {
            return "Successful run" + session;
        }

        String str = "Unsuccessful run" + session;

        if (e != null) {
            str += "; threw " + e.toString();
        }

        if (phase != null) {
            str += " failed in phase " + phase.toString();
        }

        if (matrix != null) {
            str += "; blame = " + matrix.toString();
        }

        return str;
    }
}