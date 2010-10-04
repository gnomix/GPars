// GPars - Groovy Parallel Systems
//
// Copyright © 2008-10  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars.dataflow.stream;

import groovy.lang.Closure;

import java.util.Collection;
import java.util.Iterator;

//todo - thread-safe, potential laziness, proper interface, performance characteristics, generics

//todo test empty and one-element map and filter
@SuppressWarnings({"TailRecursion", "CallToSimpleGetterFromWithinClass"})
public class Cons<T> implements FList<T> {

    @SuppressWarnings({"RawUseOfParameterizedType"})
    static final FList EMPTY = new EmptyList();

    private final T first;
    private final FList<T> rest;

    public static <T> FList<T> from(final Collection<T> coll) {
        //noinspection unchecked
        return from(coll.toArray(), coll.size(), Cons.EMPTY);
    }

    private static <T> FList<T> from(final T[] array, final int index, final FList<T> result) {
        if (index == 0)
            return result;
        return from(array, index - 1, new Cons<T>(array[index - 1], result));
    }

    public Cons(final T first, final FList<T> rest) {
        this.first = first;
        this.rest = rest;
    }

    @Override
    public T getFirst() {
        return first;
    }

    @Override
    public FList<T> getRest() {
        return rest;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @SuppressWarnings({"AutoUnboxing"})
    @Override
    public FList<T> filter(final Closure filterClosure) {
        final Boolean accept = (Boolean) filterClosure.call(new Object[]{getFirst()});
        if (accept)
            return new Cons<T>(getFirst(), getRest().filter(filterClosure));
        else
            return getRest().filter(filterClosure);
    }

    @Override
    public FList<Object> map(final Closure mapClosure) {
        final Object mapped = mapClosure.call(new Object[]{getFirst()});
        return new Cons<Object>(mapped, getRest().map(mapClosure));
    }

    @Override
    public T reduce(final Closure reduceClosure) {
        return reduce(getFirst(), getRest(), reduceClosure);
    }

    private T reduce(final T current, final FList<T> rest, final Closure reduceClosure) {
        if (rest.isEmpty())
            return current;
        @SuppressWarnings({"unchecked"})
        final T aggregate = (T) reduceClosure.call(new Object[]{current, rest.getFirst()});
        return reduce(aggregate, rest.getRest(), reduceClosure);
    }

    @Override
    public T reduce(final T seed, final Closure reduceClosure) {
        return new Cons<T>(seed, this).reduce(reduceClosure);
    }

    @Override
    public Iterator<T> iterator() {
        return new FListIterator<T>(this);
    }

    @Override
    public String toString() {
        return "Cons[" + first + rest.appendingString() + ']';
    }

    @Override
    public String appendingString() {
        return ", " + first + rest.appendingString();
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject", "RawUseOfParameterizedType"})
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Cons)) return false;

        final Cons cons = (Cons) obj;

        if (first != null ? !first.equals(cons.first) : cons.first != null) return false;
        return !(rest != null ? !rest.equals(cons.rest) : cons.rest != null);

    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (rest != null ? rest.hashCode() : 0);
        return result;
    }
}
