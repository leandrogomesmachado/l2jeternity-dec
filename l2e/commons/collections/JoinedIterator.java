package l2e.commons.collections;

import java.util.Iterator;
import java.util.List;

public class JoinedIterator<E> implements Iterator<E> {
   private Iterator<E>[] _iterators;
   private int _currentIteratorIndex;
   private Iterator<E> _currentIterator;
   private Iterator<E> _lastUsedIterator;

   public JoinedIterator(List<Iterator<E>> iterators) {
      this(iterators.toArray(new Iterator[iterators.size()]));
   }

   public JoinedIterator(Iterator... iterators) {
      if (iterators == null) {
         throw new NullPointerException("Unexpected NULL iterators argument");
      } else {
         this._iterators = iterators;
      }
   }

   @Override
   public boolean hasNext() {
      this.updateCurrentIterator();
      return this._currentIterator.hasNext();
   }

   @Override
   public E next() {
      this.updateCurrentIterator();
      return this._currentIterator.next();
   }

   @Override
   public void remove() {
      this.updateCurrentIterator();
      this._lastUsedIterator.remove();
   }

   protected void updateCurrentIterator() {
      if (this._currentIterator == null) {
         if (this._iterators.length == 0) {
            this._currentIterator = EmptyIterator.getInstance();
         } else {
            this._currentIterator = this._iterators[0];
         }

         this._lastUsedIterator = this._currentIterator;
      }

      while(!this._currentIterator.hasNext() && this._currentIteratorIndex < this._iterators.length - 1) {
         ++this._currentIteratorIndex;
         this._currentIterator = this._iterators[this._currentIteratorIndex];
      }
   }
}
