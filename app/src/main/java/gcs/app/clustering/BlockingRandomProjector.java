package gcs.app.clustering;

@jakarta.inject.Singleton
public final class BlockingRandomProjector {
	private final int inDim;
	private final int outDim;
	private final float[][] R;
	private final float scale;

	public BlockingRandomProjector() {
		this(1536, 64, 42L); // in=full embedding dim, out=64, fixed seed for reproducibility
	}

	public BlockingRandomProjector(int inDim, int outDim, long seed) {
		this.inDim = inDim;
		this.outDim = outDim;
		this.scale = (float) (1.0 / Math.sqrt(outDim));
		this.R = new float[inDim][outDim];

		var rnd = new java.util.Random(seed);
		for (int i = 0; i < inDim; i++) {
			for (int j = 0; j < outDim; j++) {
				R[i][j] = (float) (rnd.nextGaussian() * scale); // N(0,1)/sqrt(outDim)
			}
		}
	}

	public float[] project(float[] xUnit) {
		// xUnit MUST be L2-normalized full embedding (length==inDim).
		var z = new float[outDim];
		for (int j = 0; j < outDim; j++) {
			double acc = 0;
			for (int i = 0; i < inDim; i++) acc += xUnit[i] * R[i][j];
			z[j] = (float) acc;
		}
		// L2-normalize z for cosine-friendly distances
		double n = 0;
		for (float v : z) n += v * v;
		n = Math.sqrt(n);
		if (n > 0) for (int j = 0; j < outDim; j++) z[j] /= n;
		return z;
	}
}

