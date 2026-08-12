import pathlib
import unittest


ROOT = pathlib.Path(__file__).resolve().parents[1]
VN_SRC = ROOT / "Game" / "sangobuildVn" / "server" / "src"


def source(relative_path):
    return (VN_SRC / relative_path).read_text(encoding="utf-8")


class VnServerSecurityRegressionTests(unittest.TestCase):
    def test_chat_cheat_unlock_is_disabled(self):
        handler = source("peony/game/PlayerPacketHandler.java")
        self.assertNotIn("message.equals(Server.server.cheat)", handler)
        self.assertIn('Boolean.getBoolean("peony.gmChatEnabled")', handler)
        self.assertIn('System.getProperty("peony.gmChatPlayerIds", "")', handler)
        self.assertIn("isChatCheatAllowed(player.id)", handler)
        self.assertIn("DANGEROUS_CHAT_CHEATS_ENABLED && cmds[0].equals(\"/load\")", handler)
        self.assertIn("DANGEROUS_CHAT_CHEATS_ENABLED && cmds[0].equals(\"/shut\")", handler)
        self.assertIn("message.length() > 500", handler)
        self.assertIn("content.length() > 2000", handler)

    def test_auction_search_uses_bound_parameters(self):
        dao = source("peony/auction/AuctionDAO.java")
        self.assertIn('buff.append(" and a.name like ?")', dao)
        self.assertIn('values.add("%" + name + "%")', dao)
        self.assertNotIn('a.name like \'%" + name', dao)
        self.assertIn("amount > 100", dao)

    def test_packet_lengths_are_bounded(self):
        packet = source("peony/net/Packet.java")
        self.assertIn("MAX_PACKET_SIZE = 1024 * 1024", packet)
        self.assertIn("len > data.remaining()", packet)
        for decoder in ("MinaUADecoder.java", "FlashUADecoder.java", "DispatchUADecoder.java"):
            self.assertIn("MAX_PACKET_SIZE", source("peony/net/" + decoder))

    def test_pagination_and_depot_counts_are_validated(self):
        mail = source("peony/db/MailListCall.java")
        auction = source("peony/auction/AuctionListCall.java")
        depot = source("peony/depot/DepotService.java")
        account_depot = source("peony/game/AccountDepotService.java")
        self.assertIn("pageSize <= 0 || pageSize > 100", mail)
        self.assertIn("amount <= 0 || amount > 100", auction)
        self.assertGreaterEqual(depot.count("count <= 0"), 2)
        self.assertGreaterEqual(account_depot.count("count <= 0"), 2)

    def test_movement_validation_is_enforced(self):
        player = source("peony/game/Player.java")
        handler = source("peony/game/PlayerPacketHandler.java")
        self.assertIn("protected boolean checkPosition", player)
        self.assertIn("if (!checkPosition(currentPosition))", player)
        self.assertIn("antiPlugModel = ANTIPLUG_MODEL_NONBENEFIT", player)
        self.assertIn("player.addForbidScore(1);\n\t\t\t\treturn;", handler)


if __name__ == "__main__":
    unittest.main()
