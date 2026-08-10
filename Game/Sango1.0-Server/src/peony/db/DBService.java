package peony.db;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import peony.auction.AuctionDAO;
import peony.common.AsyncCall;
import peony.game.association.AssociationDao;
import peony.game.asyncbattle.AsyncNormalBoardDao;
import peony.game.beautyparade.BeautySignDao;
import peony.game.beautyparade.BeautyVoteDao;
import peony.game.instance.BossScoreDao;
import peony.game.instance.BossTimeScoreDao;
import peony.game.nation.CandidateDao;
import peony.game.stepserver.StepBattleScoreDao;
import peony.game.stepserver.StepBattleScore_FinalsDao;
import peony.service.Service;
import peony.service.account.AccountPropertyDao;
import peony.service.account.BindImoneyDao;
import peony.service.account.ChargeDao;
import peony.service.account.FirstChargeDao;
import peony.service.exam.ExamBoardDao;
import peony.service.exam.ExamPublishBoardDao;
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
	public RankingDAO rankingDAO = new RankingDAO();
	public BindImoneyDao bindImoneyDao = new BindImoneyDao();
	public FirstChargeDao firstChargeDao = new FirstChargeDao();
	public StepBattleScoreDao stepbattlescoreDAO=new StepBattleScoreDao();
	public StepBattleScore_FinalsDao stepbattlescore_FinalsDAO=new StepBattleScore_FinalsDao();
	public AccountPropertyDao accountPropertyDao = new AccountPropertyDao();
	public AsyncNormalBoardDao asyncnormalboardDao = new AsyncNormalBoardDao();
	public ExamBoardDao examBoardDao = new ExamBoardDao();
	public ExamPublishBoardDao examPublishBoardDao = new ExamPublishBoardDao();
	
	
	protected ExecutorService executor = new ThreadPoolExecutorEx(8,10,60L,TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
	
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
