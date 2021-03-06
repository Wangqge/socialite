package socialite.collection;

import gnu.trove.map.hash.TLongIntHashMap;

public class SLongIntHashMap extends TLongIntHashMap {
	long prevKey;
	int prevVal;
	
	public SLongIntHashMap(int initCapacity, float f, long noEntryKey, int noEntryValue) {
		super(initCapacity, f, noEntryKey, noEntryValue);
		prevKey = noEntryValue;
		prevVal = noEntryValue;
	}

	@Override
	public int get(long key) {
		if (prevKey!=super.no_entry_key && key==prevKey) {
			return prevVal;
		}
		prevKey = key;
		prevVal = super.get(key);
		return prevVal;
	}
	
	@Override
	public int put(long key, int value) {
		prevKey = key;
		prevVal = value;
		return super.put(key, value);
	}
	@Override
	public int remove(long key) {
		prevKey = super.no_entry_key;
		prevVal = super.no_entry_value;
		return super.remove(key);
	}
	@Override
	public void clear() {
		prevKey=super.no_entry_key;
		super.clear();
	}
}
