package edu.berkeley.cs186.database.concurrency;

// If you see this line, you have successfully pulled the latest changes from the skeleton for proj4!

public enum LockType {
    S,   // shared
    X,   // exclusive
    IS,  // intention shared
    IX,  // intention exclusive
    SIX, // shared intention exclusive
    NL;  // no lock held

    /**
     * This method checks whether lock types A and B are compatible with
     * each other. If a transaction can hold lock type A on a resource
     * at the same time another transaction holds lock type B on the same
     * resource, the lock types are compatible.
     */
    public static boolean compatible(LockType a, LockType b) {
        if (a == null || b == null) {
            throw new NullPointerException("null lock type");
        }
        // TODO(proj4_part1): implement
        switch (a) {
            case S:
                switch (b) {
                    case S:
                    case IS:
                    case NL: return true;
                    case X:
                    case IX:
                    case SIX: return false;
                    default: throw new UnsupportedOperationException("bad lock type");
                }
            case X:
                switch (b) {
                    case S:
                    case X:
                    case IS:
                    case IX:
                    case SIX: return false;
                    case NL: return true;
                    default: throw new UnsupportedOperationException("bad lock type");
                }
            case IS:
                switch (b) {
                    case X: return false;
                    case S:
                    case IS:
                    case IX:
                    case SIX:
                    case NL: return true;
                    default: throw new UnsupportedOperationException("bad lock type");
                }
            case IX:
                switch (b) {
                    case S:
                    case X:
                    case SIX: return false;
                    case IS:
                    case IX:
                    case NL: return true;
                    default: throw new UnsupportedOperationException("bad lock type");
                }
            case SIX:
                switch (b) {
                    case S:
                    case X:
                    case IX:
                    case SIX: return false;
                    case IS:
                    case NL: return true;
                    default: throw new UnsupportedOperationException("bad lock type");
                }

            case NL:
                switch (b) {
                    case S:
                    case X:
                    case IS:
                    case IX:
                    case SIX:
                    case NL: return true;
                    default: throw new UnsupportedOperationException("bad lock type");
                }
            default: throw new UnsupportedOperationException("bad lock type");
        }
    }

    /**
     * This method returns the lock on the parent resource
     * that should be requested for a lock of type A to be granted.
     */
    public static LockType parentLock(LockType a) {
        if (a == null) {
            throw new NullPointerException("null lock type");
        }
        switch (a) {
        case S: return IS;
        case X: return IX;
        case IS: return IS;
        case IX: return IX;
        case SIX: return IX;
        case NL: return NL;
        default: throw new UnsupportedOperationException("bad lock type");
        }
    }

    /**
     * This method returns if parentLockType has permissions to grant a childLockType
     * on a child.
     */
    public static boolean canBeParentLock(LockType parentLockType, LockType childLockType) {
        if (parentLockType == null || childLockType == null) {
            throw new NullPointerException("null lock type");
        }
        // TODO(proj4_part1): implement
        switch (childLockType) {
            case S:
            case IS:
                switch (parentLockType) {
                    case IS:
                    case IX: return true;
                    case SIX:
                    case S:
                    case NL:
                    case X: return false;
                    default: throw new UnsupportedOperationException("bad lock type");
                }
            case X:
            case IX:
            case SIX:
                switch (parentLockType) {
                    case IX:
                    case SIX: return true;
                    case IS:
                    case S:
                    case X:
                    case NL: return false;
                    default: throw new UnsupportedOperationException("bad lock type");
                }
            case NL:
                switch (parentLockType) {
                    case IS:
                    case IX:
                    case S:
                    case X:
                    case SIX:
                    case NL: return true;
                    default: throw new UnsupportedOperationException("bad lock type");
                }
            default: throw new UnsupportedOperationException("bad lock type");
        }
    }

    /**
     * This method returns whether a lock can be used for a situation
     * requiring another lock (e.g. an S lock can be substituted with
     * an X lock, because an X lock allows the transaction to do everything
     * the S lock allowed it to do).
     */
    public static boolean substitutable(LockType substitute, LockType required) {
        if (required == null || substitute == null) {
            throw new NullPointerException("null lock type");
        }
        // TODO(proj4_part1): implement
        switch (required) {
            case S:
                switch (substitute) {
                    case S:
                    case SIX:
                    case X: return true;
                    case IS:
                    case IX:
                    case NL: return false;
                    default: throw new UnsupportedOperationException("bad lock type");
                }
            case X:
                switch (substitute) {
                    case X: return true;
                    case S:
                    case IS:
                    case IX:
                    case SIX:
                    case NL: return false;
                    default: throw new UnsupportedOperationException("bad lock type");
                }
            case IS:
                switch (substitute) {
                    case IS:
                    case IX: return true;
                    case X:
                    case SIX:
                    case S:
                    case NL: return false;
                    default: throw new UnsupportedOperationException("bad lock type");
                }
            case IX:
                switch (substitute) {
                    case IX:
                    case SIX: return true;
                    case X:
                    case S:
                    case IS:
                    case NL: return false;
                    default: throw new UnsupportedOperationException("bad lock type");
                }
            case SIX:
                switch (substitute) {
                    case SIX: return true;
                    case X:
                    case IX:
                    case S:
                    case IS:
                    case NL: return false;
                    default: throw new UnsupportedOperationException("bad lock type");
                }
            case NL:
                switch (substitute) {
                    case X:
                    case SIX:
                    case IX:
                    case S:
                    case IS:
                    case NL: return true;
                    default: throw new UnsupportedOperationException("bad lock type");
                }
            default: throw new UnsupportedOperationException("bad lock type");
        }
    }

    @Override
    public String toString() {
        switch (this) {
        case S: return "S";
        case X: return "X";
        case IS: return "IS";
        case IX: return "IX";
        case SIX: return "SIX";
        case NL: return "NL";
        default: throw new UnsupportedOperationException("bad lock type");
        }
    }
}

