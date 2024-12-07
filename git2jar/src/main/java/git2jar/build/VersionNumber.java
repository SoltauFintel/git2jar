package git2jar.build;

public class VersionNumber {
	private final String v;
	private final String sort;

	/**
	 * @param v version number, e.g. "1.3.7"
	 */
	public VersionNumber(String v) {
		if (v == null || v.isBlank()) {
			throw new IllegalArgumentException("VersionNumber must not be empty!");
		}
		this.v = v.toLowerCase();
		String[] w = this.v.split("\\.");
		String ret = "";
		for (int i = 0; i < w.length; i++) {
			String a = w[i];
			int j;
			for (j = 0; j < a.length(); j++) {
				char c = a.charAt(j);
				if (!(c >= '0' && c <= '9')) {
					break;
				}
			}
			String alpha = j >= a.length() ? "" : a.substring(j);
			String nr = a.substring(0, j);
			while (nr.length() < 5) {
				nr = "0" + nr;
			}
			if (!ret.isEmpty()) {
				ret += ".";
			}
			ret += nr + alpha;
		}
		sort = ret;
	}

	public String sort() {
		return sort;
	}

	@Override
	public String toString() {
		return v;
	}
}
