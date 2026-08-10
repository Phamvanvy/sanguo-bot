package peony.npc.service;

    public class Npc {
		public int npcId;
		public int x;
		public int y;
		public String message;
		public boolean isWorldBoss;
		public Npc(int npcId, int x, int y, String message) {
			this.npcId = npcId;
			this.x = x;
			this.y = y;
			this.message = message;
		}
    }
