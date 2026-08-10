package peony.db;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import peony.auction.AuctionDAO;
import peony.common.AsyncCall;
import peony.game.association.AssociationDao;
import peony.game.beautyparade.BeautySignDao;
import peony.game.beautyparade.BeautyVoteDao;
import peony.game.instance.BossScoreDao;
import peony.game.instance.BossTimeScoreDao;
import peony.game.nation.CandidateDao;
import peony.service.Service;
import peony.service.account.ChargeDao;
import peony.service.tong.apply.TongBattleApplyDao;

public class DBService implements Service {
	
	public PlayerDAO playerDAO = new PlayerDAO();
	public MailDAO mailDAO = new MailDAO();
	public PlayerRelationDAO playerRelationDAO = new PlayerRelationDAO();
	public TongDAO tongDAO = new TongDAO();
	public TongMemberDAO tongMemberDAO = new TongMemberDAO();
	public GMRequestDAO gmQuestDAO = new GMRequestDAO();
	public AuctionDAO auctionDAO = new AuctionDAO();
	public GiftDAO giftDAO = new GiftDAO();
	public AlphaGiftDAO alphaGiftDAO = new AlphaGiftDAO();
	public PropertyDAO propertyDAO = new PropertyDAO();
	public IBuyDAO ibuyDAO = new IBuyDAO();
	public PvpInfoDAO pvpInfoDAO = new PvpInfoDAO();
	public CandidateDao candidateDao = new CandidateDao();
	public NationDAO nationDAO = new NationDAO();
	public ForbidDAO forbidDAO = new ForbidDAO();
	public PunishDAO punishDAO = new PunishDAO();
	public OfficerDAO officerDAO = new OfficerDAO(); 
	public NationRelDAO nationRelDAO = new NationRelDAO();
	public BbsDAO bbsDAO = new BbsDAO();
	public BossScoreDao bossScoreDao = new BossScoreDao();
	public BossTimeScoreDao bossTimeScoreDao = new BossTimeScoreDao();
	public TongBattleApplyDao tongBattleApplyDao = new TongBattleApplyDao();
	public ClientBbsDAO clientBbsDao = new ClientBbsDAO();
	public BeautySignDao beautySignDao = new BeautySignDao();
	public BeautyVoteDao beautyVoteDao = new BeautyVoteDao();
	public ChargeDao chargeDao = new ChargeDao();
	public FameDAO fameDAO = new FameDAO();
	public AssociationDao associationDao = new AssociationDao();
	public AccountDepotDAO accountDepotDAO = new AccountDepotDAO();
	
	
	
	protected ExecutorService executor = new ThreadPoolExecutor(5,10,60L,TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
	
	public DBService(){
	}
	
	public void startup() {
	}
	
	public void shutdown() {
		
	}
	
	public void schedule(AsyncCall call){
		executor.execute(call);
	}
}
