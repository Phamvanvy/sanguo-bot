import com.pip.engine.Random;

/*
 * Golden generator: dumps values from the game's own PRNG (com/pip/engine/Random.java,
 * compiled unmodified from the client source) so the JS port can be pinned to it.
 * Seeds include the real randomSeed of map 1395 and the bounds the landform code actually uses.
 */
public class RandomGolden {
    public static void main(String[] args) {
        long[] seeds = { 0L, 1L, 42L, 1692343764L, 1691377442L, -1L, 2147483647L };
        int[] bounds = { 1, 2, 3, 7, 16, 64, 100, 1000, 32768 };
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"nextInt32\": {\n");
        for (int s = 0; s < seeds.length; s++) {
            Random r = new Random(seeds[s]);
            sb.append("    \"").append(seeds[s]).append("\": [");
            for (int i = 0; i < 8; i++) {
                if (i > 0) sb.append(", ");
                sb.append(r.nextInt());
            }
            sb.append("]").append(s == seeds.length - 1 ? "\n" : ",\n");
        }
        sb.append("  },\n  \"nextIntBounded\": {\n");
        for (int s = 0; s < seeds.length; s++) {
            sb.append("    \"").append(seeds[s]).append("\": {\n");
            for (int b = 0; b < bounds.length; b++) {
                Random r = new Random(seeds[s]);
                sb.append("      \"").append(bounds[b]).append("\": [");
                for (int i = 0; i < 12; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(r.nextInt(bounds[b]));
                }
                sb.append("]").append(b == bounds.length - 1 ? "\n" : ",\n");
            }
            sb.append("    }").append(s == seeds.length - 1 ? "\n" : ",\n");
        }
        sb.append("  },\n  \"nextLong\": {\n");
        for (int s = 0; s < seeds.length; s++) {
            Random r = new Random(seeds[s]);
            sb.append("    \"").append(seeds[s]).append("\": [");
            for (int i = 0; i < 4; i++) {
                if (i > 0) sb.append(", ");
                sb.append("\"").append(r.nextLong()).append("\"");
            }
            sb.append("]").append(s == seeds.length - 1 ? "\n" : ",\n");
        }
        sb.append("  }\n}\n");
        System.out.print(sb);
    }
}
