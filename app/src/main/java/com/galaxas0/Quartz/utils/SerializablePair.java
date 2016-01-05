package com.galaxas0.Quartz.utils;

import android.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

@Deprecated
public class SerializablePair<F, S> implements Serializable {
    private static final long serialVersionUID = 7526471155622776147L;

    public F first;
    public S second;

    public static <F, S> SerializablePair<F, S> create(F first, S second) {
        return new SerializablePair<F, S>(first, second);
    }

    public SerializablePair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Pair))
            return false;
        return Objects.equals(((Pair<?, ?>) o).first, first) &&
                Objects.equals(((Pair<?, ?>)o).second, second);
    }

    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^
                (second == null ? 0 : second.hashCode());
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        first = (F)in.readObject();
        second = (S)in.readObject();
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(first);
        out.writeObject(second);
    }

    public static class ObjectHolder {
        public Object object;
    }
}
