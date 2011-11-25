package org.tiling.util.test;

// NB does not have to be Serializable

public class Wrapper {
	int i;

	public int getInt() {
		return i;
	}

	public void setInt(int i) {
		this.i = i;
	}
	public boolean equals(Object o) {
		if (!(o instanceof Wrapper)) {
			return false;
		} else {
			return ((Wrapper) o).i == i;
		}
	}

	public String toString() {
		return super.toString() + ", " + i;
	}
}