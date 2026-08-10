package com.pip.itimes.server.world;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Vector;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.dao.PlayerDao;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.Grid;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.Pet;
import com.pip.itimes.server.world.fee.FeeService;
import com.pip.itimes.server.world.game.WorldService;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class HackServer {
    private static final Logger log = Logger.getLogger(Server.class);

    private ConnectService connectService = null;
    private StageService stageService = null;
    private ChatService chatService = null;
    private LockService lockService = null;
    private PlayerService playerService = null;
    private PositionService positionService = null;
    private TeamService teamService = null;
    private BufService bufService = null;
    private MailService mailService = null;
    private AuctionService auctionService = null;
    private ShopService shopService = null;
    private BuyService buyService = null;
    private OemService oemService = null;
    private TongService tongService = null;
    private FriendService friendService = null;
    private Configuration configuration = null;
    private AdminService adminService = null;
    private TimeService timeService = null;
    private PhoneService phoneService = null;
    private WorldService worldService = null;
//    private FallService fallService = null;
    private FeeService feeService = null;
    private PetSellService petSellService = null;
    private TreasureService treasureService = null;
    private HopeGrassService hopeGrassService = null;

    public HackServer() {
    }

    public void launch() throws Exception {
        configuration = new PropertiesConfiguration(
                "config.properties");
        connectService = new ConnectService();
        stageService = new StageService(new File(configuration.getString("datadir")));
        stageService.setConnectService(connectService);
//        chatService = new ChatService();
//        chatService.setStageService(stageService);
//        chatService.setConnectService(connectService);
//        chatService.init();
//        lockService = new LockService();
        playerService = new PlayerService(new PlayerDao());
        playerService.setBufService(new BufService());
//        chatService.setPlayerService(playerService);
//        positionService = new PositionService();
//        positionService.setConnectService(connectService);
//        worldService = new WorldService(stageService,positionService);
//        worldService.setConnectService(connectService);
//        stageService.setWorldService(worldService);
//        battleService = new BattleService();
//        battleService.setConnectService(connectService);
//        battleService.setStageService(stageService);
//        battleService.setChatService(chatService);
//        battleService.setPlayerService(playerService);
//        teamService = new TeamService();
//        teamService.setChatService(chatService);
//        battleService.setTeamService(teamService);
//        treasureService = new TreasureService(new TreasureDao());
//        hopeGrassService = new HopeGrassService(new HopeGrassDao());
//        bufService = new BufService();
//        bufService.setConnectService(connectService);
//        bufService.setStageService(stageService);
//        bufService.setTreasureService(treasureService);
//        bufService.setHopeGrassService(hopeGrassService);
//        mailService = new MailService(new MailDao());
//        mailService.setConnectService(connectService);
//        mailService.setPlayerService(playerService);
//        mailService.start();
//        auctionService = new AuctionService(new AuctionDao());
//        auctionService.setMailService(mailService);
//        auctionService.start();
//        shopService = new ShopService(new ShopDao());
//        auctionService.setShopService(shopService);
//        buyService = new BuyService(new BuyDao());
//        oemService = new OemService(new OemDao());
//        tongService = new TongService(new TongDao());
//        tongService.setPlayerService(playerService);
//        tongService.setStageService(stageService);
//        tongService.setConnectService(connectService);
//        tongService.setChatService(chatService);
//        chatService.setTongService(tongService);
//        battleService.setTongService(tongService);
//        friendService = new FriendService();
//        friendService.setConnectService(connectService);
//        friendService.setPlayerService(playerService);
//        adminService = new AdminService(new AdminDao());
//        timeService = new TimeService();
//        timeService.setConnectService(connectService);
//        phoneService = new PhoneService();
//        stageService.setPhoneService(phoneService);
//        fallService = new FallService();
//        fallService.setChatService(chatService);
//        fallService.setConnectService(connectService);
//        fallService.setStageService(stageService);
//        fallService.setPlayerService(playerService);
//        battleService.setFallService(fallService);
//        shopService.setAuctionService(auctionService);
//        shopService.setOemService(oemService);
//        shopService.setBuyService(buyService);
//        log.info("load shops");
//        shopService.loadAllShops();
//        log.info("shops loaded");
//        shopService.start();
//        playerService.setShopService(shopService);
//        playerService.start();
//        feeService = new FeeService();
//
//        feeService.setPlayerService(playerService);
//        feeService.setConnectService(connectService);
//        feeService.setStageService(stageService);
//        feeService.setChatService(chatService);
//        petSellService = new PetSellService();
//        SessionRegistry registry = new SessionRegistry();
//        SessionHandler connectSessionHandler = new ConnectSessionHandler(
//                registry);
//        AuthSessionHandler authSessionHandler = new AuthSessionHandler(registry);
//        log.info("connect auth");
//        connectAuth(authSessionHandler);
//        while(authSession==null);
//        log.info("Auth connected");
//        bind(registry,connectSessionHandler);
//        AdminSessionHandler adminSessionHandler = new AdminSessionHandler(registry);
//        bindAdmin(adminSessionHandler);
//        chatService.start();
//        timeService.start();
//        feeService.setAuthSession(authSession);
        log.info("WorldServer started");
        doSomething();
//        removeIMoneyItemForCMCC();
        System.out.println("All Ok");

        System.exit(1);
//        lookPet(1185);
    }

    public void modifyPet(Pet pet,int level,boolean baby,int petType,int agility,int vitality,int strength,int intelligence,int point,short[] as){
        pet.setLevel(level);
        pet.setBaby(baby);
        pet.setPetType(petType);
        pet.setAgility(agility);
        pet.setVitality(vitality);
        pet.setStrength(strength);
        pet.setIntelligence(intelligence);
        pet.setPoint(point);
        Ability[] abilities = new Ability[5];
        for(int i=0;i<as.length;i++){
            abilities[i] = Ability.getAbility(as[i]);
        }
        pet.setAbilities(abilities);
    }
//    id="1001"  "¶þÁ¬Í»"
//    id="1002"  "·ßÅ­"
//    id="1003" "ÉáÉíÍ»½ø"
//    id="1004" "ÉñÊÞ»¯"
//    id="1005" "¿ñ±©"
//    id="1006" "¶¾Æø"
//    id="1007" "¾«Éñ´´»÷"
//    id="1008"  "Ë«´´»÷"
//    id="1009" "»Ã¾õ"
//    id="1010" "ÒÆÐÎ»»Ó°"
//    id="1011"  "ÁÛ»¯Æ¤·ô"
//    id="1012" "¼áÈÍ"
//    id="1013" "ÃâÒß"
//    id="1014" "°®´÷"
//    id="1015"  "Ç¿»¯Íâ¿Ç"
//    id="1016"  "ÍÌÊÉ"
//    id="1017"  "ÔÙÉú"
//    id="1018"  "ÉúÃüÁ´½Ó"
//    id="1019"  "Áé»êÁ´½Ó"
//    id="1020"  "Çý³ý¶ñÒâ"

    public void doSomething() throws Exception{
        WorldPlayer player = playerService.loadWorldPlayer(137348);
        Pet[] pets = player.getPets();
        for(int i=0;i<pets.length;i++){
            if(i==0)
                // ItemId[7208961]Id[430491]Baby[false]PetType[5]Level[82]Point[0]Agility[71]Strength[14]Vitality[91]Intelligence[180]Abilities[¾«Éñ´´»÷,»Ã¾õ,Áé»êÁ´½Ó,Çý³ý¶ñÒâ,ÃâÒß               »Ã¶¾ÒÆÇý¼á
                modifyPet(pets[i],99,true,5,215,53,12,180,0,new short[]{1010,1009,1019,1016,1012});
//            else
//                //Ë«¾«ÁéÍÌÉá
//                modifyPet(pets[i],1,true,5,13,28,13,28,0,new short[]{1008,1007,1019,1016,1003});

        }

        player.reset();
        playerService.savePlayer(player);
    }

    public void removeIMoneyItemForCMCC() throws Exception{
        int[] itemIds = new int[]{
                        210021, 550012, 550013, 200129, 200125, 200128, 550006, 550007, 200130, 200131, 200132, 200133, 108, 109, 110, 111, 112, 113, 114, 115, 116, 119, 200122, 200123, 200124,
                        200149, 200129, 200150, 200151, 550008, 550011, 550014, 550015, 550012, 550013, 210021, 200125, 200126, 200127, 200128, 200136, 200031, 550006, 550007
        };

        BufferedReader br = null;

        try{
            br = new BufferedReader(new FileReader("playerids.txt"));

            String line = br.readLine();

            int i = 0;

            while(line != null){
                try{
                    WorldPlayer player = playerService.loadWorldPlayer(Integer.parseInt(line));

                    log.info("  " + player.getId() + " : " + player.getPlayerName());

                    Changed changed = new Changed();

                    boolean needSave = false;

                    for(int j = 0; j < itemIds.length; j++){

                        Grid grid = player.getItem(itemIds[j], -1);

                        if(grid != null){
                            needSave = true;
                            player.completeRemoveItem(grid.item, grid.count, changed);
                            log.info("    Remove Item : " + grid.item.getItemId() + " , " + grid.item.getName() + " , " + grid.count);
                        }
                    }

                    if(needSave){
                        player.reset();
                        playerService.savePlayer(player);
                    }
                }catch(Exception e){
                    log.info(e, e);
                }

                i++;

                line = br.readLine();
            }

            log.info("All player : " + i);
        }catch(Exception e){
            log.info(e, e);
        }finally{
            try{
                br.close();
            }catch(Exception e){
            }
        }
    }

//
//    public void doSomething() throws Exception{
//        List l = playerService.loadAllPlayers();
//        for(int i=0;i<l.size();i++){
//            Player pp = (Player)l.get(i);
//            try {
//                PlayerData p = new PlayerData(pp);
//                p.reset();
//                playerService.savePlayer(p);
//                log.info("ID["+p.getId()+"]OK");
//            } catch (Exception ex) {
//                log.info("Error ID["+pp.getId()+"]");
//                log.info(ex,ex);
//            }
//        }
//    }

//    public void doSomething() throws Exception{
//        WorldPlayer player = playerService.loadWorldPlayer(1046);
//        Pet[] pets = player.getPets();
//        for(int i=0;i<pets.length;i++){
//            Pet pet = pets[i];
//            System.out.println("Pet Name:"+pet.getName());
//            System.out.println("Pet Leve:"+pet.getLevel());
//            System.out.println("Pet Baby:"+pet.getBaby());
//            System.out.println("Pet Type:"+pet.getPetType());
//            System.out.println("Pet Point:"+pet.getPoint());
//            System.out.println("Pet PropertiesPoint:"+pet.getPropertyPoints());
//            System.out.println("Pet CurrentPoint:"+pet.getCurrentPoint());
//            System.out.println("Pet Agility:"+pet.getAgility());
//            System.out.println("Pet Strength:"+pet.getStrength());
//            System.out.println("Pet Vitality:"+pet.getVitality());
//            System.out.println("Pet Intelligence:"+pet.getIntelligence());
//            Ability[] abilities = pet.getAbilities();
//            for(int j=0;j<abilities.length;j++){
//                System.out.println("Ability "+j+":"+abilities[j].getId());
//            }
//        }
//    }

    public void lookPet(int id) throws Exception{
        WorldPlayer player = playerService.loadWorldPlayer(30395);
        Pet[] pets = player.getPets();
        for(int i=0;i<pets.length;i++){
            System.out.println("Pet:"+pets[i].getId());
        }
    }

    public static void main(String[] args) {
        try {
            HackServer server = new HackServer();
            server.launch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
