package edu.berkeley.cs186.database.query;

import java.util.*;

import edu.berkeley.cs186.database.TransactionContext;
import edu.berkeley.cs186.database.common.iterator.BacktrackingIterator;
import edu.berkeley.cs186.database.databox.DataBox;
import edu.berkeley.cs186.database.table.Record;

class SortMergeOperator extends JoinOperator {
    SortMergeOperator(QueryOperator leftSource,
                      QueryOperator rightSource,
                      String leftColumnName,
                      String rightColumnName,
                      TransactionContext transaction) {
        super(leftSource, rightSource, leftColumnName, rightColumnName, transaction, JoinType.SORTMERGE);

        this.stats = this.estimateStats();
        this.cost = this.estimateIOCost();
    }

    @Override
    public Iterator<Record> iterator() {
        return new SortMergeIterator();
    }

    @Override
    public int estimateIOCost() {
        //does nothing
        return 0;
    }

    /**
     * An implementation of Iterator that provides an iterator interface for this operator.
     *    See lecture slides.
     *
     * Before proceeding, you should read and understand SNLJOperator.java
     *    You can find it in the same directory as this file.
     *
     * Word of advice: try to decompose the problem into distinguishable sub-problems.
     *    This means you'll probably want to add more methods than those given (Once again,
     *    SNLJOperator.java might be a useful reference).
     *
     */
    private class SortMergeIterator extends JoinIterator {
        /**
        * Some member variables are provided for guidance, but there are many possible solutions.
        * You should implement the solution that's best for you, using any member variables you need.
        * You're free to use these member variables, but you're not obligated to.
        */
        private BacktrackingIterator<Record> leftIterator;
        private BacktrackingIterator<Record> rightIterator;
        private Record leftRecord;
        private Record nextRecord;
        private Record rightRecord;
        private boolean marked;

        private SortMergeIterator() {
            super();
            // TODO(proj3_part1): implement
            SortOperator sortedLeft = new SortOperator(SortMergeOperator.this.getTransaction(),
                                                    this.getLeftTableName(), this.new LeftRecordComparator());
            SortOperator sortedRight = new SortOperator(SortMergeOperator.this.getTransaction(),
                                                    this.getRightTableName(), this.new RightRecordComparator());

            leftIterator = (BacktrackingIterator<Record>) sortedLeft.iterator();
            rightIterator = (BacktrackingIterator<Record>) sortedRight.iterator();

            leftRecord = leftIterator.hasNext() ? leftIterator.next() : null;
            rightRecord = rightIterator.hasNext() ? rightIterator.next() : null;

            this.nextRecord = null;

            if (this.rightRecord != null && this.leftIterator != null) {
                try {
                    fetchNextRecord();
                } catch (NoSuchElementException e) {
                    this.nextRecord = null;
                }
            }
        }

        /**
         * Advances the left record
         *
         * The thrown exception means we're done: there is no next record
         * It causes this.fetchNextRecord (the caller) to hand control to its caller.
         */
        private void nextLeftRecord() {
            if (!leftIterator.hasNext()) { throw new NoSuchElementException("All Done!"); }
            leftRecord = leftIterator.next();
        }

        /**
         * Helper function: Advances the right record
         */
        private void nextRightRecord() {
            rightRecord = rightIterator.hasNext() ? rightIterator.next() : null;
        }

        /**
         * Helper function: comparator of two records
         * @param o1
         * @param o2
         * @return
         */
        public int compare(Record o1, Record o2) {
            DataBox leftJoinValue = o1.getValues().get(SortMergeOperator.this.getLeftColumnIndex());
            DataBox rightJoinValue = o2.getValues().get(SortMergeOperator.this.getRightColumnIndex());
            return leftJoinValue.compareTo(rightJoinValue);
        }

        /**
         * Helper method to create a joined record from a record of the left relation
         * and a record of the right relation.
         * @param leftRecord Record from the left relation
         * @param rightRecord Record from the right relation
         * @return joined record
         */
        private Record joinRecords(Record leftRecord, Record rightRecord) {
            List<DataBox> leftValues = new ArrayList<>(leftRecord.getValues());
            List<DataBox> rightValues = new ArrayList<>(rightRecord.getValues());
            leftValues.addAll(rightValues);
            return new Record(leftValues);
        }

        /**
         * Pre-fetches what will be the next record, and puts it in this.nextRecord.
         * Pre-fetching simplifies the logic of this.hasNext() and this.next()
         */
        private void fetchNextRecord() {
            if (this.leftRecord == null) {
                throw new NoSuchElementException("No new record to fetch");
            }
            this.nextRecord = null;

            do {
                if (!this.marked) {
                    while (this.compare(this.leftRecord, this.rightRecord) < 0) {
                        nextLeftRecord();
                    }
                    while (this.compare(this.leftRecord, this.rightRecord) > 0) {
                        nextRightRecord();
                        if (this.rightRecord == null) {
                            throw new NoSuchElementException("all right < all left");
                        }
                    }
                    this.marked = true;
                    this.rightIterator.markPrev();
                }
                if (this.rightRecord != null && this.compare(this.leftRecord, this.rightRecord) == 0) {
                    this.nextRecord = this.joinRecords(this.leftRecord, this.rightRecord);
                    nextRightRecord();
                } else {
                    nextLeftRecord();
                    this.rightIterator.reset();
                    nextRightRecord();
                    this.marked = false;
                }
            } while (!this.hasNext());

        }

        /**
         * Checks if there are more record(s) to yield
         *
         * @return true if this iterator has another record to yield, otherwise false
         */
        @Override
        public boolean hasNext() {
            // TODO(proj3_part1): implement
            return this.nextRecord != null;
        }

        /**
         * Yields the next record of this iterator.
         *
         * @return the next Record
         * @throws NoSuchElementException if there are no more Records to yield
         */
        @Override
        public Record next() {
            // TODO(proj3_part1): implement
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }

            Record nextRecord = this.nextRecord;
            try {
                this.fetchNextRecord();
            } catch (NoSuchElementException e) {
                this.nextRecord = null;
            }
            return nextRecord;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private class LeftRecordComparator implements Comparator<Record> {
            @Override
            public int compare(Record o1, Record o2) {
                return o1.getValues().get(SortMergeOperator.this.getLeftColumnIndex()).compareTo(
                           o2.getValues().get(SortMergeOperator.this.getLeftColumnIndex()));
            }
        }

        private class RightRecordComparator implements Comparator<Record> {
            @Override
            public int compare(Record o1, Record o2) {
                return o1.getValues().get(SortMergeOperator.this.getRightColumnIndex()).compareTo(
                           o2.getValues().get(SortMergeOperator.this.getRightColumnIndex()));
            }
        }
    }
}
