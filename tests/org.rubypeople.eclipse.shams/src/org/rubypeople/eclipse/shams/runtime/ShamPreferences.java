package org.rubypeople.eclipse.shams.runtime;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferenceNodeVisitor;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class ShamPreferences implements IEclipsePreferences {
    private Map data = new HashMap();
    
    public void addNodeChangeListener(INodeChangeListener listener) {
        throw new ShamException();
    }

    public void removeNodeChangeListener(INodeChangeListener listener) {
        throw new ShamException();
    }

    public void addPreferenceChangeListener(
            IPreferenceChangeListener listener) {
        throw new ShamException();
    }

    public void removePreferenceChangeListener(
            IPreferenceChangeListener listener) {
        throw new ShamException();
    }

    public void removeNode() throws BackingStoreException {
        throw new ShamException();
    }

    public Preferences node(String path) {
        throw new ShamException();
    }

    public void accept(IPreferenceNodeVisitor visitor)
            throws BackingStoreException {
        throw new ShamException();
    }

    public void put(String key, String value) {
        data.put(key, value);
    }

    public String get(String key, String def) {
        Object value = data.get(key);
        if (value == null)
            return def;
        return (String) value;
    }

    public void remove(String key) {
        throw new ShamException();
    }

    public void clear() throws BackingStoreException {
        throw new ShamException();
    }

    public void putInt(String key, int value) {
        throw new ShamException();
    }

    public int getInt(String key, int def) {
        throw new ShamException();
    }

    public void putLong(String key, long value) {
        throw new ShamException();
    }

    public long getLong(String key, long def) {
        throw new ShamException();
    }

    public void putBoolean(String key, boolean value) {
        throw new ShamException();
    }

    public boolean getBoolean(String key, boolean def) {
        throw new ShamException();
    }

    public void putFloat(String key, float value) {
        throw new ShamException();
    }

    public float getFloat(String key, float def) {
        throw new ShamException();
    }

    public void putDouble(String key, double value) {
        throw new ShamException();
    }

    public double getDouble(String key, double def) {
        throw new ShamException();
    }

    public void putByteArray(String key, byte[] value) {
        throw new ShamException();
    }

    public byte[] getByteArray(String key, byte[] def) {
        throw new ShamException();
    }

    public String[] keys() throws BackingStoreException {
        throw new ShamException();
    }

    public String[] childrenNames() throws BackingStoreException {
        throw new ShamException();
    }

    public Preferences parent() {
        throw new ShamException();
    }

    public boolean nodeExists(String pathName) throws BackingStoreException {
        throw new ShamException();
    }

    public String name() {
        throw new ShamException();
    }

    public String absolutePath() {
        throw new ShamException();
    }

    public void flush() throws BackingStoreException {
        throw new ShamException();
    }

    public void sync() throws BackingStoreException {
        throw new ShamException();
    }

}