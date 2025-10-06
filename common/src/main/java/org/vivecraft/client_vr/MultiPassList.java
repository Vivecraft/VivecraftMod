package org.vivecraft.client_vr;

import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_xr.render_pass.RenderPassType;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Function;

public class MultiPassList<T> implements List<T> {

    private final List<T> vanilla;
    private final Function<RenderPass, List<T>> passes;

    public MultiPassList(List<T> vanilla, Function<RenderPass, List<T>> passes) {
        this.vanilla = vanilla;
        this.passes = passes;
    }

    @Override
    public int size() {
        return callOnTargetRet(List::size);
    }

    @Override
    public boolean isEmpty() {
        return callOnTargetRet(List::isEmpty);
    }

    @Override
    public boolean contains(Object o) {
        return callOnTargetRet(l -> l.contains(o));
    }

    @Override
    public Iterator<T> iterator() {
        return callOnTargetRet(List::iterator);
    }

    @Override
    public Object[] toArray() {
        return callOnTargetRet(List::toArray);
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return callOnTargetRet(l -> l.toArray(a));
    }

    @Override
    public boolean add(T t) {
        return callOnTargetRet(l -> l.add(t));
    }

    @Override
    public boolean remove(Object o) {
        return callOnTargetRet(l -> l.remove(o));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return callOnTargetRet(l -> l.containsAll(c));
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return callOnTargetRet(l -> l.addAll(c));
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return callOnTargetRet(l -> l.addAll(c));
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return callOnTargetRet(l -> l.removeAll(c));
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return callOnTargetRet(l -> l.retainAll(c));
    }

    @Override
    public void clear() {
        callOnTarget(List::clear);
    }

    @Override
    public T get(int index) {
        return callOnTargetRet(l -> l.get(index));
    }

    @Override
    public T set(int index, T element) {
        return callOnTargetRet(l -> l.set(index, element));
    }

    @Override
    public void add(int index, T element) {
        callOnTarget(l -> l.add(index, element));
    }

    @Override
    public T remove(int index) {
        return callOnTargetRet(l -> l.remove(index));
    }

    @Override
    public int indexOf(Object o) {
        return callOnTargetRet(l -> l.indexOf(o));
    }

    @Override
    public int lastIndexOf(Object o) {
        return callOnTargetRet(l -> l.lastIndexOf(o));
    }

    @Override
    public ListIterator<T> listIterator() {
        return callOnTargetRet(List::listIterator);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return callOnTargetRet(l -> l.listIterator(index));
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return callOnTargetRet(l -> l.subList(fromIndex, toIndex));
    }


    private void callOnTarget(Consumer<List<T>> consumer) {
        consumer.accept(getCurrent());
    }

    private <R> R callOnTargetRet(Function<List<T>, R> function) {
        return function.apply(getCurrent());
    }

    private List<T> getCurrent() {
        if (RenderPassType.isVanilla()) {
            return this.vanilla;
        } else {
            List<T> list = this.passes.apply(ClientDataHolderVR.getInstance().currentPass);
            return list != null ? list : this.vanilla;
        }
    }
}
