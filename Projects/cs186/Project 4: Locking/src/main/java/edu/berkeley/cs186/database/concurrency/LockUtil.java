package edu.berkeley.cs186.database.concurrency;
// If you see this line, you have successfully pulled the latest changes from the skeleton for proj4!
import edu.berkeley.cs186.database.Transaction;
import edu.berkeley.cs186.database.TransactionContext;

/**
 * LockUtil is a declarative layer which simplifies multigranularity lock acquisition
 * for the user (you, in the second half of Part 2). Generally speaking, you should use LockUtil
 * for lock acquisition instead of calling LockContext methods directly.
 */
public class LockUtil {
    /**
     * Ensure that the current transaction can perform actions requiring LOCKTYPE on LOCKCONTEXT.
     *
     * This method should promote/escalate as needed, but should only grant the least
     * permissive set of locks needed.
     *
     * lockType is guaranteed to be one of: S, X, NL.
     *
     * If the current transaction is null (i.e. there is no current transaction), this method should do nothing.
     */
    public static void ensureSufficientLockHeld(LockContext lockContext, LockType lockType) {
        // TODO(proj4_part2): implement

        TransactionContext transaction = TransactionContext.getTransaction(); // current transaction

        if (lockContext.parent != null) {
            if (lockContext.parent.isAutoEscalate()) {
                if (lockContext.parent != null && lockContext.parent.capacity() >= 10 && lockContext.parent.saturation(transaction) >= .2) {
                    lockContext.parent.escalate(transaction);
                }
            }
        }

        if (transaction == null) {
            return;
        } else if (!(lockType.equals(LockType.S) || lockType.equals(LockType.X))) {
            return;
        }

        LockType effectiveLockType = lockContext.getEffectiveLockType(transaction);
        LockType explicitLockType = lockContext.getExplicitLockType(transaction);

        if (lockType.equals(LockType.S)) {
            if (LockType.substitutable(effectiveLockType, LockType.S)) { //effectiveLockType = S,X
                return;
            }

            if (explicitLockType.equals(LockType.NL)) {
                requireParent(transaction, lockContext, LockType.parentLock(lockType));
                lockContext.acquire(transaction, lockType);
            } else {
                if (explicitLockType.equals(LockType.IS)) {
                    lockContext.escalate(transaction);
                } else if (explicitLockType.equals(LockType.IX)) {
                    lockContext.promote(transaction, LockType.SIX);
                }
            }

        } else if (lockType.equals(LockType.X)) {
            if (LockType.substitutable(effectiveLockType, LockType.X)) { //effectiveLockType = LockType.X
                return;
            }

            if (effectiveLockType.equals(LockType.S)) {
                requireParent(transaction, lockContext, LockType.parentLock(lockType));
                if (explicitLockType.equals(LockType.NL)) {
                    lockContext.acquire(transaction, lockType);
                } else if (explicitLockType.equals(LockType.S)) {
                    lockContext.promote(transaction, lockType);
                }
            } else if (effectiveLockType.equals(LockType.NL)) {
                requireParent(transaction, lockContext, LockType.parentLock(lockType));
                lockContext.acquire(transaction, lockType);
            } else if (effectiveLockType.equals(LockType.SIX) || effectiveLockType.equals(LockType.IX)) {
                lockContext.escalate(transaction);
            } else if (effectiveLockType.equals(LockType.IS)) {
                requireParent(transaction, lockContext, LockType.parentLock(lockType));
                lockContext.promote(transaction, LockType.S);
                lockContext.promote(transaction, lockType);
            }
        }
    }

    private static void requireParent(TransactionContext transaction, LockContext lockContext, LockType lockType) {
        LockContext parentContext = lockContext.parentContext();
        if (parentContext == null) {
            return;
        }

        LockType parentLockType = parentContext.getExplicitLockType(transaction);
        if (LockType.substitutable(parentLockType, lockType)) {
            return;
        }

        requireParent(transaction, parentContext, LockType.parentLock(lockType));

        if (parentLockType.equals(LockType.NL)) {
            parentContext.acquire(transaction, lockType);
        } else if (parentLockType.equals(LockType.S)){
            if (lockType.equals(LockType.IX)) {
                parentContext.promote(transaction, LockType.SIX);
            }
        } else if (parentLockType.equals(LockType.IS)) {
            if (lockType.equals(LockType.IX)) {
                parentContext.promote(transaction, LockType.IX);
            }
        }
    }


    // TODO(proj4_part2): add helper methods as you see fit
}
